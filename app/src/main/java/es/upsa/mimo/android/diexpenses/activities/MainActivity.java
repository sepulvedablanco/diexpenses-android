package es.upsa.mimo.android.diexpenses.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.upsa.mimo.android.diexpenses.R;
import es.upsa.mimo.android.diexpenses.events.ProfileImageChanged;
import es.upsa.mimo.android.diexpenses.fragments.BankAccountsFragment;
import es.upsa.mimo.android.diexpenses.fragments.HomeFragment;
import es.upsa.mimo.android.diexpenses.fragments.KindsFragment;
import es.upsa.mimo.android.diexpenses.fragments.MovementsFragment;
import es.upsa.mimo.android.diexpenses.fragments.StatisticsFragment;
import es.upsa.mimo.android.diexpenses.models.User;
import es.upsa.mimo.android.diexpenses.utils.Constants;
import es.upsa.mimo.android.diexpenses.utils.Diexpenses;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private User user;

    @Bind(R.id.main_drawer_layout) DrawerLayout mDrawerLayout;
    @Bind(R.id.main_nav_view) NavigationView navView;
    private ImageView ivProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        ButterKnife.bind(this);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            user = bundle.getParcelable(Constants.Parcelables.USER);
        }

        LayoutInflater.from(getApplicationContext()).inflate(R.layout.nav_header_main, navView);

        navView.setNavigationItemSelectedListener(this);
        navView.setCheckedItem(R.id.nav_home);
        setTitle(navView.getMenu().getItem(0).getTitle());

        Log.d(TAG, user.toString());
        ivProfileImage = ButterKnife.findById(navView, R.id.drawer_avatar);
        TextView tvName = ButterKnife.findById(navView, R.id.drawer_name);
        TextView tvUser = ButterKnife.findById(navView, R.id.drawer_user);
        checkAndSetProfileImage();
        tvName.setText(user.getName());
        tvUser.setText(user.getUser());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerStateChanged(int newState) {
                if((newState == DrawerLayout.STATE_SETTLING || newState == DrawerLayout.STATE_DRAGGING)
                        && !mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    closeKeyboard();
                }
                super.onDrawerStateChanged(newState);
            }
        };
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        onNavigationItemSelected(R.id.nav_home);
    }

    private void checkAndSetProfileImage() {
        try {
            String diexpensesFolder = Diexpenses.getAppFolder(this);
            File file = new File(diexpensesFolder, Constants.Preferences.PROFILE_IMAGE_NAME);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                ivProfileImage.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while loading profile image", e);
        }
    }

    @Subscribe
    public void onEvent(ProfileImageChanged event) {
        ivProfileImage.setImageBitmap(event.getBitmapProfileImage());
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            Log.d(TAG, "Popping backstack");
            fm.popBackStack();
        } else {
            Log.d(TAG, "Nothing on backstack. Calling super");
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Log.d(TAG, "Settings selected");
            Intent intent = new Intent(this, PreferencesActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_logout) {
            Log.d(TAG, "Logout selected");
            doLogout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void doLogout() {
        String methodName = "doLogout - ";
        Log.d(TAG, methodName + "start");

        Diexpenses.removeUserFromSharedPreferences(this);
        Intent intentLoginActivity = new Intent(this, LoginActivity.class);
        intentLoginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentLoginActivity);

        Log.d(TAG, methodName + "end");
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        int id = item.getItemId();

        onNavigationItemSelected(id);

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onNavigationItemSelected(int id) {

        if(id != R.id.nav_contact && id != R.id.nav_about) {
            navView.setCheckedItem(id);
            setTitle(navView.getMenu().findItem(id).getTitle());
        }

        if (id == R.id.nav_home) {
            Log.d(TAG, "Home selected");
            Fragment homeFragment = HomeFragment.newInstance(user);
            getFragmentManager().beginTransaction().replace(R.id.content_main, homeFragment).commit();
        } else if (id == R.id.nav_movements) {
            Log.d(TAG, "Movements selected");
            Fragment movementsFragment = MovementsFragment.newInstance(user.getId());
            getFragmentManager().beginTransaction().replace(R.id.content_main, movementsFragment).commit();
        } else if (id == R.id.nav_types) {
            Log.d(TAG, "Types selected");
            Fragment kindsFragment = KindsFragment.newInstance(user.getId());
            getFragmentManager().beginTransaction().replace(R.id.content_main, kindsFragment).commit();
        } else if (id == R.id.nav_accounts) {
            Log.d(TAG, "Accounts selected");
            Fragment bankAccountsFragment = BankAccountsFragment.newInstance(user.getId());
            getFragmentManager().beginTransaction().replace(R.id.content_main, bankAccountsFragment).commit();
        } else if (id == R.id.nav_statistics) {
            Log.d(TAG, "Statistics selected");
            Fragment statisticsFragment = StatisticsFragment.newInstance(user.getId());
            getFragmentManager().beginTransaction().replace(R.id.content_main, statisticsFragment).commit();
        } else if (id == R.id.nav_contact) {
            Log.d(TAG, "Contact selected");
            Intent intentContactActivity = new Intent(Intent.ACTION_SENDTO);
            intentContactActivity.setData(Uri.parse("mailto:"));
            intentContactActivity.putExtra(Intent.EXTRA_EMAIL, getString(R.string.email_to));
            intentContactActivity.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
            startActivity(Intent.createChooser(intentContactActivity, getString(R.string.send_mail)));
        } else if (id == R.id.nav_about) {
            Log.d(TAG, "About selected");
            Intent intentAboutActivity = new Intent(this, AboutActivity.class);
            startActivity(intentAboutActivity);
        }
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
package es.upsa.mimo.android.diexpenses.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import es.upsa.mimo.android.diexpenses.R;
import es.upsa.mimo.android.diexpenses.models.User;
import es.upsa.mimo.android.diexpenses.utils.Constants;
import es.upsa.mimo.android.diexpenses.utils.Diexpenses;

public class EntryActivity extends AppCompatActivity {

    private static final String TAG = EntryActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        checkUserLogIn();
    }

    private void checkUserLogIn() {
        String methodName = "checkUserLogIn - ";
        Log.d(TAG, methodName + "start");

        User user = Diexpenses.getUserFromSharedPreferences(this);
        if(user == null) {
            Log.d(TAG, methodName + "there is not data in shared preferences");
            showLoginActivity();
        } else {
            checkAndShowRateAppAlert();

            Log.d(TAG, methodName + "user is logged in");
            showMainActivity(user);
        }
        Log.d(TAG, methodName + "end");
    }

    private void showLoginActivity() {
        String methodName = "showLoginActivity - ";
        Log.d(TAG, methodName + "start");
        Intent intentLoginActivity = new Intent(this, LoginActivity.class);
        startActivity(intentLoginActivity);
        Log.d(TAG, methodName + "end");
    }

    private void showMainActivity(User user) {
        String methodName = "showMainActivity - ";
        Log.d(TAG, methodName + "start");
        Intent intentHomeActivity = new Intent(this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.Parcelables.USER, user);
        intentHomeActivity.putExtras(bundle);
        intentHomeActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentHomeActivity);
        Log.d(TAG, methodName + "end");
    }

    private void checkAndShowRateAppAlert() {
        if (checkRateAppAlert()) {
            showRateAppAlert();
        }
    }

    private boolean checkRateAppAlert() {
        boolean hasRated = Diexpenses.hasRated(getApplicationContext());
        if(hasRated) {
            return false;
        }
        int counterOfUses = Diexpenses.incrementAppUse(getApplicationContext());
        return counterOfUses % 10 == 0;
    }

    private void showRateAppAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(EntryActivity.this);
        alert.setTitle(getString(R.string.rate_title));
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setMessage(getString(R.string.rate_message));

        alert.setPositiveButton(getString(R.string.common_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Do nothing
            }
        });

        alert.setNegativeButton(getString(R.string.rate_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                try {
                    intent.setData(Uri.parse(getString(R.string.protocol_market) + getPackageName()));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }

                } catch (android.content.ActivityNotFoundException anfe) {
                    intent.setData(Uri.parse(getString(R.string.protocol_market_url) + getPackageManager()));
                    startActivity(intent);
                }
            }
        });
        alert.show();
    }
}

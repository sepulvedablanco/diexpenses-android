package es.upsa.mimo.android.diexpenses.activities;

import android.content.Intent;
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
            Diexpenses.incrementAppUse(getApplicationContext());
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
}

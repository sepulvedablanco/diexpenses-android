package es.upsa.mimo.android.diexpenses.activities;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import es.upsa.mimo.android.diexpenses.R;
import es.upsa.mimo.android.diexpenses.api.Requester;
import es.upsa.mimo.android.diexpenses.models.Credential;
import es.upsa.mimo.android.diexpenses.models.User;
import es.upsa.mimo.android.diexpenses.utils.Constants;
import es.upsa.mimo.android.diexpenses.utils.Diexpenses;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    @BindView(R.id.signup_user) TextInputLayout tilUser;
    @BindView(R.id.signup_password) TextInputLayout tilPassword;
    @BindView(R.id.login_signIn) Button btnLogin;
    @BindView(R.id.login_signup) TextView tvSignup;
    @BindView(R.id.login_progress_bar) ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

    }

    @OnEditorAction(R.id.login_passwordET)
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        if(arg1 == EditorInfo.IME_ACTION_DONE) {
            onSignIn();
        }
        return false;
    }

    @OnClick(R.id.login_signIn)
    public void onSignIn() {
        Log.d(TAG, "onSignIn - start");

        btnLogin.setEnabled(false);

        resetErrors();

        String user = tilUser.getEditText().getText().toString();
        String password = tilPassword.getEditText().getText().toString();

        boolean valid = checkLogin(user, password);
        if(!valid) {
            btnLogin.setEnabled(true);
            Log.i(TAG, "Invalid data");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        doLogin(user, password);
        Log.d(TAG, "onSignIn - end");
    }

    private void doLogin(String user, String password) {
        Call<User> call = Requester.getInstance().getExpensesApi().doLogin(new Credential(user, password));
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                resetCommonComponentes();

                User user = response.body();
                if (user != null) {
                    toHomeScreen(user);
                } else {
                    tilPassword.setError(getString(R.string.error_incorrect_password));
                    tilPassword.requestFocus();
                }
            }

            private void toHomeScreen(User user) {
                String methodName = "toHomeScreen - ";
                Log.d(TAG, methodName + "start");
                Diexpenses.setUserInSharedPreferences(LoginActivity.this, user);
                Intent intentHomeActivity = new Intent(LoginActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(Constants.Parcelables.USER, user);
                intentHomeActivity.putExtras(bundle);
                intentHomeActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentHomeActivity);
                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d(TAG, "Error performing login", t);
            }

        });
    }

    private void resetErrors() {
        tilUser.setError(null);
        tilUser.setErrorEnabled(false);
        tilPassword.setError(null);
        tilPassword.setErrorEnabled(false);
    }

    private boolean checkLogin(String user, String password) {
        boolean valid = true;
        if(user == null || user.isEmpty()) {
            tilUser.setError(getString(R.string.common_field_required));
            tilUser.setErrorEnabled(true);
            valid = false;
        }
        if(password == null || password.isEmpty()) {
            tilPassword.setError(getString(R.string.common_field_required));
            tilPassword.setErrorEnabled(true);
            valid = false;
        }
        return valid;
    }

    @OnClick(R.id.login_signup)
    public void onCreateAccount() {
        Log.d(TAG, "onCreateAccount - start");
        Intent intentSignupActivity = new Intent(this, SignupActivity.class);
        intentSignupActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentSignupActivity);
    }

    private void resetCommonComponentes() {
        progressBar.setVisibility(View.GONE);
        btnLogin.setEnabled(true);
    }
}

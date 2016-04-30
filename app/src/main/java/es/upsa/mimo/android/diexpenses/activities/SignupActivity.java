package es.upsa.mimo.android.diexpenses.activities;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.upsa.mimo.android.diexpenses.R;
import es.upsa.mimo.android.diexpenses.api.Requester;
import es.upsa.mimo.android.diexpenses.models.Credential;
import es.upsa.mimo.android.diexpenses.models.GenericResponse;
import es.upsa.mimo.android.diexpenses.models.NewUser;
import es.upsa.mimo.android.diexpenses.models.User;
import es.upsa.mimo.android.diexpenses.utils.Constants;
import es.upsa.mimo.android.diexpenses.utils.Diexpenses;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = SignupActivity.class.getSimpleName();

    @Bind(R.id.signup_name) TextInputLayout tilName;
    @Bind(R.id.signup_user) TextInputLayout tilUser;
    @Bind(R.id.signup_password) TextInputLayout tilPassword;
    @Bind(R.id.signup_confirm_password) TextInputLayout tilConfirmPassword;
    @Bind(R.id.signup_action) Button btnSignup;
    @Bind(R.id.signup_login_now) TextView tvLogin;
    @Bind(R.id.signup_progress_bar) ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.signup_action)
    public void onSignUp(View view) {
        Log.d(TAG, "onSignUp - start");

        btnSignup.setEnabled(false);

        resetErrors();

        // Store values at the time of the login attempt.
        String name = tilName.getEditText().getText().toString();
        String user = tilUser.getEditText().getText().toString();
        String password = tilPassword.getEditText().getText().toString();
        String confirmPassword = tilConfirmPassword.getEditText().getText().toString();

        boolean valid = checkForm(name, user, password, confirmPassword);
        if(!valid) {
            btnSignup.setEnabled(true);
            Log.i(TAG, "Invalid data");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        NewUser newUser = new NewUser(name, user, password);
        createUser(newUser);
        Log.d(TAG, "onSignUp - end");
    }

    @OnClick(R.id.signup_login_now)
    public void onLogin(View view) {
        Log.d(TAG, "onLogin - start");
        Intent intentLoginActivity = new Intent(this, LoginActivity.class);
        intentLoginActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentLoginActivity);
        Log.d(TAG, "onLogin - end");
    }

    private void resetErrors() {
        tilName.setError(null);
        tilName.setErrorEnabled(false);
        tilUser.setError(null);
        tilUser.setErrorEnabled(false);
        tilPassword.setError(null);
        tilPassword.setErrorEnabled(false);
        tilConfirmPassword.setError(null);
        tilConfirmPassword.setErrorEnabled(false);
    }

    private boolean checkForm(String name, String user, String password, String confirmPassword) {
        Log.d(TAG, "onLogin - start");
        boolean valid = true;
        if(name == null || name.isEmpty()) {
            tilName.setError(getString(R.string.common_field_required));
            tilName.setErrorEnabled(true);
            valid = false;
        }
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
        if(confirmPassword == null || confirmPassword.isEmpty()) {
            tilConfirmPassword.setError(getString(R.string.common_field_required));
            tilConfirmPassword.setErrorEnabled(true);
            valid = false;
        }
        if(password != null && !password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.different_passwords));
            tilConfirmPassword.setErrorEnabled(true);
            valid = false;
        }
        Log.d(TAG, "onLogin - valid=" + valid + " - end");
        return valid;
    }

    private void createUser(final NewUser newUser) {
        Call<GenericResponse> call = Requester.getInstance().getExpensesApi().createUser(newUser);
        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                GenericResponse genericResponse = response.body();
                if (genericResponse == null) {

                    return;
                }

                Log.d(TAG, genericResponse.toString());
                if(genericResponse.getCode() == 3) {
                    doLogin(newUser.getUser(), newUser.getPass());
                    return;
                }

                Snackbar.make(tilConfirmPassword, genericResponse.getMessage(), Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Log.d(TAG, "Error creating user", t);
            }

        });
    }

    private void doLogin(String user, String password) {
        Call<User> call = Requester.getInstance().getExpensesApi().doLogin(new Credential(user, password));
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                progressBar.setVisibility(View.GONE);

                User user = response.body();
                if(user == null) {

                    return;
                }

                Diexpenses.setUserInSharedPreferences(SignupActivity.this, user);

                showMainActivity(user);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d(TAG, "Error performing login", t);
            }
        });
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

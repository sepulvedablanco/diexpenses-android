package es.upsa.mimo.android.diexpenses.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.ParseException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import es.upsa.mimo.android.diexpenses.R;
import es.upsa.mimo.android.diexpenses.api.Requester;
import es.upsa.mimo.android.diexpenses.models.BankAccount;
import es.upsa.mimo.android.diexpenses.models.GenericResponse;
import es.upsa.mimo.android.diexpenses.utils.Constants;
import es.upsa.mimo.android.diexpenses.utils.Diexpenses;
import es.upsa.mimo.android.diexpenses.utils.NumericFilter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Diego on 24/4/16.
 */
public class BankAccountDetailsFragment extends Fragment {

    private static final String TAG = BankAccountDetailsFragment.class.getSimpleName();

    private Long userId;
    private BankAccount bankAccount;

    @Bind(R.id.bank_account_description) TextInputLayout tilDescription;
    @Bind(R.id.bank_account_iban) TextInputLayout tilIban;
    @Bind(R.id.bank_account_entity) TextInputLayout tilEntity;
    @Bind(R.id.bank_account_office) TextInputLayout tilOffice;
    @Bind(R.id.bank_account_control_digit) TextInputLayout tilControlDigit;
    @Bind(R.id.bank_account_number) TextInputLayout tilAccountNumber;
    @Bind(R.id.bank_account_balance) TextInputLayout tilBalance;
    @Bind(R.id.bank_account_action) Button btnAction;
    @Bind(R.id.bank_account_progress_bar) ProgressBar progressBar;

    public static BankAccountDetailsFragment newInstance(Long userId) {
        return  newInstance(userId, null);
    }

    public static BankAccountDetailsFragment newInstance(Long userId, BankAccount bankAccount) {
        BankAccountDetailsFragment bankAccountsFragment = new BankAccountDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.Bundles.USER_ID, userId);
        bundle.putParcelable(Constants.Parcelables.BANK_ACCOUNT, bankAccount);
        bankAccountsFragment.setArguments(bundle);
        return bankAccountsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bank_account, container, false);
        Bundle bundle = getArguments();
        userId = bundle.getLong(Constants.Bundles.USER_ID);
        bankAccount = bundle.getParcelable(Constants.Parcelables.BANK_ACCOUNT);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() != null) {

            ButterKnife.bind(this, getView());

            configBalanceField();

            if(bankAccount != null) {
                fillForm(bankAccount);
            }

            configActionButton(bankAccount == null ? R.string.common_create : R.string.common_update);
        }
    }

    private void configBalanceField() {
        String allowedCharacteres = "0123456789" + Diexpenses.getDecimalSeparator();
        tilBalance.getEditText().setKeyListener(DigitsKeyListener.getInstance(allowedCharacteres));
        InputFilter[] filter = new InputFilter[1];
        filter[0] = new NumericFilter();
        tilBalance.getEditText().setFilters(filter);
    }

    private void fillForm(BankAccount bankAccount) {
        tilDescription.getEditText().setText(bankAccount.getDescription());
        tilIban.getEditText().setText(bankAccount.getIban());
        tilEntity.getEditText().setText(bankAccount.getEntity());
        tilOffice.getEditText().setText(bankAccount.getOffice());
        tilControlDigit.getEditText().setText(bankAccount.getControlDigit());
        tilAccountNumber.getEditText().setText(bankAccount.getAccountNumber());
        tilBalance.getEditText().setText(Diexpenses.formatAmount(bankAccount.getBalance()));
    }

    private void configActionButton(@StringRes int titleId) {
        btnAction.setText(titleId);
    }

    @OnEditorAction(R.id.bank_account_balance_et)
    public boolean onEditorAction(TextView arg0, int keyCode, KeyEvent event) {
        if(keyCode == EditorInfo.IME_ACTION_DONE) {
            onCreateOrEditBankAccount();
        }
        return false;
    }

    @OnClick(R.id.bank_account_action)
    public void onCreateOrEditBankAccount() {

        btnAction.setEnabled(false);

        resetErrors();

        boolean isValidForm = checkForm();
        if(!isValidForm) {
            btnAction.setEnabled(true);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        try {
            BankAccount account = formToBankAccount();
            if (bankAccount == null) {
                createBankAccount(account);
            } else {
                account.setId(bankAccount.getId());
                editBankAccount(userId, account);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Invalid amount: " + tilBalance.getEditText().getText().toString());
            setErrorInField(tilBalance, getString(R.string.common_invalid_amount));
            resetCommonComponentes();
        }
    }

    private void resetErrors() {
        resetError(tilDescription);
        resetError(tilIban);
        resetError(tilEntity);
        resetError(tilOffice);
        resetError(tilControlDigit);
        resetError(tilAccountNumber);
        resetError(tilBalance);
    }

    private void resetError(TextInputLayout view) {
        view.setError(null);
        view.setErrorEnabled(false);
    }


    private BankAccount formToBankAccount() throws ParseException {
        BankAccount account = new BankAccount();
        account.setDescription(tilDescription.getEditText().getText().toString());
        String iban = tilIban.getEditText().getText().toString();
        account.setIban(iban.isEmpty() ? null : iban);
        account.setEntity(tilEntity.getEditText().getText().toString());
        account.setOffice(tilOffice.getEditText().getText().toString());
        account.setControlDigit(tilControlDigit.getEditText().getText().toString());
        account.setAccountNumber(tilAccountNumber.getEditText().getText().toString());
        account.setBalance(Diexpenses.parseAmount(tilBalance.getEditText().getText().toString()));
        return account;
    }

    private boolean checkForm() {
        String methodName = "checkForm - ";
        Log.d(TAG, methodName + "start");

        boolean isValidForm = true;

        isValidForm = checkRequiredField(tilDescription, isValidForm);
        isValidForm = checkRequiredField(tilEntity, isValidForm, 4);
        isValidForm = checkRequiredField(tilOffice, isValidForm, 4);
        isValidForm = checkRequiredField(tilControlDigit, isValidForm, 2);
        isValidForm = checkRequiredField(tilAccountNumber, isValidForm, 10);
        isValidForm = checkRequiredField(tilBalance, isValidForm);

        Log.d(TAG, methodName + "end. isValidForm=" + isValidForm);
        return isValidForm;
    }

    private boolean checkRequiredField(TextInputLayout tilField, boolean isValidForm, int length) {
        String text = tilField.getEditText().getText().toString();
        if(text.isEmpty()) {
            setErrorInField(tilField, getString(R.string.common_field_required));
            return false;
        } else if (text.length() < length) {
            setErrorInField(tilField, getString(R.string.common_numeric_field_required_with_lenght, length));
            return false;
        }
        return isValidForm;
    }

    private void setErrorInField(TextInputLayout tilField, String strError) {
        tilField.setError(strError);
        tilField.setErrorEnabled(true);
    }

    private boolean checkRequiredField(TextInputLayout etField, boolean isValidForm) {
        return checkRequiredField(etField, isValidForm, 0);
    }

    private void createBankAccount(BankAccount account) {
        String methodName = "createBankAccount - ";
        Log.d(TAG, methodName + "start. Account=" + account.toString());

        Call<GenericResponse> callCreateBankAccount = Requester.getInstance().getExpensesApi().createBankAccount(userId, account);
        callCreateBankAccount.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                String methodName = "onResponse(CreateBankAccount) - ";
                Log.d(TAG, methodName + "start");

                resetCommonComponentes();

                boolean isValidResponse = Requester.processResponse(response, BankAccountDetailsFragment.this);
                if(!isValidResponse) {
                    return;
                }

                GenericResponse genericResponse = response.body();
                Log.d(TAG, "Response=" + genericResponse.toString());
                if (genericResponse != null && genericResponse.getCode() == 30) {
//                    EventBus.getDefault().post(new BankAccountCreateOrUpdate());
                    getFragmentManager().popBackStack();
                } else {
                    Snackbar.make(getView(), R.string.bank_account_unknow_error_creating, Snackbar.LENGTH_LONG).show();
                }
                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                resetCommonComponentes();
                Snackbar.make(getView(), R.string.bank_account_error, Snackbar.LENGTH_LONG).show();
            }
        });
        Log.d(TAG, methodName + "end");
    }

    private void editBankAccount(Long userId, BankAccount account) {
        String methodName = "editBankAccount - ";
        Log.d(TAG, methodName + "start. Account=" + account.toString());

        Call<GenericResponse> callEditBankAccount = Requester.getInstance().getExpensesApi().editBankAccount(userId, account.getId(), account);
        callEditBankAccount.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                String methodName = "onResponse(EditBankAccount) - ";
                Log.d(TAG, methodName + "start");

                resetCommonComponentes();

                boolean isValidResponse = Requester.processResponse(response, BankAccountDetailsFragment.this);
                if(!isValidResponse) {
                    return;
                }

                GenericResponse genericResponse = response.body();
                Log.d(TAG, "Response=" + genericResponse.toString());
                if (genericResponse != null && genericResponse.getCode() == 39) {
//                    EventBus.getDefault().post(new BankAccountCreateOrUpdate());
                    getFragmentManager().popBackStack();
                } else {
                    Snackbar.make(getView(), R.string.bank_account_unknow_error_updating, Snackbar.LENGTH_LONG).show();
                }
                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                resetCommonComponentes();
                Snackbar.make(getView(), R.string.bank_account_error_updating, Snackbar.LENGTH_LONG).show();
            }
        });
        Log.d(TAG, methodName + "end");
    }

    private void resetCommonComponentes() {
        progressBar.setVisibility(View.GONE);
        btnAction.setEnabled(true);
    }
}
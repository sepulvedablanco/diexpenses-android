package es.upsa.mimo.android.diexpenses.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
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

    @Bind(R.id.bank_account_description) EditText etDescription;
    @Bind(R.id.bank_account_iban) EditText etIban;
    @Bind(R.id.bank_account_entity) EditText etEntity;
    @Bind(R.id.bank_account_office) EditText etOffice;
    @Bind(R.id.bank_account_control_digit) EditText etControlDigit;
    @Bind(R.id.bank_account_number) EditText etAccountNumber;
    @Bind(R.id.bank_account_balance) EditText etBalance;
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

            fillForm(bankAccount);
        }
    }

    private void fillForm(BankAccount bankAccount) {
        if(bankAccount == null) {
            btnAction.setText(R.string.common_create);
        } else {
            etDescription.setText(bankAccount.getDescription());
            etIban.setText(bankAccount.getIban());
            etEntity.setText(bankAccount.getEntity());
            etOffice.setText(bankAccount.getOffice());
            etControlDigit.setText(bankAccount.getControlDigit());
            etAccountNumber.setText(bankAccount.getAccountNumber());
            etBalance.setText(Diexpenses.formatAmount(bankAccount.getBalance()));
            btnAction.setText(R.string.common_update);
        }
    }

    @OnEditorAction(R.id.bank_account_balance)
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        if(arg1 == EditorInfo.IME_ACTION_DONE) {
            onCreateOrEditBankAccount();
        }
        return false;
    }

    @OnClick(R.id.bank_account_action)
    public void onCreateOrEditBankAccount() {

        btnAction.setEnabled(false);

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
        } catch (Exception e) {
            Log.e(TAG, "Error while creating or editing bank account:", e);
        }
    }

    private BankAccount formToBankAccount() throws ParseException {
        BankAccount account = new BankAccount();
        account.setDescription(etDescription.getText().toString());
        String iban = etIban.getText().toString();
        account.setIban(iban.isEmpty() ? null : iban);
        account.setEntity(etEntity.getText().toString());
        account.setOffice(etOffice.getText().toString());
        account.setControlDigit(etControlDigit.getText().toString());
        account.setAccountNumber(etAccountNumber.getText().toString());
        account.setBalance(Diexpenses.parseAmount(etBalance.getText().toString()));
        return account;
    }

    private boolean checkForm() {
        String methodName = "checkForm - ";
        Log.d(TAG, methodName + "start");

        boolean isValidForm = true;

        isValidForm = checkRequiredField(etDescription, isValidForm);
        isValidForm = checkRequiredField(etEntity, isValidForm);
        isValidForm = checkRequiredField(etOffice, isValidForm);
        isValidForm = checkRequiredField(etControlDigit, isValidForm);
        isValidForm = checkRequiredField(etAccountNumber, isValidForm);
        isValidForm = checkRequiredField(etBalance, isValidForm);

        Log.d(TAG, methodName + "end. isValidForm=" + isValidForm);
        return isValidForm;
    }

    private boolean checkRequiredField(EditText etField, boolean isValidForm) {
        if(etField.getText().toString().isEmpty()) {
            etField.setText(R.string.common_field_required);
            return false;
        }
        return isValidForm;
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

                progressBar.setVisibility(View.GONE);

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

                progressBar.setVisibility(View.GONE);

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
                    btnAction.setEnabled(true);
                    Snackbar.make(getView(), R.string.bank_account_unknow_error_updating, Snackbar.LENGTH_LONG).show();
                }
                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Snackbar.make(getView(), R.string.bank_account_error_updating, Snackbar.LENGTH_LONG).show();
            }
        });
        Log.d(TAG, methodName + "end");
    }

}
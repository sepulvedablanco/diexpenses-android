package es.upsa.mimo.android.diexpenses.fragments;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import es.upsa.mimo.android.diexpenses.R;
import es.upsa.mimo.android.diexpenses.adapters.BankAccountSpinAdapter;
import es.upsa.mimo.android.diexpenses.adapters.KindBaseSpinAdapter;
import es.upsa.mimo.android.diexpenses.api.Requester;
import es.upsa.mimo.android.diexpenses.comparators.BankAccountComparator;
import es.upsa.mimo.android.diexpenses.comparators.KindBaseComparator;
import es.upsa.mimo.android.diexpenses.dialogs.DatePickerDialogFragment;
import es.upsa.mimo.android.diexpenses.models.BankAccount;
import es.upsa.mimo.android.diexpenses.models.GenericResponse;
import es.upsa.mimo.android.diexpenses.models.Kind;
import es.upsa.mimo.android.diexpenses.models.Movement;
import es.upsa.mimo.android.diexpenses.models.Subkind;
import es.upsa.mimo.android.diexpenses.utils.Constants;
import es.upsa.mimo.android.diexpenses.utils.Diexpenses;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Diego on 24/4/16.
 */
public class MovementDetailsFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = MovementDetailsFragment.class.getSimpleName();

    private Long userId;
    private Long kindIdSelected;

    private boolean subkindsLoaded = false;
    private boolean bankAccountsLoaded = false;

    @Bind(R.id.movement_toggle_expense) ToggleButton etToggleExpense;
    @Bind(R.id.movement_concept) TextInputLayout tilConcept;
    @Bind(R.id.movement_amount) TextInputLayout tilAmount;
    @Bind(R.id.movement_kind) Spinner spinnerKind;
    @Bind(R.id.movement_subkind) Spinner spinnerSubkind;
    @Bind(R.id.movement_bank_account) Spinner spinnerBankAccount;
    @Bind(R.id.movement_transaction_date) TextView tvTransactionDate;
    @Bind(R.id.movement_progress_bar) ProgressBar progressBar;
    @Bind(R.id.movement_action) Button btnAction;

    public static MovementDetailsFragment newInstance(Long userId) {
        MovementDetailsFragment movementFragment = new MovementDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.Bundles.USER_ID, userId);
        movementFragment.setArguments(bundle);
        return movementFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movement, container, false);
        Bundle bundle = getArguments();
        userId = bundle.getLong(Constants.Bundles.USER_ID);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() != null) {

            ButterKnife.bind(this, getView());

            etToggleExpense.setChecked(true);
            tvTransactionDate.setText(Diexpenses.formatDate(new Date()));

            progressBar.setVisibility(View.VISIBLE);

            loadKinds();
            loadBankAccounts();
        }
    }

    private void loadKinds() {
        Call<List<Kind>> kindsCall = Requester.getInstance().getExpensesApi().getKinds(userId);
        kindsCall.enqueue(getKindsCallback());
    }

    public Callback<List<Kind>> getKindsCallback() {
        return new Callback<List<Kind>>() {
            @Override
            public void onResponse(Call<List<Kind>> call, Response<List<Kind>> response) {
                String methodName = "onResponse - ";
                Log.d(TAG, methodName + "start");

                List<Kind> lstKinds = response.body();
                Collections.sort(lstKinds, new KindBaseComparator());
                KindBaseSpinAdapter<Kind> kindAdapter = new KindBaseSpinAdapter<Kind>(getActivity(), android.R.layout.simple_spinner_item, lstKinds);
                spinnerKind.setAdapter(kindAdapter);

                kindIdSelected = lstKinds.get(0).getId();
                loadSubkinds(kindIdSelected);
                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<List<Kind>> call, Throwable t) {
                Log.d(TAG, "Error getting kinds", t);
            }
        };
    }

    @OnClick(R.id.movement_action)
    public void onNewMovement() {

        btnAction.setEnabled(false);

        resetErrors();

        boolean isValidForm = checkForm();
        if (!isValidForm) {
            btnAction.setEnabled(true);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        try {
            Movement movement = formToMovement();
            createMovement(movement);
        } catch (ParseException e) {
            Log.e(TAG, "Error getting movement:", e);
        }
    }

    private void resetErrors() {
        resetError(tilConcept);
        resetError(tilAmount);
    }

    private void resetError(TextInputLayout view) {
        view.setError(null);
        view.setErrorEnabled(false);
    }

    @OnClick(R.id.movement_transaction_date)
    public void onSelectTransactionDate() {
        Diexpenses.checkDialog(getFragmentManager(), "DatePickerDialogFragment");
        DialogFragment datePickerDialog = DatePickerDialogFragment.newInstance(MovementDetailsFragment.this, "Select transaction date");
        datePickerDialog.show(getFragmentManager(), "DatePickerDialogFragment");
    }

    @OnItemSelected(R.id.movement_kind)
    public void onKindSelected() {

        progressBar.setVisibility(View.VISIBLE);

        Kind kind = (Kind) spinnerKind.getSelectedItem();
        if (kindIdSelected != kind.getId()) {
            kindIdSelected = kind.getId();
            loadSubkinds(kindIdSelected);
        }
    }

    private void loadSubkinds(long kindId) {
        String methodName = "loadSubkinds - ";
        Log.d(TAG, methodName + "start. KindId=" + kindId);

        Call<List<Subkind>> subkindsCall = Requester.getInstance().getExpensesApi().getSubkinds(kindId);
        subkindsCall.enqueue(getSubkindsCallback());

        Log.d(TAG, methodName + "end");
    }

    public Callback<List<Subkind>> getSubkindsCallback() {
        return new Callback<List<Subkind>>() {
            @Override
            public void onResponse(Call<List<Subkind>> call, Response<List<Subkind>> response) {
                String methodName = "onResponse - ";
                Log.d(TAG, methodName + "start");

                subkindsLoaded = true;
                if(bankAccountsLoaded) {
                    progressBar.setVisibility(View.GONE);
                }

                List<Subkind> lstSubkinds = response.body();
                Collections.sort(lstSubkinds, new KindBaseComparator());
                KindBaseSpinAdapter<Subkind> subkindAdapter = new KindBaseSpinAdapter<Subkind>(getActivity(), android.R.layout.simple_spinner_item, lstSubkinds);
                spinnerSubkind.setAdapter(subkindAdapter);
                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<List<Subkind>> call, Throwable t) {
                Log.d(TAG, "Error getting subkinds", t);
            }
        };
    }

    private void loadBankAccounts() {
        Call<List<BankAccount>> bankAccountsCall = Requester.getInstance().getExpensesApi().getBankAccounts(userId);
        bankAccountsCall.enqueue(getBankAccountsCallback());
    }

    public Callback<List<BankAccount>> getBankAccountsCallback() {
        return new Callback<List<BankAccount>>() {
            @Override
            public void onResponse(Call<List<BankAccount>> call, Response<List<BankAccount>> response) {
                String methodName = "onResponse - ";
                Log.d(TAG, methodName + "start");

                bankAccountsLoaded = true;
                if(subkindsLoaded) {
                    progressBar.setVisibility(View.GONE);
                }

                List<BankAccount> lstBankAccounts = response.body();
                Collections.sort(lstBankAccounts, new BankAccountComparator());
                BankAccountSpinAdapter bankAccountAdapter = new BankAccountSpinAdapter(getActivity(), android.R.layout.simple_spinner_item, lstBankAccounts);
                spinnerBankAccount.setAdapter(bankAccountAdapter);
                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<List<BankAccount>> call, Throwable t) {
                Log.d(TAG, "Error getting bank accounts", t);
            }
        };
    }

    private Movement formToMovement() throws ParseException {
        Movement movement = new Movement();
        movement.setExpense(etToggleExpense.isChecked());
        movement.setConcept(tilConcept.getEditText().getText().toString());
        movement.setAmount(new BigDecimal(tilAmount.getEditText().getText().toString()));
        movement.setKind((Kind) spinnerKind.getSelectedItem());
        movement.setSubkind((Subkind) spinnerSubkind.getSelectedItem());
        movement.setBankAccount((BankAccount) spinnerBankAccount.getSelectedItem());
        movement.setTransactionDate(Diexpenses.parseDate(tvTransactionDate.getText().toString()));

        return movement;
    }

    private boolean checkForm() {
        String methodName = "checkForm - ";
        Log.d(TAG, methodName + "start");

        boolean isValidForm = true;

        isValidForm = checkRequiredField(tilConcept, isValidForm);
        isValidForm = checkRequiredField(tilAmount, isValidForm);

        Log.d(TAG, methodName + "end. isValidForm=" + isValidForm);
        return isValidForm;
    }

    private boolean checkRequiredField(TextInputLayout etField, boolean isValidForm) {
        if(etField.getEditText().getText().toString().isEmpty()) {
            etField.setError(getString(R.string.common_field_required));
            etField.setErrorEnabled(true);
            return false;
        }
        return isValidForm;
    }


    private void createMovement(Movement movement) {
        String methodName = "createMovement - ";
        Log.d(TAG, methodName + "start. Movement=" + movement.toString());

        Call<GenericResponse> callCreateMovement = Requester.getInstance().getExpensesApi().createMovement(userId, movement);
        callCreateMovement.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                String methodName = "onResponse(CreateMovement) - ";
                Log.d(TAG, methodName + "start");

                resetCommonComponentes();

                boolean isValidResponse = Requester.processResponse(response, MovementDetailsFragment.this);
                if(!isValidResponse) {
                    return;
                }

                GenericResponse genericResponse = response.body();
                Log.d(TAG, "Response=" + genericResponse.toString());
                if (genericResponse != null && genericResponse.getCode() == 127) {
                    getFragmentManager().popBackStack();
                } else {
                    Snackbar.make(getView(), R.string.movement_unknow_error_creating, Snackbar.LENGTH_LONG).show();
                }
                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                resetCommonComponentes();
                Snackbar.make(getView(), R.string.movement_error_creating, Snackbar.LENGTH_LONG).show();
            }
        });
        Log.d(TAG, methodName + "end");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        String methodName = "onDateSet(CreateMovement) - ";
        Log.d(TAG, methodName + "start. Year=" + year + " MonthOfYear=" + monthOfYear + " DayOfMonth=" + dayOfMonth);

        Calendar cSelected = Calendar.getInstance();
        cSelected.set(year, monthOfYear, dayOfMonth);

        tvTransactionDate.setText(Diexpenses.formatDate(cSelected.getTime()));

        Log.d(TAG, methodName + "end");
    }

    private void resetCommonComponentes() {
        progressBar.setVisibility(View.GONE);
        btnAction.setEnabled(true);
    }
}
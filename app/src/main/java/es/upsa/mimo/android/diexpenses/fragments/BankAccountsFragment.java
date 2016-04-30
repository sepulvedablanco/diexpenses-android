package es.upsa.mimo.android.diexpenses.fragments;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.upsa.mimo.android.diexpenses.R;
import es.upsa.mimo.android.diexpenses.adapters.BankAccountsAdapter;
import es.upsa.mimo.android.diexpenses.api.Requester;
import es.upsa.mimo.android.diexpenses.comparators.BankAccountComparator;
import es.upsa.mimo.android.diexpenses.components.DividerItemDecoration;
import es.upsa.mimo.android.diexpenses.dialogs.NoticeDialogFragment;
import es.upsa.mimo.android.diexpenses.dialogs.YesNoDialogFragment;
import es.upsa.mimo.android.diexpenses.events.BankAccountDelete;
import es.upsa.mimo.android.diexpenses.events.CurrencyChanged;
import es.upsa.mimo.android.diexpenses.listerners.ClickListener;
import es.upsa.mimo.android.diexpenses.listerners.RecyclerTouchListener;
import es.upsa.mimo.android.diexpenses.models.BankAccount;
import es.upsa.mimo.android.diexpenses.models.GenericResponse;
import es.upsa.mimo.android.diexpenses.utils.Constants;
import es.upsa.mimo.android.diexpenses.utils.Diexpenses;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Diego on 19/4/16.
 */
public class BankAccountsFragment extends Fragment implements
        NoticeDialogFragment.ActionDialogListener, YesNoDialogFragment.YesNoDialogListener {

    private static final String TAG = BankAccountsFragment.class.getSimpleName();

    private Long userId;

    @Bind(R.id.generic_add_new_tv) TextView etNewBankAccount;
    @Bind(R.id.generic_swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.generic_recycler_view) RecyclerView mRvBankAccounts;
    @Bind(R.id.generic_progress_bar) ProgressBar mProgressBar;

    private List<BankAccount> lstBankAccounts;
    private BankAccount bankAccountSelected;

    public static BankAccountsFragment newInstance(Long userId) {
        BankAccountsFragment bankAccountsFragment = new BankAccountsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.Bundles.USER_ID, userId);
        bankAccountsFragment.setArguments(bundle);
        return bankAccountsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_generic_list, container, false);
        userId = getArguments().getLong(Constants.Bundles.USER_ID);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getView() != null){

            EventBus.getDefault().register(this);

            ButterKnife.bind(this, getView());

            etNewBankAccount.setText(R.string.bank_accounts_new);

            setupRecyclerView(getView());

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadBankAccounts();
                }
            });

            mProgressBar.setVisibility(View.VISIBLE);

            loadBankAccounts();
        }

    }

    @Subscribe
    public void onEvent(CurrencyChanged event) {
        mRvBankAccounts.getAdapter().notifyDataSetChanged();
    }

    private void loadBankAccounts() {
        Call<List<BankAccount>> bankAccountsCall = Requester.getInstance().getExpensesApi().getBankAccounts(userId);
        bankAccountsCall.enqueue(getBankAccountsCallback());
    }

    public void setupRecyclerView(View view) {
        String methodName = "setupRecyclerView - ";
        Log.d(TAG, methodName + "start");

        mRvBankAccounts.setHasFixedSize(true);
        mRvBankAccounts.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRvBankAccounts.setItemAnimator(new DefaultItemAnimator());
        mRvBankAccounts.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRvBankAccounts.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mRvBankAccounts, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.d(TAG, "onClick. Row= " + position);
            }

            @Override
            public void onLongClick(View view, int position) {
                Log.d(TAG, "onLongClick. Row= " + position);
                bankAccountSelected = lstBankAccounts.get(position);

                Diexpenses.checkDialog(getFragmentManager(), "BankAccountDialogFragment");

                DialogFragment dialog = NoticeDialogFragment.newInstance(BankAccountsFragment.this, getString(R.string.bank_accounts_menu_title, bankAccountSelected.getDescription()));
                dialog.show(getFragmentManager(), "BankAccountDialogFragment");
            }
        }));

        mRvBankAccounts.setAdapter(new BankAccountsAdapter(getActivity()));

        Log.d(TAG, methodName + "end");
    }

    public Callback<List<BankAccount>> getBankAccountsCallback() {
        return new Callback<List<BankAccount>>() {
            @Override
            public void onResponse(Call<List<BankAccount>> call, Response<List<BankAccount>> response) {
                String methodName = "onResponse - ";
                Log.d(TAG, methodName + "start");

                mProgressBar.setVisibility(View.GONE);

                lstBankAccounts = response.body();
                Collections.sort(lstBankAccounts, new BankAccountComparator());
                ((BankAccountsAdapter) mRvBankAccounts.getAdapter()).setBankAccounts(lstBankAccounts);
                mSwipeRefreshLayout.setRefreshing(false);

                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<List<BankAccount>> call, Throwable t) {
                Log.d(TAG, "Error getting bank accounts", t);
            }
        };
    }

    @OnClick(R.id.generic_add_new_tv)
    public void onNewBankAccount(View view) {
        Fragment bankAccountDetailsFragment = BankAccountDetailsFragment.newInstance(userId);
        getFragmentManager().beginTransaction().replace(R.id.content_main, bankAccountDetailsFragment).addToBackStack(null).commit();
    }

    @Override
    public void onEditClick(DialogFragment dialog) {
        String methodName = "onEditClick - ";
        Log.d(TAG, methodName + "start");
        dialog.dismiss();

        Fragment bankAccountDetailsFragment = BankAccountDetailsFragment.newInstance(userId, bankAccountSelected);
        getFragmentManager().beginTransaction().replace(R.id.content_main, bankAccountDetailsFragment).addToBackStack(null).commit();

        Log.d(TAG, methodName + "end");
    }

    @Override
    public void onDeleteClick(DialogFragment dialog) {
        String methodName = "onDeleteClick - ";
        Log.d(TAG, methodName + "start");
        dialog.dismiss();

        Diexpenses.checkDialog(getFragmentManager(), "YesNoDialogFragment");

        DialogFragment yesNoDialog = YesNoDialogFragment.newInstance(BankAccountsFragment.this, getString(R.string.bank_accounts_remove_title),
                getString(R.string.bank_accounts_remove_message, bankAccountSelected.getDescription()));
        yesNoDialog.show(getFragmentManager(), "YesNoDialogFragment");

        Log.d(TAG, methodName + "end");
    }

    @Override
    public void onYesClicked(DialogFragment dialog) {
        String methodName = "onYesClicked - ";
        Log.d(TAG, methodName + "start");

        deleteBankAccount(bankAccountSelected.getId());

        Log.d(TAG, methodName + "end");
    }

    @Override
    public void onNoClicked(DialogFragment dialog) {
        String methodName = "onNoClicked - ";
        Log.d(TAG, methodName + "start");

        // Nothing to do here!

        Log.d(TAG, methodName + "end");
    }

    private void deleteBankAccount(Long bankAccountId) {
        String methodName = "deleteBankAccount - ";
        Log.d(TAG, methodName + "start. bank account id=" + bankAccountId);

        Call<GenericResponse> callDeleteBankAccount = Requester.getInstance().getExpensesApi().deleteBankAccount(userId, bankAccountId);
        callDeleteBankAccount.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                String methodName = "onResponse(DeleteBankAccount) - ";
                Log.d(TAG, methodName + "start");

                GenericResponse genericResponse = response.body();
                Log.d(TAG, "Response=" + genericResponse.toString());
                if (genericResponse != null && genericResponse.getCode() == 44) {
                    EventBus.getDefault().post(new BankAccountDelete());
                    loadBankAccounts();
                } else {
                    Snackbar.make(getView(), R.string.bank_accounts_unknow_error_deleting, Snackbar.LENGTH_LONG).show();
                }
                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Snackbar.make(getView(), R.string.bank_accounts_error_deleting, Snackbar.LENGTH_LONG).show();
            }
        });
        Log.d(TAG, methodName + "end");
    }
}

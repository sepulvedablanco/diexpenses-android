package es.upsa.mimo.android.diexpenses.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import es.upsa.mimo.android.diexpenses.models.BankAccount;
import es.upsa.mimo.android.diexpenses.models.Kind;
import es.upsa.mimo.android.diexpenses.models.Subkind;
import es.upsa.mimo.android.diexpenses.utils.Constants;
import es.upsa.mimo.android.diexpenses.utils.Diexpenses;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Diego on 2/5/16.
 */
public class StatisticsFragment extends Fragment {

    private static final String TAG = StatisticsFragment.class.getSimpleName();

    private Long userId;
    private Long kindIdSelected;

    private boolean kindsLoaded = false;
    private boolean bankAccountsLoaded = false;
    private boolean isChartDataLoaded = false;
    private Double incomes = 0D;
    private Double expenses = 0D;

    @Bind(R.id.statistics_year_spinner) Spinner spinnerYear;
    @Bind(R.id.statistics_month_spinner) Spinner spinnerMonth;
    @Bind(R.id.statistics_kind_spinner) Spinner spinnerKind;
    @Bind(R.id.statistics_subkind_spinner) Spinner spinnerSubkind;
    @Bind(R.id.statistics_bank_account_spinner) Spinner spinnerBankAccount;
    @Bind(R.id.statistics_action) Button btnAction;
    @Bind(R.id.statistics_chart) PieChart pieChart;
    @Bind(R.id.statistics_progress_bar) ProgressBar progressBar;
    @Bind(R.id.statistics_progress_bar_message) TextView progressBarMessage;

    public static StatisticsFragment newInstance(Long userId) {
        StatisticsFragment statisticsFragment = new StatisticsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.Bundles.USER_ID, userId);
        statisticsFragment.setArguments(bundle);
        return statisticsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        userId = getArguments().getLong(Constants.Bundles.USER_ID);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getView() != null){

            ButterKnife.bind(this, getView());

            showMask(View.VISIBLE);

            configChart();

            setupYearAndMonthSelector();

            setDefaultKindAdapter();
            setDefaultSubkindAdapter();
            setDefaultBankAccountAdapter();

            loadKinds();
            loadBankAccounts();
        }

    }

    private void setDefaultKindAdapter() {
        KindBaseSpinAdapter<Kind> kindAdapter = new KindBaseSpinAdapter<Kind>(getActivity(),
                android.R.layout.simple_spinner_item, Arrays.asList(new Kind(getString(R.string.statistics_all_kinds))));
        spinnerKind.setAdapter(kindAdapter);
    }

    private void setDefaultSubkindAdapter() {
        KindBaseSpinAdapter<Subkind> subkindAdapter = new KindBaseSpinAdapter<Subkind>(getActivity(),
                android.R.layout.simple_spinner_item, Arrays.asList(new Subkind(getString(R.string.statistics_select_kind))));
        spinnerSubkind.setAdapter(subkindAdapter);
    }

    private void setDefaultBankAccountAdapter() {
        BankAccountSpinAdapter bankAccountAdapter = new BankAccountSpinAdapter(getActivity(),
                android.R.layout.simple_spinner_item, Arrays.asList(new BankAccount(getString(R.string.statistics_all_bank_accounts))));
        spinnerBankAccount.setAdapter(bankAccountAdapter);
    }

    private void showMask(int visibility) {
        progressBar.setVisibility(visibility);
        progressBarMessage.setVisibility(visibility);
    }

    public void setupYearAndMonthSelector() {
        String methodName = "setupYearAndMonthSelector - ";
        Log.d(TAG, methodName + "start");

        List<String> lstYears = Diexpenses.getYears();
        lstYears.add(0, getString(R.string.statistics_historical));
        List<String> lstMonths = Diexpenses.capitalize(Diexpenses.getMonths());
        lstMonths.add(0, getString(R.string.statistics_every_month));

        ArrayAdapter<String> adapterYears = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, lstYears);
        adapterYears.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spinnerYear.setAdapter(adapterYears);

        ArrayAdapter<String> adapterMonths = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, lstMonths);
        adapterMonths.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spinnerMonth.setAdapter(adapterMonths);
        Log.d(TAG, methodName + "end");
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

                kindsLoaded = true;
                if(bankAccountsLoaded) {
                    showMask(View.GONE);
                }

                List<Kind> lstKinds = response.body();
                Collections.sort(lstKinds, new KindBaseComparator());
                lstKinds.add(0, new Kind(getString(R.string.statistics_all_kinds)));
                KindBaseSpinAdapter<Kind> kindAdapter = new KindBaseSpinAdapter<Kind>(getActivity(),
                        android.R.layout.simple_spinner_item, lstKinds);
                spinnerKind.setAdapter(kindAdapter);

                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<List<Kind>> call, Throwable t) {
                Log.d(TAG, "Error getting kinds", t);
            }
        };
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

                List<Subkind> lstSubkinds = response.body();
                Collections.sort(lstSubkinds, new KindBaseComparator());
                lstSubkinds.add(0, new Subkind(getString(R.string.statistics_all_subkinds)));
                KindBaseSpinAdapter<Subkind> subkindAdapter = new KindBaseSpinAdapter<Subkind>(getActivity(),
                        android.R.layout.simple_spinner_item, lstSubkinds);
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
                if(kindsLoaded) {
                    showMask(View.GONE);
                }

                List<BankAccount> lstBankAccounts = response.body();
                Collections.sort(lstBankAccounts, new BankAccountComparator());
                lstBankAccounts.add(0, new BankAccount(getString(R.string.statistics_all_bank_accounts)));
                BankAccountSpinAdapter bankAccountAdapter = new BankAccountSpinAdapter(getActivity(),
                        android.R.layout.simple_spinner_item, lstBankAccounts);
                spinnerBankAccount.setAdapter(bankAccountAdapter);
                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<List<BankAccount>> call, Throwable t) {
                Log.d(TAG, "Error getting bank accounts", t);
            }
        };
    }

    private void configChart() {
        pieChart.setDescription(null);
        pieChart.getLegend().setEnabled(false);
        pieChart.setNoDataText("");
    }

    @OnClick(R.id.statistics_action)
    public void onSeeStatisticsClick() {
        showMask(View.VISIBLE);

        resetStatistics();

        Integer year = null;
        if(spinnerYear.getSelectedItemPosition() != 0) {
            year = Integer.parseInt(spinnerYear.getSelectedItem().toString());
        }
        Integer month = null;
        if(spinnerMonth.getSelectedItemPosition() != 0) {
            year = spinnerMonth.getSelectedItemPosition();
        }
        Kind kind = (Kind) spinnerKind.getSelectedItem();
        Subkind subkind = (Subkind) spinnerSubkind.getSelectedItem();
        BankAccount bankAccount = (BankAccount) spinnerBankAccount.getSelectedItem();

        loadStatisticsIncomes(year, month, kind.getId(), subkind.getId(), bankAccount.getId());
        loadStatisticsExpenses(year, month, kind.getId(), subkind.getId(), bankAccount.getId());
    }

    private void resetStatistics() {
        incomes = 0D;
        expenses = 0D;
    }

    public void loadStatisticsIncomes(Integer year, Integer month, Long kindId, Long subkindId, Long bankAccountId) {
        Call<ResponseBody> incomesCall = Requester.getInstance().getExpensesApi().getStatistics(userId, false, year, month, kindId, subkindId, bankAccountId);
        incomesCall.enqueue(getIncomesStatisticsCallback());
    }

    public void loadStatisticsExpenses(Integer year, Integer month, Long kindId, Long subkindId, Long bankAccountId) {
        Call<ResponseBody> expensesCall = Requester.getInstance().getExpensesApi().getStatistics(userId, true, year, month, kindId, subkindId, bankAccountId);
        expensesCall.enqueue(getExpensesStatisticsCallback());
    }

    public Callback<ResponseBody> getIncomesStatisticsCallback() {
        return new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    incomes = Diexpenses.parseNumber(response.body().string());
                    Log.d(TAG, "Incomes=" + incomes);

                    checkRequest();

                } catch (Exception e) {
                    Log.e(TAG, "Error parsing statistics incomes:", e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Error getting statistics incomes", t);
            }
        };
    }

    public Callback<ResponseBody> getExpensesStatisticsCallback() {
        return new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    expenses = Diexpenses.parseNumber(response.body().string());
                    Log.d(TAG, "Expenses=" + expenses);

                    checkRequest();

                } catch (Exception e) {
                    Log.e(TAG, "Error parsing statistics expenses:", e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Error getting statistics expenses", t);
            }
        };
    }

    private void checkRequest() {
        if (!isChartDataLoaded) {
            isChartDataLoaded = true;
            return;
        }

        showMask(View.GONE);

        if(incomes == 0 && expenses == 0) {
            setChartNoDataMessage();
        } else {
            drawChart();
        }
    }

    private void setChartNoDataMessage() {
        pieChart.setNoDataText(getString(R.string.statistics_no_data));
        pieChart.invalidate();
    }

    private void drawChart() {
        final String methodName = "drawChart - ";
        Log.d(TAG, methodName + "start");

        List<Entry> chartValues = new ArrayList<Entry>();
        List<String> chartNames = new ArrayList<String>();
        PieDataSet dataSet = new PieDataSet(chartValues, "");

        chartValues.add(new Entry(expenses.floatValue(), 0));
        chartNames.add("Expenses");

        chartValues.add(new Entry(incomes.floatValue(), 1));
        chartNames.add("Incomes");

        dataSet.setColors(new int[]{ContextCompat.getColor(getActivity(), R.color.diexpensesRed),
                ContextCompat.getColor(getActivity(), R.color.diexpensesGreen)});

        PieData data = new PieData(chartNames, dataSet);
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        pieChart.setData(data);
        pieChart.invalidate();

        Log.d(TAG, methodName + "end");
    }

    @OnItemSelected(R.id.statistics_kind_spinner)
    public void onKindSelected() {
        Kind kind = (Kind) spinnerKind.getSelectedItem();
        if (kindIdSelected != kind.getId()) {
            kindIdSelected = kind.getId();
            if(kindIdSelected == null) {
                setDefaultSubkindAdapter();
            } else {
                loadSubkinds(kindIdSelected);
            }
        }
    }
}

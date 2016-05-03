package es.upsa.mimo.android.diexpenses.fragments;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.upsa.mimo.android.diexpenses.R;
import es.upsa.mimo.android.diexpenses.api.Requester;
import es.upsa.mimo.android.diexpenses.events.CurrencyChanged;
import es.upsa.mimo.android.diexpenses.models.User;
import es.upsa.mimo.android.diexpenses.utils.Constants;
import es.upsa.mimo.android.diexpenses.utils.Diexpenses;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Diego on 10/4/16.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();

    private boolean isDataLoaded;

    private int numRequestFinished = 0;

    @Bind(R.id.home_greetings) TextView tvGreetings;
    @Bind(R.id.home_total_amount) TextView tvTotalAmount;
    @Bind(R.id.home_month_expenses) TextView tvMonthExpenses;
    @Bind(R.id.home_month_incomes) TextView tvMonthIncomes;
    @Bind(R.id.home_chart) PieChart chart;
    @Bind(R.id.home_balance) TextView tvBalance;
    @Bind(R.id.home_progress_bar) ProgressBar progressBar;

    private User user;
    private Double expenses;
    private Double incomes;

    private int currentMonth;
    private int currentYear;

    public static HomeFragment newInstance(User user) {
        HomeFragment homeFragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.Parcelables.USER, user);
        homeFragment.setArguments(bundle);
        return homeFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        user = getArguments().getParcelable(Constants.Parcelables.USER);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getView() != null){

            if(!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }

            ButterKnife.bind(this, getView());

            initializeLabels();

            configChart();

            Requester.setAuthToken(user.getAuthToken());

            progressBar.setVisibility(View.VISIBLE);

            Calendar c = Calendar.getInstance();
            currentMonth = c.get(Calendar.MONTH) + 1;
            currentYear = c.get(Calendar.YEAR);

            loadExpenses();
            loadIncomes();
            loadTotalAmount();

            checkAndShowRateAppAlert();
        }
    }

    private void initializeLabels() {
        tvGreetings.setText(getString(R.string.home_greeting, user.getName()));
        String zero = Diexpenses.formatAmount(BigDecimal.ZERO);
        tvTotalAmount.setText(getString(R.string.home_total_amount, zero, Diexpenses.getCurrency(getActivity())));
        tvMonthIncomes.setText(getString(R.string.home_month_incomes, zero, Diexpenses.getCurrency(getActivity())));
        tvMonthExpenses.setText(getString(R.string.home_month_expenses, zero, Diexpenses.getCurrency(getActivity())));
    }

    @OnClick(R.id.floating_action_button)
    public void onFloatingActionButtonClick(View view) {
        Fragment movementDetailsFragment = MovementDetailsFragment.newInstance(user.getId());
        getFragmentManager().beginTransaction().replace(R.id.content_main, movementDetailsFragment).addToBackStack(null).commit();
    }

//    @Subscribe
//    public void onEvent(BankAccountCreateOrUpdate event) {
//        loadTotalAmount();
//    }
//
//    @Subscribe
//    public void onEvent(BankAccountDelete event) {
//        numRequestFinished = 0;
//        loadExpenses();
//        loadIncomes();
//        loadTotalAmount();
//    }

    @Subscribe
    public void onEvent(CurrencyChanged event) {
        String currency = event.getCurrency();

        String expensesText = tvMonthExpenses.getText().toString();
        String incomesText = tvMonthIncomes.getText().toString();
        String totalAmountText = tvTotalAmount.getText().toString();

        tvMonthExpenses.setText(expensesText.substring(0, expensesText.length() - 1) + currency);
        tvMonthIncomes.setText(incomesText.substring(0, incomesText.length() - 1) + currency);
        tvTotalAmount.setText(totalAmountText.substring(0, totalAmountText.length() - 1) + currency);
    }

    private void configChart() {
        chart.setDescription(null);
        chart.getLegend().setEnabled(false);
        chart.setNoDataText("");
    }

    private void loadExpenses() {
        Call<ResponseBody> expensesCall = Requester.getInstance().getExpensesApi().getAmount(user.getId(), true, currentMonth, currentYear);
        expensesCall.enqueue(getExpensesCallback());
    }

    private void loadIncomes() {
        Call<ResponseBody> incomesCall = Requester.getInstance().getExpensesApi().getAmount(user.getId(), false, currentMonth, currentYear);
        incomesCall.enqueue(getIncomesCallback());
    }

    private void loadTotalAmount() {
        Call<ResponseBody> totalAmount = Requester.getInstance().getExpensesApi().getTotalAmount(user.getId());
        totalAmount.enqueue(getTotalAmountCallback());
    }


    private void addRequestFinishAndCheckLoadingMask() {
        if (++numRequestFinished == 3) {
            progressBar.setVisibility(View.GONE);
        }
    }

    public Callback<ResponseBody> getExpensesCallback() {
        return new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    addRequestFinishAndCheckLoadingMask();
                    String strExpenses = response.body().string();
                    expenses = Diexpenses.parseNumber(strExpenses);
                    Log.d(TAG, "Expenses=" + expenses);
                    tvMonthExpenses.setText(getString(R.string.home_month_expenses, strExpenses, Diexpenses.getCurrency(getActivity())));
                    checkRequest();
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing expenses:", e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Error getting expenses", t);
            }
        };
    }

    public Callback<ResponseBody> getIncomesCallback() {
        return new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    addRequestFinishAndCheckLoadingMask();
                    String strIncomes = response.body().string();
                    incomes = Diexpenses.parseNumber(strIncomes);
                    Log.d(TAG, "Incomes=" + incomes);
                    tvMonthIncomes.setText(getString(R.string.home_month_incomes, strIncomes, Diexpenses.getCurrency(getActivity())));
                    checkRequest();
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing incomes:", e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Error getting incomes", t);
            }
        };
    }

    public Callback<ResponseBody> getTotalAmountCallback() {
        return new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    addRequestFinishAndCheckLoadingMask();
                    String strTotalAmount = response.body().string();
                    Log.d(TAG, "Total amount = " + strTotalAmount);
                    tvTotalAmount.setText(getString(R.string.home_total_amount, strTotalAmount, Diexpenses.getCurrency(getActivity())));
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing total amount:", e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Error getting total amount", t);
            }
        };
    }

    private void checkRequest() {
        if (!isDataLoaded) {
            isDataLoaded = true;
            return;
        }

        if(incomes == 0 && expenses == 0) {
            setChartNoDataMessage();
        } else {
            drawChart();
            doBalance();
        }
    }

    private void setChartNoDataMessage() {
        chart.setNoDataText(getString(R.string.home_no_data));
        chart.invalidate();
    }

    private void drawChart() {
        final String methodName = "drawChart - ";
        Log.d(TAG, methodName + "start");

        List<Entry> chartValues = new ArrayList<Entry>();
        List<String> chartNames = new ArrayList<String>();
        PieDataSet dataSet = new PieDataSet(chartValues, "");

        chartValues.add(new Entry(expenses.floatValue(), 0));
        chartNames.add(getString(R.string.home_expenses));

        chartValues.add(new Entry(incomes.floatValue(), 1));
        chartNames.add(getString(R.string.home_incomes));

        dataSet.setColors(new int[]{ContextCompat.getColor(getActivity(), R.color.diexpensesRed),
                ContextCompat.getColor(getActivity(), R.color.diexpensesGreen)});

        PieData data = new PieData(chartNames, dataSet);
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        chart.setData(data);
        chart.invalidate();

        Log.d(TAG, methodName + "end");
    }

    private void doBalance() {
        final String methodName = "doBalance - ";
        Log.d(TAG, methodName + "start");

        if (incomes > expenses) {
            tvBalance.setText(getString(R.string.home_balance_high));
        } else if (incomes == expenses) {
            tvBalance.setText(getString(R.string.home_balance_medium));
        } else {
            tvBalance.setText(getString(R.string.home_balance_low));
        }

        Log.d(TAG, methodName + "end");
    }

    private void checkAndShowRateAppAlert() {
        if (checkRateAppAlert()) {
            showRateAppAlert();
        }
    }

    private boolean checkRateAppAlert() {
        boolean hasRated = Diexpenses.hasRated(getActivity());
        if(hasRated) {
            return false;
        }
        int counterOfUses = Diexpenses.getAppUseCounter(getActivity());
        return counterOfUses % 10 == 0;
    }

    private void showRateAppAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(getString(R.string.rate_title));
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setMessage(getString(R.string.rate_message));

        alert.setPositiveButton(getString(R.string.rate_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.protocol_market, getActivity().getPackageName())));
                if (intent.resolveActivity(getActivity().getPackageManager()) == null) {
                    intent.setData(Uri.parse(getString(R.string.protocol_market_url, getActivity().getPackageName())));
                }
                startActivity(intent);
            }
        });

        alert.setNegativeButton(getString(R.string.common_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Do nothing
            }
        });
        alert.show();
    }

}
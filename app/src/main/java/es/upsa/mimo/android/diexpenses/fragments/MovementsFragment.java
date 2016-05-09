package es.upsa.mimo.android.diexpenses.fragments;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import es.upsa.mimo.android.diexpenses.R;
import es.upsa.mimo.android.diexpenses.adapters.MovementsAdapter;
import es.upsa.mimo.android.diexpenses.api.Requester;
import es.upsa.mimo.android.diexpenses.comparators.MovementsComparator;
import es.upsa.mimo.android.diexpenses.components.DividerItemDecoration;
import es.upsa.mimo.android.diexpenses.dialogs.NoticeDialogFragment;
import es.upsa.mimo.android.diexpenses.dialogs.YesNoDialogFragment;
import es.upsa.mimo.android.diexpenses.events.CurrencyChanged;
import es.upsa.mimo.android.diexpenses.listerners.ClickListener;
import es.upsa.mimo.android.diexpenses.listerners.RecyclerTouchListener;
import es.upsa.mimo.android.diexpenses.models.GenericResponse;
import es.upsa.mimo.android.diexpenses.models.Movement;
import es.upsa.mimo.android.diexpenses.models.MovementPage;
import es.upsa.mimo.android.diexpenses.utils.Constants;
import es.upsa.mimo.android.diexpenses.utils.Diexpenses;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Diego on 20/4/16.
 */
public class MovementsFragment extends Fragment implements NoticeDialogFragment.ActionDialogListener,
        YesNoDialogFragment.YesNoDialogListener {

    private static final String TAG = MovementsFragment.class.getSimpleName();

    private Long userId;

    @BindView(R.id.generic_add_new_tv) TextView etNewMovement;
    @BindView(R.id.generic_swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.generic_recycler_view) RecyclerView rvMovements;
    @BindView(R.id.generic_progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.generic_month_selector_labels_layout) PercentRelativeLayout prlLabels;
    @BindView(R.id.generic_month_selector_layout_spinners) PercentRelativeLayout prlSpinners;
    @BindView(R.id.generic_year_spinner) Spinner spinnerYear;
    @BindView(R.id.generic_month_spinner) Spinner spinnerMonth;

    private List<Movement> lstMovements;
    private Movement movementSelected;

    private int iYearSelected;
    private int iMonthSelected;

    public static MovementsFragment newInstance(Long userId) {
        MovementsFragment movementsFragment = new MovementsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.Bundles.USER_ID, userId);
        movementsFragment.setArguments(bundle);
        return movementsFragment;
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

        if(getView() != null) {

            if(!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }

            ButterKnife.bind(this, getView());

            etNewMovement.setText(R.string.movements_new);

            setupYearAndMonthSelector();

            setupRecyclerView();

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadMovements(iYearSelected, iMonthSelected);
                }
            });

            mProgressBar.setVisibility(View.VISIBLE);

            Bundle mySavedInstanceState = getArguments();
            int year = mySavedInstanceState.getInt(Constants.Arguments.YEAR, -1);
            int month = mySavedInstanceState.getInt(Constants.Arguments.MONTH, -1);

            if(year == -1 && month == -1) {
            Calendar calendarToday = Calendar.getInstance();
            iYearSelected = calendarToday.get(Calendar.YEAR);
            iMonthSelected = calendarToday.get(Calendar.MONTH) + 1;
            } else {
                iYearSelected = year;
                iMonthSelected = month;
            }
        }

        loadMovements(iYearSelected, iMonthSelected);
    }

    @Override
    public void onPause() {
        super.onPause();

        getArguments().putInt(Constants.Arguments.YEAR, iYearSelected);
        getArguments().putInt(Constants.Arguments.MONTH, iMonthSelected);
    }

    @Subscribe
    public void onEvent(CurrencyChanged event) {
        rvMovements.getAdapter().notifyDataSetChanged();
    }


    private void loadMovements(int year, int month) {
        String methodName = "loadMovements - ";
        Log.d(TAG, methodName + "start. Year=" + year + " Month=" + month);

        Call<MovementPage> movementsCall = Requester.getInstance().getExpensesApi().getMovements(userId, year, month);
        movementsCall.enqueue(getMovementsCallback());

        Log.d(TAG, methodName + "end");
    }

    public void setupYearAndMonthSelector() {
        String methodName = "setupYearAndMonthSelector - ";
        Log.d(TAG, methodName + "start");

        prlLabels.setVisibility(LinearLayout.VISIBLE);
        prlSpinners.setVisibility(LinearLayout.VISIBLE);

        Calendar cToday = Calendar.getInstance();

        List<String> lstYears = Diexpenses.getYears();
        List<String> lstMonths = Diexpenses.capitalize(Diexpenses.getMonths());

        ArrayAdapter<String> adapterYears = new ArrayAdapter<String>(getActivity(), R.layout.spinner_textview_align, lstYears);
        spinnerYear.setAdapter(adapterYears);
        spinnerYear.setSelection(0);

        ArrayAdapter<String> adapterMonths = new ArrayAdapter<String>(getActivity(), R.layout.spinner_textview_align, lstMonths);
        spinnerMonth.setAdapter(adapterMonths);
        spinnerMonth.setSelection(cToday.get(Calendar.MONTH));
        Log.d(TAG, methodName + "end");
    }

    public void setupRecyclerView() {
        String methodName = "setupRecyclerView - ";
        Log.d(TAG, methodName + "start");

        rvMovements.setHasFixedSize(true);
        rvMovements.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rvMovements.setItemAnimator(new DefaultItemAnimator());
        rvMovements.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        rvMovements.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), rvMovements, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.d(TAG, "onClick. Row= " + position);
            }

            @Override
            public void onLongClick(View view, int position) {
                Log.d(TAG, "onLongClick. Row= " + position);
                movementSelected = lstMovements.get(position);

                Diexpenses.checkDialog(getFragmentManager(), "MovementsDialogFragment");

                DialogFragment dialog = NoticeDialogFragment.newInstance(MovementsFragment.this,
                        getString(R.string.movements_menu_title, movementSelected.getConcept()), true);
                dialog.show(getFragmentManager(), "MovementsDialogFragment");
            }
        }));

        rvMovements.setAdapter(new MovementsAdapter(getActivity()));

        Log.d(TAG, methodName + "end");
    }

    @OnItemSelected(R.id.generic_year_spinner)
    public void onYearSpinnerSelected(AdapterView<?> parent, View view, int position, long id) {
        int year = Integer.parseInt(Diexpenses.getYears().get(position));
        if (year == iYearSelected) {
            Log.d(TAG, "Same year selected");
            return;
        }
        iYearSelected = year;
        mProgressBar.setVisibility(View.VISIBLE);
        loadMovements(iYearSelected, iMonthSelected);

    }

    @OnItemSelected(R.id.generic_month_spinner)
    public void onMonthSpinnerSelected(AdapterView<?> parent, View view, int position, long id) {
        int month = position + 1;
        if (iMonthSelected == month) {
            Log.d(TAG, "Same month selected");
            return;
        }
        iMonthSelected = month;
        mProgressBar.setVisibility(View.VISIBLE);
        loadMovements(iYearSelected, iMonthSelected);
    }

    public Callback<MovementPage> getMovementsCallback() {
        return new Callback<MovementPage>() {
            @Override
            public void onResponse(Call<MovementPage> call, Response<MovementPage> response) {
                String methodName = "onResponse - ";
                Log.d(TAG, methodName + "start");

                mProgressBar.setVisibility(View.GONE);

                lstMovements = response.body().getLstMovements();
                Collections.sort(lstMovements, new MovementsComparator());
                ((MovementsAdapter) rvMovements.getAdapter()).setMovements(lstMovements);
                mSwipeRefreshLayout.setRefreshing(false);

                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<MovementPage> call, Throwable t) {
                Log.d(TAG, "Error getting movements", t);
            }
        };
    }

    @OnClick(R.id.generic_add_new_tv)
    public void onNewMovement(View view) {
        Fragment movementDetailsFragment = MovementDetailsFragment.newInstance(userId);
        getFragmentManager().beginTransaction().replace(R.id.content_main, movementDetailsFragment).addToBackStack(null).commit();
    }

    @Override
    public void onEditClick(DialogFragment dialog) {
        String methodName = "onEditClick - ";
        Log.d(TAG, methodName + "start");

        Log.d(TAG, methodName + "end");

        throw new RuntimeException("Operation not supported by Expenses API.");
    }

    @Override
    public void onDeleteClick(DialogFragment dialog) {
        String methodName = "onDeleteClick - ";
        Log.d(TAG, methodName + "start");
        dialog.dismiss();

        Diexpenses.checkDialog(getFragmentManager(), "YesNoDialogFragment");

        DialogFragment yesNoDialog = YesNoDialogFragment.newInstance(MovementsFragment.this, getString(R.string.movements_remove_title),
                getString(R.string.movements_remove_message, movementSelected.getConcept()));
        yesNoDialog.show(getFragmentManager(), "YesNoDialogFragment");

        Log.d(TAG, methodName + "end");
    }

    @Override
    public void onYesClicked(DialogFragment dialog) {
        String methodName = "onYesClicked - ";
        Log.d(TAG, methodName + "start");

        mProgressBar.setVisibility(View.VISIBLE);
        deleteMovement(movementSelected.getId());

        Log.d(TAG, methodName + "end");
    }

    @Override
    public void onNoClicked(DialogFragment dialog) {
        String methodName = "onNoClicked - ";
        Log.d(TAG, methodName + "start");

        // Nothing to do here!

        Log.d(TAG, methodName + "end");
    }

    private void deleteMovement(Long movementId) {
        String methodName = "deleteMovement - ";
        Log.d(TAG, methodName + "start. movement id=" + movementId);

        Call<GenericResponse> callDeleteMovement = Requester.getInstance().getExpensesApi().deleteMovement(userId, movementId);
        callDeleteMovement.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                String methodName = "onResponse(DeleteMovement) - ";
                Log.d(TAG, methodName + "start");

                GenericResponse genericResponse = response.body();
                Log.d(TAG, "Response=" + genericResponse.toString());
                if (genericResponse != null && genericResponse.getCode() == 133) {
                    loadMovements(iYearSelected, iMonthSelected);
                } else {
                    Log.e(TAG, "Unknown error deleting movement.");
                }
                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Log.d(TAG, "Error deleting movement", t);
            }
        });
        Log.d(TAG, methodName + "end");
    }
}

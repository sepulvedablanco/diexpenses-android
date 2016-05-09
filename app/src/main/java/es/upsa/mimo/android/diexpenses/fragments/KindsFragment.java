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

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.upsa.mimo.android.diexpenses.comparators.KindBaseComparator;
import es.upsa.mimo.android.diexpenses.R;
import es.upsa.mimo.android.diexpenses.adapters.KindsBaseAdapter;
import es.upsa.mimo.android.diexpenses.api.Requester;
import es.upsa.mimo.android.diexpenses.components.DividerItemDecoration;
import es.upsa.mimo.android.diexpenses.dialogs.EditDialogFragment;
import es.upsa.mimo.android.diexpenses.dialogs.NoticeDialogFragment;
import es.upsa.mimo.android.diexpenses.dialogs.NoticeDialogFragment.ActionDialogListener;
import es.upsa.mimo.android.diexpenses.dialogs.YesNoDialogFragment;
import es.upsa.mimo.android.diexpenses.listerners.ClickListener;
import es.upsa.mimo.android.diexpenses.listerners.RecyclerTouchListener;
import es.upsa.mimo.android.diexpenses.models.GenericResponse;
import es.upsa.mimo.android.diexpenses.models.Kind;
import es.upsa.mimo.android.diexpenses.utils.Constants;
import es.upsa.mimo.android.diexpenses.utils.Diexpenses;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Diego on 14/4/16.
 */
public class KindsFragment extends Fragment implements ActionDialogListener, YesNoDialogFragment.YesNoDialogListener, EditDialogFragment.EditDialogListener {

    private static final String TAG = KindsFragment.class.getSimpleName();

    private Long userId;

    @BindView(R.id.generic_add_new_tv) TextView etNewKind;
    @BindView(R.id.generic_swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.generic_recycler_view) RecyclerView rvKinds;
    @BindView(R.id.generic_progress_bar) ProgressBar mProgressBar;

    private List<Kind> lstKinds;
    private Kind kindSelected;

    public static KindsFragment newInstance(Long userId) {
        KindsFragment kindsFragment = new KindsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.Bundles.USER_ID, userId);
        kindsFragment.setArguments(bundle);
        return kindsFragment;
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

            ButterKnife.bind(this, getView());

            etNewKind.setText(R.string.kinds_new);

            setupRecyclerView(getView());

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadKinds();
                }
            });

            mProgressBar.setVisibility(View.VISIBLE);

            loadKinds();
        }

    }

    private void loadKinds() {
        Call<List<Kind>> kindsCall = Requester.getInstance().getExpensesApi().getKinds(userId);
        kindsCall.enqueue(getKindsCallback());
    }

    public void setupRecyclerView(View view) {
        String methodName = "setupRecyclerView - ";
        Log.d(TAG, methodName + "start");

        rvKinds.setHasFixedSize(true);
        rvKinds.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rvKinds.setItemAnimator(new DefaultItemAnimator());
        rvKinds.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        rvKinds.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), rvKinds, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.d(TAG, "onClick. Row= " + position);
                showSubkinds(position);
            }

            @Override
            public void onLongClick(View view, int position) {
                Log.d(TAG, "onLongClick. Row= " + position);
                kindSelected = lstKinds.get(position);

                Diexpenses.checkDialog(getFragmentManager(), "KindsDialogFragment");

                DialogFragment dialog = NoticeDialogFragment.newInstance(KindsFragment.this, getString(R.string.kinds_menu_title, kindSelected.getDescription()));
                dialog.show(getFragmentManager(), "KindsDialogFragment");
            }
        }));

        rvKinds.setAdapter(new KindsBaseAdapter());

        Log.d(TAG, methodName + "end");
    }

    public Callback<List<Kind>> getKindsCallback() {
        return new Callback<List<Kind>>() {
            @Override
            public void onResponse(Call<List<Kind>> call, Response<List<Kind>> response) {
                String methodName = "onResponse - ";
                Log.d(TAG, methodName + "start");

                mProgressBar.setVisibility(View.GONE);

                lstKinds = response.body();
                Collections.sort(lstKinds, new KindBaseComparator());
                ((KindsBaseAdapter)rvKinds.getAdapter()).setKindsBase(lstKinds);
                mSwipeRefreshLayout.setRefreshing(false);

                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<List<Kind>> call, Throwable t) {
                Log.d(TAG, "Error getting kinds", t);
            }
        };
    }

    private void showSubkinds(int position) {
        String methodName = "showSubkinds - ";
        Log.d(TAG, methodName + "start. Position=" + position);

        Fragment subkindsFragment = SubkindsFragment.newInstance(lstKinds.get(position).getId());
        getFragmentManager().beginTransaction().replace(R.id.content_main, subkindsFragment).addToBackStack(null).commit();

        Log.d(TAG, methodName + "end");
    }

    @OnClick(R.id.generic_add_new_tv)
    public void onNewKind(View view) {
        Diexpenses.checkDialog(getFragmentManager(), "NewDialogFragment");

        DialogFragment dialog = EditDialogFragment.newInstance(KindsFragment.this, getString(R.string.kinds_new_title), getString(R.string.kinds_new_message));
        dialog.show(getFragmentManager(), "NewDialogFragment");
    }

    @Override
    public void onEditClick(DialogFragment dialog) {
        String methodName = "onEditClick - ";
        Log.d(TAG, methodName + "start");
        dialog.dismiss();

        Diexpenses.checkDialog(getFragmentManager(), "EditDialogFragment");

        DialogFragment editDialog = EditDialogFragment.newInstance(KindsFragment.this, getString(R.string.kinds_edit_title), getString(R.string.kinds_edit_message), kindSelected.getDescription());
        editDialog.show(getFragmentManager(), "EditDialogFragment");

        Log.d(TAG, methodName + "end");
    }

    @Override
    public void onDeleteClick(DialogFragment dialog) {
        String methodName = "onDeleteClick - ";
        Log.d(TAG, methodName + "start");
        dialog.dismiss();

        Diexpenses.checkDialog(getFragmentManager(), "YesNoDialogFragment");

        DialogFragment yesNoDialog = YesNoDialogFragment.newInstance(KindsFragment.this, getString(R.string.kinds_remove_title),
                getString(R.string.kinds_remove_message, kindSelected.getDescription()));
        yesNoDialog.show(getFragmentManager(), "YesNoDialogFragment");

        Log.d(TAG, methodName + "end");
    }

    @Override
    public void onYesClicked(DialogFragment dialog) {
        String methodName = "onYesClicked - ";
        Log.d(TAG, methodName + "start");

        mProgressBar.setVisibility(View.VISIBLE);

        deleteExpenseKind(kindSelected.getId());

        Log.d(TAG, methodName + "end");
    }

    @Override
    public void onNoClicked(DialogFragment dialog) {
        String methodName = "onNoClicked - ";
        Log.d(TAG, methodName + "start");

        // Nothing to do here!

        Log.d(TAG, methodName + "end");
    }

    @Override
    public void onAdd(DialogFragment dialog, String text) {
        String methodName = "onAdd - ";
        Log.d(TAG, methodName + "start. Text=" + text);

        mProgressBar.setVisibility(View.VISIBLE);

        createExpenseKind(text);

        Log.d(TAG, methodName + "end");

    }

    @Override
    public void onEdit(DialogFragment dialog, String text) {
        String methodName = "onEdit - ";
        Log.d(TAG, methodName + "start. Text=" + text);

        mProgressBar.setVisibility(View.VISIBLE);

        editExpenseKind(kindSelected.getId(), text);

        Log.d(TAG, methodName + "end");
    }

    @Override
    public void onCancel(DialogFragment dialog) {
        String methodName = "onCancel - ";
        Log.d(TAG, methodName + "start");

        // Nothing to do here!

        Log.d(TAG, methodName + "end");
    }

    private void createExpenseKind(String description) {
        String methodName = "createExpenseKind - ";
        Log.d(TAG, methodName + "start. Description=" + description);

        Kind newKind = new Kind(description);
        Call<GenericResponse> callCreateKind = Requester.getInstance().getExpensesApi().createKind(userId, newKind);
        callCreateKind.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                String methodName = "onResponse(CreateKind) - ";
                Log.d(TAG, methodName + "start");

                GenericResponse genericResponse = response.body();
                Log.d(TAG, "Response=" + genericResponse.toString());
                if (genericResponse != null && genericResponse.getCode() == 60) {
                    loadKinds();
                } else {
                    Log.e(TAG, "Unknown error creating expense kind.");
                }
                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Log.d(TAG, "Error creating expense kind", t);
            }
        });
        Log.d(TAG, methodName + "end");
    }

    private void editExpenseKind(Long kindId, String description) {
        String methodName = "editExpenseKind - ";
        Log.d(TAG, methodName + "start. id=" + kindId + ". Description=" + description);

        Kind newKind = new Kind(kindId, description);
        Call<GenericResponse> callEditKind = Requester.getInstance().getExpensesApi().editKind(userId, kindId, newKind);
        callEditKind.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                String methodName = "onResponse(EditKind) - ";
                Log.d(TAG, methodName + "start");

                GenericResponse genericResponse = response.body();
                Log.d(TAG, "Response=" + genericResponse.toString());
                if (genericResponse != null && genericResponse.getCode() == 66) {
                    loadKinds();
                } else {
                    Log.e(TAG, "Unknown error updating expense kind.");
                }
                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Log.d(TAG, "Error updating expense kind", t);
            }
        });
        Log.d(TAG, methodName + "end");
    }

    private void deleteExpenseKind(Long kindId) {
        String methodName = "deleteExpenseKind - ";
        Log.d(TAG, methodName + "start. kind id=" + kindId);

        Call<GenericResponse> callDeleteKind = Requester.getInstance().getExpensesApi().deleteKind(userId, kindId);
        callDeleteKind.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                String methodName = "onResponse(DeleteKind) - ";
                Log.d(TAG, methodName + "start");

                GenericResponse genericResponse = response.body();
                Log.d(TAG, "Response=" + genericResponse.toString());
                if (genericResponse != null && genericResponse.getCode() == 71) {
                    loadKinds();
                } else {
                    Snackbar.make(getView(), "Unknown error deleting expense kind.", Snackbar.LENGTH_LONG).show();
                }
                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Snackbar.make(getView(), "Error deleting expense kind.", Snackbar.LENGTH_LONG).show();
            }
        });
        Log.d(TAG, methodName + "end");
    }
}

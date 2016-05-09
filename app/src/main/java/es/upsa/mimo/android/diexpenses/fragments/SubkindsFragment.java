package es.upsa.mimo.android.diexpenses.fragments;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import es.upsa.mimo.android.diexpenses.R;
import es.upsa.mimo.android.diexpenses.adapters.KindsBaseAdapter;
import es.upsa.mimo.android.diexpenses.api.Requester;
import es.upsa.mimo.android.diexpenses.comparators.KindBaseComparator;
import es.upsa.mimo.android.diexpenses.components.DividerItemDecoration;
import es.upsa.mimo.android.diexpenses.dialogs.EditDialogFragment;
import es.upsa.mimo.android.diexpenses.dialogs.NoticeDialogFragment;
import es.upsa.mimo.android.diexpenses.dialogs.NoticeDialogFragment.ActionDialogListener;
import es.upsa.mimo.android.diexpenses.dialogs.YesNoDialogFragment;
import es.upsa.mimo.android.diexpenses.listerners.ClickListener;
import es.upsa.mimo.android.diexpenses.listerners.RecyclerTouchListener;
import es.upsa.mimo.android.diexpenses.models.GenericResponse;
import es.upsa.mimo.android.diexpenses.models.Subkind;
import es.upsa.mimo.android.diexpenses.utils.Constants;
import es.upsa.mimo.android.diexpenses.utils.Diexpenses;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Diego on 14/4/16.
 */
public class SubkindsFragment extends Fragment implements ActionDialogListener, YesNoDialogFragment.YesNoDialogListener, EditDialogFragment.EditDialogListener {

    private static final String TAG = SubkindsFragment.class.getSimpleName();

    private Long kindId;

    @BindView(R.id.generic_add_new_tv) TextView etNewSubkind;
    @BindView(R.id.generic_swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.generic_recycler_view) RecyclerView rvSubkinds;
    @BindView(R.id.generic_progress_bar) ProgressBar mProgressBar;

    private List<Subkind> lstSubkinds;
    private Subkind subkindSelected;

    public static SubkindsFragment newInstance(Long kindId) {
        SubkindsFragment subkindsFragment = new SubkindsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.Bundles.KIND_ID, kindId);
        subkindsFragment.setArguments(bundle);
        return subkindsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_generic_list, container, false);
        kindId = getArguments().getLong(Constants.Bundles.KIND_ID);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getView() != null){

            ButterKnife.bind(this, getView());

            etNewSubkind.setText(R.string.subkinds_new);

            setupRecyclerView(getView());

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadSubkinds();
                }
            });

            mProgressBar.setVisibility(View.VISIBLE);

            loadSubkinds();
        }

    }

    private void loadSubkinds() {
        Call<List<Subkind>> subkindsCall = Requester.getInstance().getExpensesApi().getSubkinds(kindId);
        subkindsCall.enqueue(getSubkindsCallback());
    }

    public void setupRecyclerView(View view) {
        String methodName = "setupRecyclerView - ";
        Log.d(TAG, methodName + "start");

        rvSubkinds.setHasFixedSize(true);
        rvSubkinds.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rvSubkinds.setItemAnimator(new DefaultItemAnimator());
        rvSubkinds.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        rvSubkinds.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), rvSubkinds, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.d(TAG, "onClick. Row= " + position);
            }

            @Override
            public void onLongClick(View view, int position) {
                Log.d(TAG, "onLongClick. Row= " + position);
                subkindSelected = lstSubkinds.get(position);

                Diexpenses.checkDialog(getFragmentManager(), "SubkindsDialogFragment");

                DialogFragment dialog = NoticeDialogFragment.newInstance(SubkindsFragment.this,
                        getString(R.string.subkinds_menu_title, subkindSelected.getDescription()));
                dialog.show(getFragmentManager(), "SubkindsDialogFragment");
            }
        }));

        rvSubkinds.setAdapter(new KindsBaseAdapter());

        Log.d(TAG, methodName + "end");
    }

    public Callback<List<Subkind>> getSubkindsCallback() {
        return new Callback<List<Subkind>>() {
            @Override
            public void onResponse(Call<List<Subkind>> call, Response<List<Subkind>> response) {
                String methodName = "onResponse - ";
                Log.d(TAG, methodName + "start");

                mProgressBar.setVisibility(View.GONE);

                lstSubkinds = response.body();
                Collections.sort(lstSubkinds, new KindBaseComparator());
                ((KindsBaseAdapter) rvSubkinds.getAdapter()).setKindsBase(lstSubkinds);
                mSwipeRefreshLayout.setRefreshing(false);

                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<List<Subkind>> call, Throwable t) {
                Log.d(TAG, "Error getting subkinds", t);
            }
        };
    }

    @OnClick(R.id.generic_add_new_tv)
    public void onNewSubkind(View view) {

        Diexpenses.checkDialog(getFragmentManager(), "NewDialogFragment");

        DialogFragment dialog = EditDialogFragment.newInstance(SubkindsFragment.this,
                getString(R.string.subkinds_new_title), getString(R.string.subkinds_new_message));
        dialog.show(getFragmentManager(), "NewDialogFragment");
    }

    @Override
    public void onEditClick(DialogFragment dialog) {
        String methodName = "onEditClick - ";
        Log.d(TAG, methodName + "start");
        dialog.dismiss();

        Diexpenses.checkDialog(getFragmentManager(), "EditDialogFragment");

        DialogFragment editDialog = EditDialogFragment.newInstance(SubkindsFragment.this, getString(R.string.subkinds_edit_title),
                getString(R.string.subkinds_edit_message), subkindSelected.getDescription());
        editDialog.show(getFragmentManager(), "EditDialogFragment");

        Log.d(TAG, methodName + "end");
    }

    @Override
    public void onDeleteClick(DialogFragment dialog) {
        String methodName = "onDeleteClick - ";
        Log.d(TAG, methodName + "start");
        dialog.dismiss();

        Diexpenses.checkDialog(getFragmentManager(), "YesNoDialogFragment");

        DialogFragment yesNoDialog = YesNoDialogFragment.newInstance(SubkindsFragment.this, getString(R.string.subkinds_remove_title),
                getString(R.string.subkinds_remove_message, subkindSelected.getDescription()));
        yesNoDialog.show(getFragmentManager(), "YesNoDialogFragment");

        Log.d(TAG, methodName + "end");
    }

    @Override
    public void onYesClicked(DialogFragment dialog) {
        String methodName = "onYesClicked - ";
        Log.d(TAG, methodName + "start");

        mProgressBar.setVisibility(View.VISIBLE);

        deleteExpenseSubkind(subkindSelected.getId());

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

        createExpenseSubkind(text);

        Log.d(TAG, methodName + "end");

    }

    @Override
    public void onEdit(DialogFragment dialog, String text) {
        String methodName = "onEdit - ";
        Log.d(TAG, methodName + "start. Text=" + text);

        mProgressBar.setVisibility(View.VISIBLE);

        editExpenseSubkind(subkindSelected.getId(), text);

        Log.d(TAG, methodName + "end");
    }

    @Override
    public void onCancel(DialogFragment dialog) {
        String methodName = "onCancel - ";
        Log.d(TAG, methodName + "start");

        // Nothing to do here!

        Log.d(TAG, methodName + "end");
    }

    private void createExpenseSubkind(String description) {
        String methodName = "createExpenseSubkind - ";
        Log.d(TAG, methodName + "start. Description=" + description);

        Subkind newSubkind = new Subkind(description);
        Call<GenericResponse> callCreateSubkind = Requester.getInstance().getExpensesApi().createSubkind(kindId, newSubkind);
        callCreateSubkind.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                String methodName = "onResponse(CreateSubkind) - ";
                Log.d(TAG, methodName + "start");

                GenericResponse genericResponse = response.body();
                Log.d(TAG, "Response=" + genericResponse.toString());
                if (genericResponse != null && genericResponse.getCode() == 90) {
                    loadSubkinds();
                } else {
                    Log.e(TAG, "Unknown error creating expense subkind.");
                }
                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Log.d(TAG, "Error creating expense subkind", t);
            }
        });
        Log.d(TAG, methodName + "end");
    }

    private void editExpenseSubkind(Long subkindId, String description) {
        String methodName = "editExpenseSubkind - ";
        Log.d(TAG, methodName + "start. id=" + subkindId + ". Description=" + description);

        Subkind newSubkind = new Subkind(subkindId, description);
        Call<GenericResponse> callEditSubkind = Requester.getInstance().getExpensesApi().editSubkind(kindId, subkindId, newSubkind);
        callEditSubkind.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                String methodName = "onResponse(EditSubkind) - ";
                Log.d(TAG, methodName + "start");

                GenericResponse genericResponse = response.body();
                Log.d(TAG, "Response=" + genericResponse.toString());
                if (genericResponse != null && genericResponse.getCode() == 99) {
                    loadSubkinds();
                } else {
                    Log.e(TAG, "Unknown error updating expense subkind.");
                }
                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Log.d(TAG, "Error updating expense subkind", t);
            }
        });
        Log.d(TAG, methodName + "end");
    }

    private void deleteExpenseSubkind(Long subkindId) {
        String methodName = "deleteExpenseSubkind - ";
        Log.d(TAG, methodName + "start. subkind id=" + subkindId);

        Call<GenericResponse> callDeleteSubkind = Requester.getInstance().getExpensesApi().deleteSubkind(kindId, subkindId);
        callDeleteSubkind.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                String methodName = "onResponse(DeleteSubkind) - ";
                Log.d(TAG, methodName + "start");

                GenericResponse genericResponse = response.body();
                Log.d(TAG, "Response=" + genericResponse.toString());
                if (genericResponse != null && genericResponse.getCode() == 104) {
                    loadSubkinds();
                } else {
                    Log.e(TAG, "Unknown error deleting expense subkind.");
                }
                Log.d(TAG, methodName + "end");
            }

            @Override
            public void onFailure(Call<GenericResponse> call, Throwable t) {
                Log.d(TAG, "Error deleting expense subkind", t);
            }
        });
        Log.d(TAG, methodName + "end");
    }
}

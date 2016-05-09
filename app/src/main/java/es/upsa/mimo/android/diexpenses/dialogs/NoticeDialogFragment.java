package es.upsa.mimo.android.diexpenses.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.upsa.mimo.android.diexpenses.R;

/**
 * Created by Diego on 16/4/16.
 */
public class NoticeDialogFragment extends DialogFragment {

    private static final String TAG = NoticeDialogFragment.class.getSimpleName();

    @BindView(R.id.dialog_edit) TextView tvEdit;
    @BindView(R.id.dialog_delete) TextView tvDelete;

    public interface ActionDialogListener {
        void onEditClick(DialogFragment dialog);
        void onDeleteClick(DialogFragment dialog);
    }

    private ActionDialogListener mListener;
    private String title;
    private boolean hideEdit;

    public static NoticeDialogFragment newInstance(ActionDialogListener mListener, String title, boolean hideEdit) {
        NoticeDialogFragment fragment = new NoticeDialogFragment();
        fragment.mListener = mListener;
        fragment.title = title;
        fragment.hideEdit = hideEdit;
        return fragment;
    }

        public static NoticeDialogFragment newInstance(ActionDialogListener mListener, String title) {
            return newInstance(mListener, title, false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_edit_delete_dialog, null);

        ButterKnife.bind(this, view);

        builder.setView(view);
//        builder.setMessage(title);
        builder.setTitle(title);
        if(hideEdit) {
            tvEdit.setVisibility(View.GONE);
        } else {
            tvEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "OnEdit");
                    mListener.onEditClick(NoticeDialogFragment.this);
                }
            });
        }
        tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "OnDelete");
                mListener.onDeleteClick(NoticeDialogFragment.this);
            }
        });
        return builder.create();
    }
}

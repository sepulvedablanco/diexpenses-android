package es.upsa.mimo.android.diexpenses.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import es.upsa.mimo.android.diexpenses.R;

/**
 * Created by Diego on 17/4/16.
 */
public class YesNoDialogFragment extends DialogFragment {

    private static final String TAG = YesNoDialogFragment.class.getSimpleName();

    public interface YesNoDialogListener {
        void onYesClicked(DialogFragment dialog);
        void onNoClicked(DialogFragment dialog);
    }

    private YesNoDialogListener mListener;
    private String title;
    private String message;

    public static YesNoDialogFragment newInstance(YesNoDialogListener mListener, String title, String message) {
        YesNoDialogFragment fragment = new YesNoDialogFragment();
        fragment.mListener = mListener;
        fragment.title = title;
        fragment.message = message;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_menu_delete)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.common_accept), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onPositiveClick");
                        mListener.onYesClicked(YesNoDialogFragment.this);
                    }
                })
                .setNegativeButton(getString(R.string.common_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onNegativeClick");
                        mListener.onNoClicked(YesNoDialogFragment.this);
                    }
                })
                .create();
    }
}

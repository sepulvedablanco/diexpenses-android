package es.upsa.mimo.android.diexpenses.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

/**
 * Created by Diego on 8/5/16.
 */
public class ExplainPermissionDialogFragment extends DialogFragment {

    private static final String TAG = ExplainPermissionDialogFragment.class.getSimpleName();

    public interface ExplainPermissionDialogListener {
        void onOK(String permission);
        void onKO();
    }

    private ExplainPermissionDialogListener mListener;
    private @StringRes int title;
    private @StringRes int message;
    private String permission;

    public static ExplainPermissionDialogFragment newInstance(ExplainPermissionDialogListener mListener,
                                   @StringRes int title, @StringRes int message, String permission) {
        ExplainPermissionDialogFragment fragment = new ExplainPermissionDialogFragment();
        fragment.mListener = mListener;
        fragment.title = title;
        fragment.message = message;
        fragment.permission = permission;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onPositiveClick");
                        mListener.onOK(permission);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onNegativeClick");
                        mListener.onKO();
                    }
                }).create();
    }
}

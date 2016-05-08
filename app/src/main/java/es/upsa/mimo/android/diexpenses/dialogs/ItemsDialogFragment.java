package es.upsa.mimo.android.diexpenses.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import es.upsa.mimo.android.diexpenses.R;

/**
 * Created by Diego on 8/5/16.
 */
public class ItemsDialogFragment extends DialogFragment {

    private static final String TAG = ItemsDialogFragment.class.getSimpleName();

    private @StringRes int titleId;
    private String[] items;
    private DialogInterface.OnClickListener listener;

    public static ItemsDialogFragment newInstance(@StringRes int titleId, String[] items,
                                                  DialogInterface.OnClickListener listener) {
        ItemsDialogFragment fragment = new ItemsDialogFragment();
        fragment.titleId = titleId;
        fragment.items = items;
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.preference_profile_image_origin)
                .setItems(items, listener)
                .setNeutralButton(android.R.string.no, new android.content.DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).create();
    }
}
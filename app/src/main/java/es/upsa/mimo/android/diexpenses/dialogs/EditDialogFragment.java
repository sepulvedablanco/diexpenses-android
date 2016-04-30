package es.upsa.mimo.android.diexpenses.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.upsa.mimo.android.diexpenses.R;

/**
 * Created by Diego on 17/4/16.
 */
public class EditDialogFragment extends DialogFragment {

    private static final String TAG = EditDialogFragment.class.getSimpleName();

    @Bind(R.id.dialog_edit_input) EditText etName;

    public interface EditDialogListener {
        void onAdd(DialogFragment dialog, String text);
        void onEdit(DialogFragment dialog, String text);
        void onCancel(DialogFragment dialog);
    }

    private EditDialogListener mListener;
    private String title;
    private String message;
    private String text;

    public static EditDialogFragment newInstance(EditDialogListener mListener, String title, String message) {
        return newInstance(mListener, title, message, null);
    }

    public static EditDialogFragment newInstance(EditDialogListener mListener, String title, String message, String text) {
        EditDialogFragment fragment = new EditDialogFragment();
        fragment.mListener = mListener;
        fragment.title = title;
        fragment.message = message;
        fragment.text = text;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_edit_dialog, null);

        ButterKnife.bind(this, view);

        int idIcon = android.R.drawable.ic_menu_add;
        String btTitle = getString(R.string.common_create);
        if(text != null) {
            idIcon = android.R.drawable.ic_menu_edit;
            btTitle = getString(R.string.common_edit);
            etName.setText(text);
            etName.setSelection(0, text.length());
        }

        return new AlertDialog.Builder(getActivity())
                .setIcon(idIcon)
                .setView(view)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(btTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onPositiveClick");
                        if(text == null) {
                            mListener.onAdd(EditDialogFragment.this, etName.getText().toString());
                        } else {
                            mListener.onEdit(EditDialogFragment.this, etName.getText().toString());
                        }
                    }
                })
                .setNegativeButton(getString(R.string.common_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onNegativeClick");
                        mListener.onCancel(EditDialogFragment.this);
                    }
                })
                .create();
    }
}

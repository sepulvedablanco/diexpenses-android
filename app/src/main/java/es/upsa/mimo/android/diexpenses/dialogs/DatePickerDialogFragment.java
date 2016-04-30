package es.upsa.mimo.android.diexpenses.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

import java.util.Calendar;

import es.upsa.mimo.android.diexpenses.components.DatePickerDialogWithFixedTitle;

/**
 * Created by Diego on 24/4/16.
 */
public class DatePickerDialogFragment extends DialogFragment {

    private DatePickerDialog.OnDateSetListener mListener;
    private String title;
    private String message;

    public static DatePickerDialogFragment newInstance(DatePickerDialog.OnDateSetListener mListener) {
        return newInstance(mListener, null, null);
    }

    public static DatePickerDialogFragment newInstance(DatePickerDialog.OnDateSetListener mListener, String title) {
        return newInstance(mListener, title, null);
    }

    public static DatePickerDialogFragment newInstance(DatePickerDialog.OnDateSetListener mListener, String title, String message) {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        fragment.mListener = mListener;
        fragment.title = title;
        fragment.message = message;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Calendar cToday = Calendar.getInstance();
        int year = cToday.get(Calendar.YEAR);
        int month = cToday.get(Calendar.MONTH);
        int day = cToday.get(Calendar.DAY_OF_MONTH);

        final DatePickerDialogWithFixedTitle datePickerDialog = new DatePickerDialogWithFixedTitle(getActivity(), getConstructorListener(), year, month, day);
        if (title != null) {
            datePickerDialog.setFixedTitle(title);
        }
        if (message != null) {
            datePickerDialog.setMessage(message);
        }

        if (isAffectedVersion()) {
            datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    getActivity().getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DatePicker dp = datePickerDialog.getDatePicker();
                            mListener.onDateSet(dp, dp.getYear(), dp.getMonth(), dp.getDayOfMonth());
                        }
                    });
            datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                    getActivity().getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
        }

        return datePickerDialog;
    }

    private static boolean isAffectedVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }

    private DatePickerDialog.OnDateSetListener getConstructorListener() {
        return isAffectedVersion() ? null : mListener;
    }
}

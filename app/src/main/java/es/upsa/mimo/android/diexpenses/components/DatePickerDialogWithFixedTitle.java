package es.upsa.mimo.android.diexpenses.components;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;

/**
 * Created by Diego on 29/4/16.
 */
public class DatePickerDialogWithFixedTitle extends DatePickerDialog {

    private String fixedTitle;

    public DatePickerDialogWithFixedTitle(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, callBack, year, monthOfYear, dayOfMonth);
    }

    public void setFixedTitle(String fixedTitle) {
        this.fixedTitle = fixedTitle;
        setTitle(fixedTitle);
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int month, int day) {
        super.onDateChanged(view, year, month, day);
        if(fixedTitle != null) {
            setTitle(fixedTitle);
        }
    }
}

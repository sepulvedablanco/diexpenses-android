package es.upsa.mimo.android.diexpenses.utils;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Created by Diego on 8/5/16.
 */
public class NumericFilter implements InputFilter {

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {

        if (end > start) {
            String sourceText = source.toString();
            String destText = dest.toString();
            boolean matches = sourceText.matches("^[^\\d]$"); // Is separator
            if (matches && destText.isEmpty()) { // Separator in first position
                return "0" + sourceText;
            }
            if (matches && destText.contains(sourceText)) { // There is a separator set
                return "";
            }

            if(matches) { // no separator set yet
                return null;
            }

            String resultingText = destText.substring(0, dstart)
                    + source.subSequence(start, end)
                    + destText.substring(dend);
            if (!resultingText.matches("^\\d+([.,]\\d{1,2})?$")) {
                return "";
            }
        }

        return null;
    }
}
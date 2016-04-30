package es.upsa.mimo.android.diexpenses.listerners;

import android.view.View;

/**
 * Created by Diego on 15/4/16.
 */
public interface ClickListener {

    void onClick(View view, int position);

    void onLongClick(View view, int position);
}
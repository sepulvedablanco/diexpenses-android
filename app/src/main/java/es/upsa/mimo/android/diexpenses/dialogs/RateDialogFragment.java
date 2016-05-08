package es.upsa.mimo.android.diexpenses.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import es.upsa.mimo.android.diexpenses.R;
import es.upsa.mimo.android.diexpenses.utils.Diexpenses;

/**
 * Created by Diego on 8/5/16.
 */
public class RateDialogFragment extends DialogFragment {

    private static final String TAG = RateDialogFragment.class.getSimpleName();

    public static RateDialogFragment newInstance() {
        RateDialogFragment fragment = new RateDialogFragment();
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getString(R.string.rate_title))
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(getActivity().getString(R.string.rate_message))

                .setPositiveButton(getActivity().getString(R.string.rate_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(getActivity().getString(R.string.protocol_market, getActivity().getPackageName())));
                        if (intent.resolveActivity(getActivity().getPackageManager()) == null) {
                            intent.setData(Uri.parse(getActivity().getString(R.string.protocol_market_url, getActivity().getPackageName())));
                        }
                        getActivity().startActivity(intent);
                        Diexpenses.updateHasRated(getActivity());
                    }
                })

                .setNegativeButton(getActivity().getString(R.string.common_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing
                    }
                }).create();
    };
}

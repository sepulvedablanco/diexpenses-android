package es.upsa.mimo.android.diexpenses.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.upsa.mimo.android.diexpenses.models.KindBase;

/**
 * Created by Diego on 14/4/16.
 */
public class KindsBaseAdapter extends RecyclerView.Adapter<KindsBaseAdapter.KindsBaseViewHolder> {

    private static final String TAG = KindsBaseAdapter.class.getSimpleName();

    private List<? extends KindBase> lstKindBase;

    public KindsBaseAdapter() {
        this.lstKindBase = new ArrayList<KindBase>();
    }

    @Override
    public KindsBaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(android.R.layout.simple_list_item_1, viewGroup, false);

        return new KindsBaseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(KindsBaseViewHolder viewHolder, int pos) {
        String methodName = "onBindViewHolder - ";
        Log.d(TAG, methodName + "start - pos=" + pos);

        KindBase kindBase = lstKindBase.get(pos);
        viewHolder.bindKindBase(kindBase);

        Log.d(TAG, methodName + "end");
    }

    @Override
    public int getItemCount() {
        String methodName = "getItemCount - ";
        Log.d(TAG, methodName + "start. Size=" + lstKindBase.size() + " - End");

        return lstKindBase.size();
    }

    public static class KindsBaseViewHolder extends RecyclerView.ViewHolder {

        @BindView(android.R.id.text1) TextView tvKindBase;

        public KindsBaseViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        public void bindKindBase(KindBase kindBase) {
            String methodName = "bindKindBase - ";
            Log.d(TAG, methodName + "start. Description=" + kindBase.getDescription());

            tvKindBase.setText(kindBase.getDescription());

            Log.d(TAG, methodName + "end");
        }
    }

    public void setKindsBase(List<? extends KindBase> lstKindBase) {
        this.lstKindBase = lstKindBase;
        this.notifyDataSetChanged();
    }
}

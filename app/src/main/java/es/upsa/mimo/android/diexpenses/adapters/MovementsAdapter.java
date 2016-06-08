package es.upsa.mimo.android.diexpenses.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.upsa.mimo.android.diexpenses.R;
import es.upsa.mimo.android.diexpenses.models.Movement;
import es.upsa.mimo.android.diexpenses.utils.Diexpenses;

/**
 * Created by Diego on 20/4/16.
 */
public class MovementsAdapter extends RecyclerView.Adapter<MovementsAdapter.MovementViewHolder> {

    private static final String TAG = MovementsAdapter.class.getSimpleName();

    private Context mContext;
    private List<Movement> lstMovements;

    public MovementsAdapter(Context context) {
        this.mContext = context;
        this.lstMovements = new ArrayList<Movement>();
    }

    @Override
    public MovementViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_movement, viewGroup, false);

        return new MovementViewHolder(mContext, itemView);
    }

    @Override
    public void onBindViewHolder(MovementViewHolder viewHolder, int pos) {
        String methodName = "onBindViewHolder - ";
        Log.d(TAG, methodName + "start - pos=" + pos);

        Movement movement = lstMovements.get(pos);
        viewHolder.bindMovement(movement);

        Log.d(TAG, methodName + "end");
    }

    @Override
    public int getItemCount() {
        String methodName = "getItemCount - ";
        Log.d(TAG, methodName + "start. Size=" + lstMovements.size() + " - End");

        return lstMovements.size();
    }

    public static class MovementViewHolder extends RecyclerView.ViewHolder {

        private Context mContext;

        @BindView(R.id.row_movement_icon) ImageView ivIcon;
        @BindView(R.id.row_movement_concept_amount) TextView tvConcept;
        @BindView(R.id.row_movement_kind_subkind) TextView tvKindSubkind;
        @BindView(R.id.row_movement_bank_account) TextView tvAccountNumber;

        public MovementViewHolder(Context context, View itemView) {
            super(itemView);

            mContext = context;

            ButterKnife.bind(this, itemView);
        }

        public void bindMovement(Movement movement) {
            String methodName = "bindMovement - ";
            Log.d(TAG, methodName + "start. Concept=" + movement.getConcept());


            ivIcon.setImageResource(getImageResource(movement.getExpense()));
            tvConcept.setText(movement.getConcept() + " (" + Diexpenses.formatAmount(movement.getAmount()) + Diexpenses.getCurrency(mContext) + ")");
            String subkindDescription = movement.getSubkind() != null ? " - " + movement.getSubkind().getDescription() : "";
            tvKindSubkind.setText(movement.getKind().getDescription() + subkindDescription
                    + " (" + Diexpenses.formatDate(movement.getTransactionDate()) + ")");
            tvAccountNumber.setText(movement.getBankAccount().getCompleteBankAccount());

            Log.d(TAG, methodName + "end");
        }

        private int getImageResource(Boolean isExpense) {
            String methodName = "getImageResource - ";
            Log.d(TAG, methodName + "start. isExpense=" + isExpense);
            if(isExpense == null) {
                return R.drawable.unknown_100;
            }

            int id =  isExpense ? R.drawable.ic_remove_black_48dp : R.drawable.ic_add_black_48dp;

            Log.d(TAG, methodName + "end. Id=" + id);
            return id;
        }
    }

    public void setMovements(List<Movement> lstMovements) {
        this.lstMovements = lstMovements;
        this.notifyDataSetChanged();
    }
}

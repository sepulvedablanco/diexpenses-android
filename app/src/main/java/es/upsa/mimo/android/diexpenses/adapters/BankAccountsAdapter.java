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
import es.upsa.mimo.android.diexpenses.models.BankAccount;
import es.upsa.mimo.android.diexpenses.utils.Diexpenses;

/**
 * Created by Diego on 19/4/16.
 */
public class BankAccountsAdapter extends RecyclerView.Adapter<BankAccountsAdapter.BankAccountViewHolder> {

    private static final String TAG = BankAccountsAdapter.class.getSimpleName();

    private Context mContext;
    private List<BankAccount> lstBankAccount;

    public BankAccountsAdapter(Context context) {
        this.mContext = context;
        this.lstBankAccount = new ArrayList<BankAccount>();
    }

    @Override
    public BankAccountViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_bank_account, viewGroup, false);

        return new BankAccountViewHolder(mContext, itemView);
    }

    @Override
    public void onBindViewHolder(BankAccountViewHolder viewHolder, int pos) {
        String methodName = "onBindViewHolder - ";
        Log.d(TAG, methodName + "start - pos=" + pos);

        BankAccount bankAccount = lstBankAccount.get(pos);
        viewHolder.bindBankAccount(bankAccount);

        Log.d(TAG, methodName + "end");
    }

    @Override
    public int getItemCount() {
        String methodName = "getItemCount - ";
        Log.d(TAG, methodName + "start. Size=" + lstBankAccount.size() + " - End");

        return lstBankAccount.size();
    }

    public static class BankAccountViewHolder extends RecyclerView.ViewHolder {

        private Context mContext;

        @BindView(R.id.row_bank_account_icon) ImageView ivIcon;
        @BindView(R.id.row_bank_account_description) TextView tvDescription;
        @BindView(R.id.row_bank_account_accountNumber) TextView tvAccountNumber;

        public BankAccountViewHolder(Context context, View itemView) {
            super(itemView);

            mContext = context;

            ButterKnife.bind(this, itemView);
        }

        public void bindBankAccount(BankAccount bankAccount) {
            String methodName = "bindBankAccount - ";
            Log.d(TAG, methodName + "start. Description=" + bankAccount.getDescription());

            ivIcon.setImageResource(getImageResource(bankAccount.getEntity()));
            tvDescription.setText(bankAccount.getDescription() + " (" + Diexpenses.formatAmount(bankAccount.getBalance()) + Diexpenses.getCurrency(mContext) + ")");
            tvAccountNumber.setText(bankAccount.getCompleteBankAccount());

            Log.d(TAG, methodName + "end");
        }

        private int getImageResource(String entity) {
            String methodName = "getImageResource - ";
            Log.d(TAG, methodName + "start. Entity=" + entity);
            int iResource = -1;
            switch (entity) {
                case "0216":
                    iResource = R.drawable.targo_bank_100;
                    break;
                case "0049":
                    iResource = R.drawable.santander_100;
                    break;
                case "0075":
                    iResource = R.drawable.popular_100;
                    break;
                case "0073":
                    iResource = R.drawable.open_bank_100;
                    break;
                case "0131":
                    iResource = R.drawable.novo_bank_100;
                    break;
                case "2048":
                    iResource = R.drawable.liberbank_100;
                    break;
                case "2100":
                    iResource = R.drawable.lacaixa_100;
                    break;
                case "2095":
                    iResource = R.drawable.kutxabank_100;
                    break;
                case "1465":
                    iResource = R.drawable.ing_direct_100;
                    break;
                case "2085":
                    iResource = R.drawable.ibercaja_100;
                    break;
                case "0239":
                    iResource = R.drawable.evo_bank_100;
                    break;
                case "0145":
                    iResource = R.drawable.deutsche_bank_100;
                    break;
                case "3058":
                    iResource = R.drawable.cajamar_100;
                    break;
                case "0057":
                    iResource = R.drawable.bbva_100;
                    break;
                case "0128":
                    iResource = R.drawable.bankinter_100;
                    break;
                case "2038":
                    iResource = R.drawable.bankia_100;
                    break;
                default:
                    iResource = R.drawable.unknown_100;
            }

            Log.d(TAG, methodName + "end. Id=" + iResource);
            return iResource;
        }
    }

    public void setBankAccounts(List<BankAccount> lstBankAccount) {
        this.lstBankAccount = lstBankAccount;
        this.notifyDataSetChanged();
    }
}

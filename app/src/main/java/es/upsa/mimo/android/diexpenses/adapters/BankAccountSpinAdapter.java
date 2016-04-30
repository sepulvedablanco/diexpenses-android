package es.upsa.mimo.android.diexpenses.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import es.upsa.mimo.android.diexpenses.models.BankAccount;

/**
 * Created by Diego on 24/4/16.
 */
public class BankAccountSpinAdapter extends ArrayAdapter<BankAccount> {

    private Context context;
    private List<BankAccount> values;
    private int resource;

    public BankAccountSpinAdapter(Context context, int textViewResourceId, List<BankAccount> values) {
        super(context, textViewResourceId);
        this.context = context;
        this.resource = textViewResourceId;
        this.values = values;
    }

    public int getCount(){
        return values.size();
    }

    public BankAccount getItem(int position){
        return values.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv = createViewFromResource(convertView, parent);
        tv.setText(values.get(position).getDescription());
        return tv;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView tv = createViewFromResource(convertView, parent);
        tv.setText(values.get(position).getDescription());
        return tv;
    }

    public TextView createViewFromResource(View convertView, ViewGroup parent) {
        TextView tv;
        if (convertView == null) {
            LayoutInflater mInflater = LayoutInflater.from(context);
            tv = (TextView) mInflater.inflate(resource, parent, false);
        } else {
            tv = (TextView) convertView;
        }
        return tv;
    }
}

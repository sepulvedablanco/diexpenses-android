package es.upsa.mimo.android.diexpenses.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import es.upsa.mimo.android.diexpenses.models.KindBase;

/**
 * Created by Diego on 24/4/16.
 */
public class KindBaseSpinAdapter<T extends KindBase> extends ArrayAdapter {

    private Context context;
    private List<T> values;
    private int resource;

    public KindBaseSpinAdapter(Context context, int textViewResourceId, List<T> values) {
        super(context, textViewResourceId);
        this.context = context;
        this.resource = textViewResourceId;
        this.values = values;
    }

    public int getCount(){
        return values.size();
    }

    public T getItem(int position){
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

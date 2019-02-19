package com.truebil.crm.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.truebil.crm.Models.FilterCarModel;
import com.truebil.crm.R;

import java.util.ArrayList;

public class ModelFilterGridViewAdapter extends BaseAdapter {

    private ArrayList<FilterCarModel> models;
    private Context context;

    public ModelFilterGridViewAdapter(ArrayList<FilterCarModel> models, Context context) {
        this.context = context;
        this.models = models;
    }

    @Override
    public int getCount() {
        return models.size();
    }

    @Override
    public Object getItem(int i) {
        return models.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        FilterCarModel carModel = models.get(i);

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater == null) return null;

            convertView = inflater.inflate(R.layout.item_model_grid_view, parent, false);
        }

        TextView modelName = convertView.findViewById(R.id.item_model_grid_view_text_view);
        modelName.setText(carModel.getName());
        convertView.setTag(carModel);

        return convertView;
    }
}

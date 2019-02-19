package com.truebil.crm.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.truebil.crm.Models.FeatureItem;
import com.truebil.crm.R;

import java.util.List;

public class GridViewAdapter extends BaseAdapter {

    private List<FeatureItem> mFeatureItemsList;
    private Context mContext;

    public GridViewAdapter(Context mContext, List<FeatureItem> items) {
        this.mFeatureItemsList = items;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mFeatureItemsList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFeatureItemsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        FeatureItem featureItem = mFeatureItemsList.get(position);

        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            if (inflater == null) return null;

            convertView = inflater.inflate(R.layout.item_listing_feature, parent, false);
            ImageView featureItemImageView = convertView.findViewById(R.id.item_listing_feature_image_view);
            TextView featureItemTextView = convertView.findViewById(R.id.item_listing_feature_text_view);

            featureItemImageView.setImageResource(featureItem.getFeatureIcon());
            featureItemTextView.setText(featureItem.getFeatureText());

            if (!featureItem.isActive()) { //Set gray/black shade
                featureItemTextView.setTextColor(Color.GRAY);
            }
            else {
                featureItemImageView.setColorFilter(ContextCompat.getColor(mContext, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);
            }
        }

        return convertView;
    }
}

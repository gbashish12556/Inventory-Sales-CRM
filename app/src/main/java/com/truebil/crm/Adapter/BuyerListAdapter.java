package com.truebil.crm.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.truebil.crm.Activities.UserProfileActivity;
import com.truebil.crm.Models.BuyersListModel;
import com.truebil.crm.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class BuyerListAdapter extends ArrayAdapter<BuyersListModel> {

    private Context context;
    private static final String TAG = "BuyerListAdapter";

    public BuyerListAdapter(@NonNull Context context, int resource, ArrayList<BuyersListModel> data) {
        super(context, resource, data);
        this.context = context;
    }

    @NonNull @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final BuyersListModel listingModel = getItem(position);

        if (listingModel != null) {

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_users_list_view, parent, false);
            }

            TextView monthTextView = convertView.findViewById(R.id.month);;
            TextView dateTextView = convertView.findViewById(R.id.date);
            TextView buyerNameTextView = convertView.findViewById(R.id.buyer_name);;
            ImageView callButtonImageView = convertView.findViewById(R.id.call_buyer_button);
            TextView buyerStatusTextView = convertView.findViewById(R.id.item_user_status_text_view);
            TextView buyerOfferTextView = convertView.findViewById(R.id.buyer_offer);
            TextView offerTypeTextView = convertView.findViewById(R.id.item_user_list_offer_type_text_view);
            TextView salesRepNameTextView = convertView.findViewById(R.id.item_user_list_representative_name_text_view);
            offerTypeTextView.setVisibility(View.GONE);

            String visitDate = listingModel.getVisitDate();
            if (visitDate != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                    Date date = sdf.parse(visitDate);
                    int days = date.getDate();
                    String month = new SimpleDateFormat("MMM", Locale.US).format(date.getTime());
                    monthTextView.setText(month.toUpperCase());
                    dateTextView.setText(String.valueOf(days));
                }
                catch (ParseException e) {
                    //e.printStackTrace();
                    monthTextView.setText("NA");
                    dateTextView.setText("NA");
                }
            }
            else {
                monthTextView.setText("NA");
                dateTextView.setText("NA");
            }

            if (listingModel.getSalesRepresentativeName() != null) {
                salesRepNameTextView.setVisibility(View.VISIBLE);
                salesRepNameTextView.setText("Rep: " + listingModel.getSalesRepresentativeName());
            }
            else
                salesRepNameTextView.setVisibility(View.GONE);

            if (listingModel.getBuyerOffer() == 0) {
                buyerOfferTextView.setVisibility(View.GONE);
            }
            else {
                buyerOfferTextView.setText("Offer: ₹" + listingModel.getBuyerOffer());
            }

            buyerNameTextView.setText(listingModel.getBuyerName());

            if (listingModel.getBuyerStatus() != null) {
                buyerStatusTextView.setVisibility(View.VISIBLE);
                buyerStatusTextView.setText(listingModel.getBuyerStatus());
                buyerStatusTextView.setTextColor(Color.parseColor(listingModel.getStatusColor()));
            }
            else
                buyerStatusTextView.setVisibility(View.GONE);

            callButtonImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + String.valueOf(listingModel.getBuyerMobile())));
                    context.startActivity(callIntent);
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int buyerId = listingModel.getBuyerId();
                    Bundle bundle = new Bundle();
                    bundle.putString("BUYER_ID", String.valueOf(buyerId));
                    Intent intent = new Intent(context, UserProfileActivity.class);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });
        }

        return convertView;
    }
}
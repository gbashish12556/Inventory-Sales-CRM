package com.truebil.crm.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.crm.Activities.UserProfileActivity;
import com.truebil.crm.Constants;
import com.truebil.crm.Helper;
import com.truebil.crm.Network.VolleyService;
import com.truebil.crm.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class OfferDetailFragment extends Fragment {

    private static final String TAG = "OfferDetailsFragment";
    private SharedPreferences sharedPref;
    private RequestQueue queue;
    private ArrayList<OfferDetailModel> offerDetailModelList = new ArrayList<>();
    private OfferDetailAdapter offerDetailAdapter;
    private String nextUrl;
    private int preLast;
    private ListView offersListView;
    private ProgressBar loaderProgressBar;
    private LinearLayout emptyStateLinearLayout;
    private TextView emptyStateTextView;

    public OfferDetailFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_offer_detail, container, false);
        sharedPref = getContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        if (getArguments() != null) {
            String listingId = getArguments().getString("LISTING_ID");

            String url = Constants.Config.API_PATH + "/car_offer_details?listing_id=" + listingId;
            fetchOfferDetails(url);
        }

        emptyStateLinearLayout = rootView.findViewById(R.id.item_empty_state_linear_layout);
        emptyStateTextView = rootView.findViewById(R.id.item_empty_state_header_text_view);
        offersListView = rootView.findViewById(R.id.fragment_offer_details_list_view);
        offerDetailAdapter = new OfferDetailAdapter(getContext(), R.layout.item_car_listing, offerDetailModelList);
        offersListView.setAdapter(offerDetailAdapter);
        setListViewFooter();

        setupEndlessListener();

        return rootView;
    }

    void setupEndlessListener() {
        offersListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                final int lastItem = firstVisibleItem + visibleItemCount;

                if (lastItem == totalItemCount) {
                    if (preLast != lastItem) { //to avoid multiple calls for last item
                        Log.d(TAG, "Reached end of list. Fetching more cars...");
                        preLast = lastItem;

                        if (nextUrl != null && !nextUrl.equals("null")) {
                            loaderProgressBar.setVisibility(View.VISIBLE);
                            fetchOfferDetails(nextUrl);
                        }
                    }
                }
            }
        });
    }

    void fetchOfferDetails(String url) {
        if (getActivity() == null)
            return;

        if (queue == null)
            queue = Volley.newRequestQueue(getActivity());

        final JSONObject jsonObject = new JSONObject();
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, jsonObject,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    parseOfferDetails(response);
                    loaderProgressBar.setVisibility(View.GONE);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        jsonObject.put("API","fetch_offer_detail_get_api");
                        VolleyService.handleVolleyError(error,jsonObject,true, getActivity());
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    loaderProgressBar.setVisibility(View.GONE);
                }
            }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String salesRepToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");
                headers.put("Authorization", "jwt " + salesRepToken);
                return headers;
            }
        };

        jsonRequest.setTag(TAG);
        queue.add(jsonRequest);
    }

    void parseOfferDetails(JSONObject response) {
        try {

            nextUrl = response.getString("next");
            JSONArray data = response.getJSONArray("results");

            if (data.length() == 0) {
                emptyStateLinearLayout.setVisibility(View.VISIBLE);
                emptyStateTextView.setText(R.string.no_offer);
                loaderProgressBar.setVisibility(View.GONE);
            }

            else {
                emptyStateLinearLayout.setVisibility(View.GONE);
                for (int i = 0; i < data.length(); i++) {
                    JSONObject offerJson = data.getJSONObject(i);
                    OfferDetailModel offerDetailModel = new OfferDetailModel(offerJson);
                    offerDetailModelList.add(offerDetailModel);
                }

                offerDetailAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void setListViewFooter(){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.footer_listview_progressbar,null);
        loaderProgressBar = view.findViewById(R.id.footer_listview_progress_bar);
        offersListView.addFooterView(view);
    }

    public class OfferDetailAdapter extends ArrayAdapter<OfferDetailModel> {

        Context context;

        OfferDetailAdapter(@NonNull Context context, int resource, ArrayList<OfferDetailModel> data) {
            super(context, resource, data);
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            final OfferDetailModel offerDetailModel = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_users_list_view, parent, false);
            }

            TextView nameTextView = convertView.findViewById(R.id.buyer_name);
            TextView offerTextView = convertView.findViewById(R.id.buyer_offer);
            TextView monthTextView = convertView.findViewById(R.id.month);
            TextView dateTextView = convertView.findViewById(R.id.date);
            TextView statusTextView = convertView.findViewById(R.id.item_user_status_text_view);
            ImageView callImageView = convertView.findViewById(R.id.call_buyer_button);
            TextView offerTypeTextView = convertView.findViewById(R.id.item_user_list_offer_type_text_view);
            TextView salesRepNameTextView = convertView.findViewById(R.id.item_user_list_representative_name_text_view);
            salesRepNameTextView.setVisibility(View.GONE);
            offerTypeTextView.setVisibility(View.VISIBLE);

            if (offerDetailModel != null) {
                nameTextView.setText(offerDetailModel.getBuyerName());
                offerTextView.setText("Offer: " + Helper.getIndianCurrencyFormat(offerDetailModel.getOfferedPrice()));

                if (offerDetailModel.getOfferType() != null)
                    offerTypeTextView.setText(offerDetailModel.getOfferType());
                else
                    offerTypeTextView.setVisibility(View.GONE);

                String visitDate = offerDetailModel.getVisitDate();
                if (visitDate != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

                    try {
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

                if (offerDetailModel.getBuyerStatus() != null) {
                    statusTextView.setVisibility(View.VISIBLE);
                    statusTextView.setText(offerDetailModel.getBuyerStatus());
                    statusTextView.setTextColor(Color.parseColor(offerDetailModel.getStatusColor()));
                }
                else
                    statusTextView.setVisibility(View.GONE);

                callImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + String.valueOf(offerDetailModel.getBuyerMobile())));
                        context.startActivity(callIntent);
                    }
                });

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), UserProfileActivity.class);
                        intent.putExtra("BUYER_ID", String.valueOf(offerDetailModel.getBuyerId()));
                        startActivity(intent);
                    }
                });
            }

            return convertView;
        }
    }

    class OfferDetailModel {

        private String visitDate = null,
                buyerName,
                buyerStatus,
                statusColor,
                offerType;
        private long buyerMobile;
        private int buyerId,
                offeredPrice = 0;


        OfferDetailModel(JSONObject jsonObject) {
            try {
                if (jsonObject.has("name"))
                    buyerName = jsonObject.getString("name");

                if (jsonObject.has("mobile"))
                    buyerMobile = jsonObject.getLong("mobile");

                buyerId = jsonObject.getInt("buyer_id");

                if (!jsonObject.isNull("visit_date"))
                    visitDate = jsonObject.getString("visit_date");

                if (!jsonObject.isNull("status")) {
                    buyerStatus = jsonObject.getJSONObject("status").getString("display");
                    statusColor = jsonObject.getJSONObject("status").getString("color");
                }

                if (!jsonObject.isNull("offer")) {
                    offeredPrice = jsonObject.getJSONObject("offer").getInt("amount");
                    offerType = jsonObject.getJSONObject("offer").getString("type");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public int getBuyerId() {
            return buyerId;
        }

        public String getVisitDate() {
            return visitDate;
        }

        public String getBuyerName() {
            return buyerName;
        }

        public long getBuyerMobile() {
            return buyerMobile;
        }

        public String getBuyerStatus() {
            return buyerStatus;
        }

        public int getOfferedPrice() {
            return offeredPrice;
        }

        public String getStatusColor() {
            return statusColor;
        }

        public String getOfferType() {
            return offerType;
        }
    }

}

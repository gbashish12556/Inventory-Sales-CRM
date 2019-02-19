package com.truebil.crm.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.crm.Constants;
import com.truebil.crm.Helper;
import com.truebil.crm.Models.ServiceAmountListModel;
import com.truebil.crm.Network.VolleyService;
import com.truebil.crm.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DealTermsFragment extends Fragment {

    TextView totalAmountTextView, totalPaidForCarTextView, totalPaidForServiceTextView, totalPendingTextView;
    int carPaymentPending = 0, servicesPaymentPending = 0;
    DealTermsFragmentInterface mCallback;
    public static final String TAG = "DealTermsFragment";
    private RequestQueue requestQueue;
    private SharedPreferences sharedPref;
    private LinearLayout servicesListLinearLayout;
    private int buyerVisitListingId;
    private ProgressBar progressBar;

    public interface DealTermsFragmentInterface {
        void onMakePaymentClick(int carPaymentPending, int servicesPaymentPending);
        //void updateServiceList(ArrayList<ServiceAmountListModel> listData);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (DealTermsFragmentInterface) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement DealTermsFragmentInterface");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.fragment_deal_terms, container, false);

        if (getActivity() == null)
            return rootView;

        Helper.setupKeyboardHidingUI(rootView, getActivity());
        sharedPref = getActivity().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        servicesListLinearLayout = rootView.findViewById(R.id.fragment_deal_terms_services_list_linear_layout);
        LinearLayout makePaymentLinearLayout = rootView.findViewById(R.id.fragment_deal_terms_pay_linear_layout);
        totalAmountTextView = rootView.findViewById(R.id.fragment_deal_terms_total_amount_textview);
        totalPaidForCarTextView = rootView.findViewById(R.id.fragment_deal_terms_total_paid_for_car_textview);
        totalPaidForServiceTextView = rootView.findViewById(R.id.fragment_deal_terms_total_paid_for_service_textview);
        totalPendingTextView = rootView.findViewById(R.id.fragment_deal_terms_total_pending);
        progressBar = rootView.findViewById(R.id.fragment_deal_terms_progress_bar);
        TextView cancelTextView = rootView.findViewById(R.id.fragment_deal_terms_cancel_text_view);

        cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        makePaymentLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mCallback.updateServiceList(serviceAmountListData);
                mCallback.onMakePaymentClick(carPaymentPending, servicesPaymentPending);
            }
        });

        if (getArguments() != null) {
            buyerVisitListingId = getArguments().getInt("buyer_visit_listing_id");
        }

        fetchServiceListData();

        return rootView;
    }

    public void fetchServiceListData() {

        String url = Constants.Config.API_PATH + "/bill_info/?buyer_visit_listing_id=" + buyerVisitListingId;

        if (getActivity() == null)
            return;

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseServiceList(response);
                        progressBar.setVisibility(View.GONE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        JSONObject additionalLogJson = new JSONObject();
                        try {
                            additionalLogJson.put("API", "/bill_info (GET)");
                            VolleyService.handleVolleyError(error, additionalLogJson, true, getContext());
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String dealerJWTToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");
                headers.put("Authorization", "jwt " + dealerJWTToken);
                return headers;
            }
        };

        jsonObject.setTag(TAG);
        requestQueue.add(jsonObject);
    }

    public void parseServiceList(JSONObject response) {
        try {
            Boolean status = (Boolean) response.get("status");
            if (!status)
                return;

            JSONObject data = response.getJSONObject("data");

            JSONArray buyerListarray = data.getJSONArray("services");
            int totalAmount = data.getInt("total_amount");
            int totalPaidForCar = data.getInt("total_paid_for_car");
            int totalPaidForService = data.getInt("total_paid_for_service");
            int totalPending = data.getInt("total_pending");
            if (data.has("car_payment_pending"))
                carPaymentPending = data.getInt("car_payment_pending");
            if (data.has("services_payment_pending"))
                servicesPaymentPending = data.getInt("services_payment_pending");

            totalAmountTextView.setText(Helper.getIndianCurrencyFormat(totalAmount));
            totalPaidForCarTextView.setText(Helper.getIndianCurrencyFormat(totalPaidForCar));
            totalPaidForServiceTextView.setText(Helper.getIndianCurrencyFormat(totalPaidForService));
            totalPendingTextView.setText(Helper.getIndianCurrencyFormat(totalPending));

            for (int i = 0; i < buyerListarray.length(); i++) {
                JSONObject individualResultJSON = buyerListarray.getJSONObject(i);
                ServiceAmountListModel buyersListModel = new ServiceAmountListModel(individualResultJSON);

                View serviceView = getLayoutInflater().inflate(R.layout.item_services_with_amount, servicesListLinearLayout, false);
                TextView serviceTextView = serviceView.findViewById(R.id.service_name);
                TextView amountEditText = serviceView.findViewById(R.id.service_amount);

                serviceTextView.setText(buyersListModel.getServiceName());
                amountEditText.setText(String.valueOf(buyersListModel.getServiceAmount()));
                serviceTextView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                amountEditText.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                serviceView.setBackgroundColor(Color.parseColor("#EDEDF5"));

                // Add main service header with combined amount
                if (buyersListModel.getIsSelected()) {
                    servicesListLinearLayout.addView(serviceView);
                }

                // Add individual services
                if (individualResultJSON.has("selected_services")) {
                    JSONArray individualServiceJson = individualResultJSON.getJSONArray("selected_services");
                    for (int j=0; j<individualServiceJson.length(); j++) {
                        ServiceAmountListModel individualServiceModel = new ServiceAmountListModel(individualServiceJson.getJSONObject(j));
                        View miniServiceView = getLayoutInflater().inflate(R.layout.item_services_with_amount, servicesListLinearLayout, false);
                        TextView miniServiceTextView = miniServiceView.findViewById(R.id.service_name);
                        TextView miniAmountEditText = miniServiceView.findViewById(R.id.service_amount);
                        miniServiceTextView.setText(individualServiceModel.getServiceName());
                        miniAmountEditText.setText(String.valueOf(individualServiceModel.getServiceAmount()));
                        servicesListLinearLayout.addView(miniServiceView);
                    }
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (requestQueue != null)
            requestQueue.cancelAll(TAG);
    }

    // NOT IN USE
    public void updateServiceList(ArrayList<ServiceAmountListModel> listData) {

        String url = Constants.Config.API_PATH + "/bill_info/";

        final JSONObject params = new JSONObject();
        try {
            params.put("buyer_visit_listing_id", buyerVisitListingId);
            for (int j = 0; j < listData.size(); j++) {
                params.put(listData.get(j).getServiceKey(), String.valueOf(listData.get(j).getServiceAmount()));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //startMakePaymentDialog(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            params.put("API", "/bill_info (POST)");
                            VolleyService.handleVolleyError(error, params, true, getActivity());
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String dealerJWTToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");
                headers.put("Authorization", "jwt " + dealerJWTToken);
                return headers;
            }
        };

        jsonRequest.setTag(TAG);
        requestQueue.add(jsonRequest);
    }
}

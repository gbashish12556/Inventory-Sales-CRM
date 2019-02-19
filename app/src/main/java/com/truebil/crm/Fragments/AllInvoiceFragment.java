package com.truebil.crm.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.crm.Constants;
import com.truebil.crm.Network.VolleyService;
import com.truebil.crm.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class AllInvoiceFragment extends Fragment {

    private AllInvoiceListAdapter allInvoiceListAdapter;
    private ArrayList<AllInvoiceListModel> allInvoiceDataList = new ArrayList<>();
    private RequestQueue requestQueue;
    private int buyerVisitListingId;
    private SharedPreferences sharedPref;
    public static final String TAG = "AllInvoiceFragment";
    private LinearLayout emptyStateLinearLayout;
    private TextView emptyStateTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_all_invoice, container, false);

        if (getActivity() == null)
            return rootView;

        sharedPref = getActivity().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        emptyStateLinearLayout = rootView.findViewById(R.id.item_empty_state_linear_layout);
        emptyStateTextView = rootView.findViewById(R.id.item_empty_state_header_text_view);
        allInvoiceListAdapter = new AllInvoiceListAdapter(allInvoiceDataList, getActivity());
        RecyclerView invoiceListingRecyclerView = rootView.findViewById(R.id.fragment_all_invoices_list_view);
        invoiceListingRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        invoiceListingRecyclerView.setAdapter(allInvoiceListAdapter);

        if (getArguments() != null) {
            buyerVisitListingId = getArguments().getInt("buyer_visit_listing_id");
            fetchInvoicesListData();
        }

        return rootView;
    }

    public void fetchInvoicesListData() {

        String url = Constants.Config.API_PATH + "/invoices/?buyer_visit_listing_id=" + buyerVisitListingId;

        if (getActivity() == null)
            return;

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseInvoice(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        final JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("API", "/invoices (GET)");
                            jsonObject.put("buyer_visit_listing_id", buyerVisitListingId);
                            VolleyService.handleVolleyError(error, jsonObject, true, getActivity());
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

        jsObjRequest.setTag(TAG);
        requestQueue.add(jsObjRequest);
    }

    void parseInvoice(JSONObject response) {
        try {
            Boolean status = (Boolean) response.get("status");
            if (!status)
                return;

            String invoiceData = String.valueOf(response.getJSONObject("data").getJSONArray("invoices"));

            JSONArray buyerListArray = new JSONArray(invoiceData);

            if (buyerListArray.length() == 0) {
                emptyStateLinearLayout.setVisibility(View.VISIBLE);
                emptyStateTextView.setText(R.string.no_invoice);
            }

            else {
                emptyStateLinearLayout.setVisibility(View.GONE);
                for (int i=0; i<buyerListArray.length(); i++) {
                    JSONObject individualResultJSON = buyerListArray.getJSONObject(i);
                    AllInvoiceListModel buyersListModel = new AllInvoiceListModel(individualResultJSON);
                    allInvoiceDataList.add(buyersListModel);
                }
                allInvoiceListAdapter.notifyDataSetChanged();
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class AllInvoiceListAdapter extends RecyclerView.Adapter {

        ArrayList<AllInvoiceListModel> allInvoiceDataList;
        Context mContext;

        AllInvoiceListAdapter(ArrayList<AllInvoiceListModel> allInvoiceDataList , Context mContext) {
            this.allInvoiceDataList = allInvoiceDataList;
            this.mContext = mContext;
        }

        @NonNull @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            return new AllInvoiceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((AllInvoiceViewHolder) holder).bindData(allInvoiceDataList.get(position));
        }

        @Override
        public int getItemCount() {
            return allInvoiceDataList.size();
        }

        @Override
        public int getItemViewType(final int position) {
            return R.layout.item_payment_invoice;
        }
    }

    public class AllInvoiceViewHolder extends RecyclerView.ViewHolder {

        private TextView paymentDateTextView;
        private TextView transactionIdTextView;
        private TextView paymentForCarTextView;
        private TextView paymentForServiceTextView;
        private TextView totalAmountTextView;

        AllInvoiceViewHolder(final View itemView) {
            super(itemView);
            paymentDateTextView = itemView.findViewById(R.id.payment_date);
            transactionIdTextView = itemView.findViewById(R.id.transaction_id);
            paymentForCarTextView = itemView.findViewById(R.id.payment_for_car);
            paymentForServiceTextView = itemView.findViewById(R.id.payment_for_service);
            totalAmountTextView = itemView.findViewById(R.id.total_amount);
        }

        public void bindData(final AllInvoiceListModel viewModel) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            try {
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date date = simpleDateFormat.parse(viewModel.getPaymentDate());
                DateFormat formatter = new SimpleDateFormat("MMM dd, E", Locale.US);
                paymentDateTextView.setText(formatter.format(date));
                transactionIdTextView.setText(viewModel.getTransactionId());
                paymentForCarTextView.setText("\u20B9"+String.valueOf(viewModel.getPaymentForCar()));
                paymentForServiceTextView.setText("\u20B9"+String.valueOf(viewModel.getPaymentForService()));
                totalAmountTextView.setText("\u20B9"+String.valueOf(viewModel.getTotalAmount()));
            }
            catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public class AllInvoiceListModel {
        private String paymentDate;
        private String transactionId;
        private int paymentForCar;
        private int paymentForService;
        private int totalAmount;

        AllInvoiceListModel(JSONObject jsonObject){
            try {
                transactionId = jsonObject.getString("transaction_id");
                paymentDate = jsonObject.getString("payment_date").replace("T", " ");
                paymentForService =  jsonObject.getInt("payment_for_service");
                paymentForCar =  jsonObject.getInt("payment_for_car");
                totalAmount = paymentForService+paymentForCar;
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public int getPaymentForCar() {
            return paymentForCar;
        }

        public int getPaymentForService() {
            return paymentForService;
        }

        public String getPaymentDate() {
            return paymentDate;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public int getTotalAmount() {
            return totalAmount;
        }
    }
 }

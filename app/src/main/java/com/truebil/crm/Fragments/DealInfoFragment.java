package com.truebil.crm.Fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.crm.Adapter.ServiceAmountListToggleAdapter;
import com.truebil.crm.Constants;
import com.truebil.crm.Helper;
import com.truebil.crm.Models.ServiceAmountListModel;
import com.truebil.crm.Network.VolleyService;
import com.truebil.crm.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DealInfoFragment extends Fragment {

    private static final String TAG = "DealInfoFragment";
    RecyclerView paperTransferRecyclerView, serviceWarrantyRecyclerView, octroitRecyclerView, adjustmentRecyclerView;
    ServiceAmountListToggleAdapter paperTransfertListAdapter, serviceWarrantyListAdapter, octroiListAdapter, adjustmentListAdapter;
    private RequestQueue queue;
    private DealInfoInterface mCallback;
    private String buyerVisitListingId;
    private SharedPreferences sharedPref;
    private ProgressBar loadingProgressBar;
    private LinearLayout superLinearLayout;
    private EditText carValueEditText, internalNoteEditText, customerMessageEditText, deliveryDateEditText;
    private Spinner transferCategorySpinner, amountCollectionExclusiveSpinner, sellerRTOSpinner;
    private ArrayAdapter<String> transferCategoryAdapter, amountCollectionExclusiveAdapter, sellerRTOAdapter;
    private ArrayList<ServiceAmountListModel> paperTransferList, serviceWarrantyList, octroiList, adjustmentList;
    CheckBox insuranceCheckBox;
    EditText insuranceAmountEditText;
    Switch insuranceInclusiveSwitch;
    RadioGroup insuranceRadioGroup;
    RadioButton truebilDirectRadioButton, postsalesTeamRadioButton;

    public DealInfoFragment() {
    }

    public interface DealInfoInterface {
        void OnBuyerInfoClick();
        void OnLoanInfoClick();
        void OnFormValidationError(String error);
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (DealInfoInterface) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement DealInfoInterface");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_deal_info, container, false);

        if (getArguments() != null) {
            buyerVisitListingId = getArguments().getString("BUYER_VISIT_LISTING_ID");
        }

        if (getActivity() == null || getContext() == null)
            return rootView;

        Helper.setupKeyboardHidingUI(rootView, getActivity());
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPref = getContext().getSharedPreferences("APP_PREFS", 0);

        superLinearLayout = rootView.findViewById(R.id.fragment_deal_info_super_linear_layout);
        loadingProgressBar = rootView.findViewById(R.id.fragment_deal_info_progress_bar);
        carValueEditText = rootView.findViewById(R.id.fragment_deal_info_car_value_edit_text);
        internalNoteEditText = rootView.findViewById(R.id.fragment_deal_info_internal_note_edit_text);
        customerMessageEditText = rootView.findViewById(R.id.fragment_deal_info_send_message_edit_text);
        deliveryDateEditText = rootView.findViewById(R.id.fragment_deal_info_date_delivery_edit_text);
        transferCategorySpinner = rootView.findViewById(R.id.fragment_deal_info_transfer_category_spinner);
        amountCollectionExclusiveSpinner = rootView.findViewById(R.id.fragment_deal_info_amount_collection_spinner);
        sellerRTOSpinner = rootView.findViewById(R.id.fragment_deal_info_seller_rto_spinner);

        paperTransferList = new ArrayList<>();
        octroiList = new ArrayList<>();
        serviceWarrantyList = new ArrayList<>();
        adjustmentList = new ArrayList<>();

        paperTransfertListAdapter = new ServiceAmountListToggleAdapter(paperTransferList,DealInfoFragment.this);
        octroiListAdapter = new ServiceAmountListToggleAdapter(octroiList,DealInfoFragment.this);
        serviceWarrantyListAdapter = new ServiceAmountListToggleAdapter(serviceWarrantyList,DealInfoFragment.this);
        adjustmentListAdapter = new ServiceAmountListToggleAdapter(adjustmentList,DealInfoFragment.this);

        serviceWarrantyRecyclerView = rootView.findViewById(R.id.fragement_deal_info_service_warranty_recycler_view);
        paperTransferRecyclerView = rootView.findViewById(R.id.fragement_deal_info_paper_transfer_recycler_view);
        octroitRecyclerView = rootView.findViewById(R.id.fragement_deal_info_octroi_recycler_view);
        adjustmentRecyclerView = rootView.findViewById(R.id.fragement_deal_info_ajustment_recycler_view);

        RecyclerView.LayoutManager paperTransferLayoutManager = new LinearLayoutManager(getActivity());
        paperTransferRecyclerView.setLayoutManager(paperTransferLayoutManager);

        RecyclerView.LayoutManager octroiLayoutManager = new LinearLayoutManager(getActivity());
        octroitRecyclerView.setLayoutManager(octroiLayoutManager);

        RecyclerView.LayoutManager serviceLayoutManager = new LinearLayoutManager(getActivity());
        serviceWarrantyRecyclerView.setLayoutManager(serviceLayoutManager);

        RecyclerView.LayoutManager adjustmentLayoutManager = new LinearLayoutManager(getActivity());
        adjustmentRecyclerView.setLayoutManager(adjustmentLayoutManager);

        serviceWarrantyRecyclerView.setAdapter(serviceWarrantyListAdapter);
        paperTransferRecyclerView.setAdapter(paperTransfertListAdapter);
        octroitRecyclerView.setAdapter(octroiListAdapter);
        adjustmentRecyclerView.setAdapter(adjustmentListAdapter);

        LinearLayout buyerInfoLinearLayout = rootView.findViewById(R.id.fragment_deal_info_buyer_info_linear_layout);
        LinearLayout loanInfoLinearLayout = rootView.findViewById(R.id.fragment_deal_info_load_info_linear_layout);

        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "yyyy-MM-dd"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                deliveryDateEditText.setText(sdf.format(calendar.getTime()));
            }
        };

        insuranceCheckBox = rootView.findViewById(R.id.fragment_deal_info_insurance_required_checkbox);
        insuranceAmountEditText = rootView.findViewById(R.id.fragment_deal_info_insurance_amount_edit_text);
        insuranceInclusiveSwitch = rootView.findViewById(R.id.fragment_deal_info_insurance_inclusive_switch);
        insuranceRadioGroup = rootView.findViewById(R.id.fragment_deal_info_insurance_radio_group);
        truebilDirectRadioButton = rootView.findViewById(R.id.fragment_deal_info_truebil_direct_radio_button);
        postsalesTeamRadioButton = rootView.findViewById(R.id.fragment_deal_info_postsales_team_radio_button);

        insuranceRadioGroup.setVisibility(View.GONE);
        insuranceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if (isChecked) {
                    insuranceRadioGroup.setVisibility(View.VISIBLE);
                }
                else {
                    insuranceRadioGroup.setVisibility(View.GONE);
                }
            }
        });

        deliveryDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis() - 1000);
                datePickerDialog.show();
            }
        });

        // Fetch data from Constants and populate spinner adapters

        // Category of Transfer
        ArrayList<String> transferCategoryArray = Helper.getCategoryOfTransferList(getContext());
        transferCategoryArray.add(0, getString(R.string.category_of_transfer_prompt));
        transferCategoryAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, transferCategoryArray);
        transferCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transferCategorySpinner.setAdapter(transferCategoryAdapter);

        // Amount Collection Paper Transfer
        ArrayList<String> amountCollectionPaperTransferArray = Helper.getAmountCollectionPaperTransferList(getContext());
        amountCollectionPaperTransferArray.add(0, getString(R.string.amount_collection_exclusive_prompt));
        amountCollectionExclusiveAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, amountCollectionPaperTransferArray);
        amountCollectionExclusiveAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        amountCollectionExclusiveSpinner.setAdapter(amountCollectionExclusiveAdapter);

        // Seller RTOs from Shared Preferences
        ArrayList<String> rtoList = Helper.getRTOList(getContext());
        rtoList.add(0, getString(R.string.seller_rto_prompt));
        sellerRTOAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, rtoList);
        sellerRTOAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sellerRTOSpinner.setAdapter(sellerRTOAdapter);

        buyerInfoLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.OnBuyerInfoClick();
            }
        });

        loanInfoLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isValidForm()) {

                    JSONArray serviceWarranty = new JSONArray();
                    for(int i=0;i<serviceWarrantyList.size();i++){
                        if (serviceWarrantyList.get(i).getIsSelected()) {
                            try {
                                JSONObject newObject = new JSONObject();
                                newObject.put("id", serviceWarrantyList.get(i).getServiceKey());
                                newObject.put("amount", serviceWarrantyList.get(i).getServiceAmount());
                                newObject.put("is_inclusive", serviceWarrantyList.get(i).getInclusive());
                                serviceWarranty.put(newObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    JSONArray paperTransfer = new JSONArray();
                    for(int i=0;i<paperTransferList.size();i++){
                        if (paperTransferList.get(i).getIsSelected()) {
                            try {
                                JSONObject newObject = new JSONObject();
                                newObject.put("id", paperTransferList.get(i).getServiceKey());
                                newObject.put("amount", paperTransferList.get(i).getServiceAmount());
                                newObject.put("is_inclusive", paperTransferList.get(i).getInclusive());
                                paperTransfer.put(newObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    JSONArray octroi = new JSONArray();
                    for(int i=0;i<octroiList.size();i++){
                        if (octroiList.get(i).getIsSelected()) {
                            try {
                                JSONObject newObject = new JSONObject();
                                newObject.put("id", octroiList.get(i).getServiceKey());
                                newObject.put("amount", octroiList.get(i).getServiceAmount());
                                newObject.put("is_inclusive", octroiList.get(i).getInclusive());
                                octroi.put(newObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    JSONArray adjustmentsCharges = new JSONArray();
                    for (int i=0; i < adjustmentList.size(); i++) {
                        if (adjustmentList.get(i).getIsSelected()) {
                            try {
                                JSONObject newObject = new JSONObject();
                                newObject.put("id", adjustmentList.get(i).getServiceKey());
                                newObject.put("amount", adjustmentList.get(i).getServiceAmount());
                                newObject.put("is_inclusive", adjustmentList.get(i).getInclusive());
                                adjustmentsCharges.put(newObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    JSONArray insurance = new JSONArray();
                    if (insuranceCheckBox.isChecked()) {
                        JSONObject insuranceJson = new JSONObject();
                        try {
                            if (insuranceRadioGroup.getCheckedRadioButtonId() == R.id.fragment_deal_info_truebil_direct_radio_button)
                                insuranceJson.put("id", "5");
                            else if (insuranceRadioGroup.getCheckedRadioButtonId() == R.id.fragment_deal_info_postsales_team_radio_button) {
                                insuranceJson.put("id", "6");
                            }
                            insuranceJson.put("amount", insuranceAmountEditText.getText().toString());
                            insuranceJson.put("is_inclusive", insuranceInclusiveSwitch.isChecked());
                            insurance.put(insuranceJson);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    int transferCategoryId = Helper.getCategoryOfTransferId(transferCategorySpinner.getSelectedItem().toString(), getContext());
                    int amountCollectionExclusiveId = Helper.getAmountCollectionPaperTransferId(amountCollectionExclusiveSpinner.getSelectedItem().toString(), getContext());
                    int sellerRTOId = Helper.getRtoId(sellerRTOSpinner.getSelectedItem().toString(), getContext());

                    postDealInfo(carValueEditText.getText().toString(),
                            deliveryDateEditText.getText().toString(),
                            serviceWarranty,
                            paperTransfer,
                            octroi,
                            insurance,
                            adjustmentsCharges,
                            amountCollectionExclusiveId,
                            transferCategoryId,
                            sellerRTOId,
                            internalNoteEditText.getText().toString(),
                            customerMessageEditText.getText().toString());
                }
            }
        });

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            showLoadingProgress();
            clearServiceAmountAdapters();
            getPreFilledDealInfoData(buyerVisitListingId);
        }
    }

    void clearServiceAmountAdapters() {
        paperTransferList.clear();
        serviceWarrantyList.clear();
        octroiList.clear();
        adjustmentList.clear();

        paperTransfertListAdapter.notifyDataSetChanged();
        serviceWarrantyListAdapter.notifyDataSetChanged();
        octroiListAdapter.notifyDataSetChanged();
        adjustmentListAdapter.notifyDataSetChanged();
    }

    void getPreFilledDealInfoData(final String buyerVisitListingId) {
        String url = Constants.Config.API_PATH + "/deal_info?buyer_visit_listing_id=" + buyerVisitListingId;

        if (getActivity() == null)
            return;

        if (queue == null)
            queue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hideLoadingProgress();
                parseDealInfoGetResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoadingProgress();
                final JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("BVL_ID", buyerVisitListingId);
                    jsonObject.put("API","/deal_info (GET)");
                    VolleyService.handleVolleyError(error,jsonObject,true, getActivity());
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String dealerJWTToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");
                headers.put("Authorization", "jwt " + dealerJWTToken);
                return headers;
            }
        };

        jsonRequest.setTag(TAG);
        queue.add(jsonRequest);
    }

    void postDealInfo(String carValue,
                      String scheduleDeliveryDate,
                      JSONArray serviceWarranty,
                      JSONArray paperTransfer,
                      JSONArray Octroi,
                      JSONArray insurance,
                      JSONArray adjustmentCharges,
                      int paperTransferAmountCollectedById,
                      int transferCategoryId,
                      int sellerRtoId,
                      String internalNote,
                      String customerMessage) {

        String url = Constants.Config.API_PATH + "/deal_info/";

        if (getActivity() == null)
            return;

        if (queue == null)
            queue = Volley.newRequestQueue(getActivity());

        final JSONObject params = new JSONObject();
        try {
            params.put("buyer_visit_listing_id", buyerVisitListingId);
            params.put("truebil_offer", carValue);
            params.put("scheduled_date_of_delivery", scheduleDeliveryDate);
            params.put("service_warranty", serviceWarranty);
            params.put("paper_transfer", paperTransfer);
            params.put("octroi", Octroi);
            params.put("insurance", insurance);
            params.put("adjustment_charges", adjustmentCharges);
            params.put("amount_collection_for_paper_transfer", String.valueOf(paperTransferAmountCollectedById));
            params.put("category_of_transfer", String.valueOf(transferCategoryId));
            params.put("seller_rto", String.valueOf(sellerRtoId));
            params.put("internal_remarks", internalNote);
            params.put("customer_remarks", customerMessage);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "/deal_info (POST) params: " + params);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parseDealInfoPostResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    params.put("API","/deal_info (POST)");
                    VolleyService.handleVolleyError(error,params,true, getActivity());
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String dealerJWTToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");
                Log.d("dealerJWTToken",dealerJWTToken);
                headers.put("Authorization", "jwt " + dealerJWTToken);
                return headers;
            }
        };

        jsonRequest.setTag(TAG);
        queue.add(jsonRequest);
    }

    void parseDealInfoGetResponse(JSONObject response) {
        try {
            boolean status = response.getBoolean("status");
            if (!status) return;

            JSONObject saleObject = response.getJSONObject("sale");

            if (!saleObject.isNull("amount_collection_for_paper_transfer")) {
                int amountCollectedPaperTransfer = saleObject.getInt("amount_collection_for_paper_transfer");
                String amountCollectedPaperTransferString = Helper.getAmountCollectionPaperTransferName(amountCollectedPaperTransfer, getContext());
                int spinnerPosition = amountCollectionExclusiveAdapter.getPosition(amountCollectedPaperTransferString);
                amountCollectionExclusiveSpinner.setSelection(spinnerPosition);
            }

            if (!saleObject.isNull("customer_remarks")) {
                String customerMessage = saleObject.getString("customer_remarks");
                customerMessageEditText.setText(customerMessage);
            }

            if (!saleObject.isNull("service_warranty")) {
                JSONArray serviceWarrantyArray = saleObject.getJSONArray("service_warranty");
                for (int i=0; i<serviceWarrantyArray.length(); i++) {
                    JSONObject individualResultJSON = serviceWarrantyArray.getJSONObject(i);
                    ServiceAmountListModel buyersListModel = new ServiceAmountListModel(individualResultJSON);
                    serviceWarrantyList.add(buyersListModel);
                }
                serviceWarrantyListAdapter.notifyDataSetChanged();
            }

            if (saleObject.has("paper_transfer")) {
                JSONArray paperTransferArray = saleObject.getJSONArray("paper_transfer");
                for (int i=0; i<paperTransferArray.length(); i++) {
                    JSONObject individualResultJSON = paperTransferArray.getJSONObject(i);
                    ServiceAmountListModel buyersListModel = new ServiceAmountListModel(individualResultJSON);
                    paperTransferList.add(buyersListModel);
                }
                paperTransfertListAdapter.notifyDataSetChanged();
            }

            if (saleObject.has("octroi")) {
                JSONArray paperTransferArray = saleObject.getJSONArray("octroi");
                for (int i=0; i<paperTransferArray.length(); i++) {
                    JSONObject individualResultJSON = paperTransferArray.getJSONObject(i);
                    ServiceAmountListModel buyersListModel = new ServiceAmountListModel(individualResultJSON);
                    octroiList.add(buyersListModel);
                }
                octroiListAdapter.notifyDataSetChanged();
            }

            if (saleObject.has("insurance")) {
                JSONArray insuranceArray = saleObject.getJSONArray("insurance");
                for (int i=0; i<insuranceArray.length(); i++) {
                    JSONObject individualResultJSON = insuranceArray.getJSONObject(i);
                    ServiceAmountListModel buyersListModel = new ServiceAmountListModel(individualResultJSON);

                    if (buyersListModel.getIsSelected()) {
                        int amount = buyersListModel.getServiceAmount();
                        boolean isInclusive = buyersListModel.getInclusive();
                        String id = buyersListModel.getServiceKey();

                        insuranceCheckBox.setChecked(true);
                        insuranceAmountEditText.setText(String.valueOf(amount));
                        insuranceInclusiveSwitch.setChecked(isInclusive);
                        if (id.equals("5")) {
                            truebilDirectRadioButton.setChecked(true);
                        }
                        else if (id.equals("6")) {
                            postsalesTeamRadioButton.setChecked(true);
                        }
                    }
                }
            }

            if (saleObject.has("adjustment_charges")) {
                JSONArray adjustmentChargesArray = saleObject.getJSONArray("adjustment_charges");
                for (int i=0; i<adjustmentChargesArray.length(); i++) {
                    JSONObject individualResultJSON = adjustmentChargesArray.getJSONObject(i);
                    ServiceAmountListModel buyersListModel = new ServiceAmountListModel(individualResultJSON);
                    adjustmentList.add(buyersListModel);
                }
                adjustmentListAdapter.notifyDataSetChanged();
            }

            if (!saleObject.isNull("category_of_transfer")) {
                int transferCategory = saleObject.getInt("category_of_transfer");
                String transferCategoryString = Helper.getCategoryOfTransferName(transferCategory, getContext());
                int spinnerPosition = transferCategoryAdapter.getPosition(transferCategoryString);
                transferCategorySpinner.setSelection(spinnerPosition);
            }

            if (!saleObject.isNull("truebil_offer")) {
                int carValue = saleObject.getInt("truebil_offer");
                carValueEditText.setText(String.valueOf(carValue));
            }

            if (!saleObject.isNull("scheduled_date_of_delivery")) {
                String scheduledDeliveryDate = saleObject.getString("scheduled_date_of_delivery");
                deliveryDateEditText.setText(scheduledDeliveryDate);
            }

            if (!saleObject.isNull("internal_remarks")) {
                String internalNote = saleObject.getString("internal_remarks");
                internalNoteEditText.setText(internalNote);
            }

            if (!saleObject.isNull("seller_rto")) {
                int sellerRTO = saleObject.getInt("seller_rto");
                String sellerRTOString = Helper.getRtoName(sellerRTO, getContext());
                int spinnerPosition = sellerRTOAdapter.getPosition(sellerRTOString);
                sellerRTOSpinner.setSelection(spinnerPosition);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void parseDealInfoPostResponse(JSONObject response) {
        try {
            boolean status = response.getBoolean("status");
            if (!status)
                return;

            Toast.makeText(getContext(), "Deal Info Submitted", Toast.LENGTH_SHORT).show();
            mCallback.OnLoanInfoClick();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    boolean isValidForm() {

        if (carValueEditText.getText().toString().isEmpty()) {
            mCallback.OnFormValidationError("Car Value is required");
            return false;
        }
        else if (transferCategorySpinner.getSelectedItem().toString().equals(getString(R.string.category_of_transfer_prompt))) {
            mCallback.OnFormValidationError("Category of Transfer is required");
            return false;
        }
        else if (sellerRTOSpinner.getSelectedItem().toString().equals(getString(R.string.seller_rto_prompt))) {
            mCallback.OnFormValidationError("Seller RTO is required");
            return false;
        }
        else if (amountCollectionExclusiveSpinner.getSelectedItem().toString().equals(getString(R.string.amount_collection_exclusive_prompt))) {
            mCallback.OnFormValidationError("Amount Collection is required");
            return false;
        }

        mCallback.OnFormValidationError("");
        return true;
    }

    ArrayList<String> createListFromJson(String spinnerCategory, String spinnerType) {
        ArrayList<String> list = new ArrayList<>();
        try {
            JSONObject jsonObject = (new JSONObject(spinnerCategory)).getJSONObject(spinnerType);
            JSONArray keys = jsonObject.names();
            for (int i=0; i<keys.length(); i++) {
                String key = keys.getString(i);
                list.add(key);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    void showLoadingProgress() {
        superLinearLayout.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.VISIBLE);
    }

    void hideLoadingProgress() {
        superLinearLayout.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.GONE  );
    }
}
package com.truebil.crm.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.crm.Constants;
import com.truebil.crm.Helper;
import com.truebil.crm.Network.VolleyService;
import com.truebil.crm.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuyerInfoFragment extends Fragment {

    private BuyerInfoInterface mCallback;
    private RequestQueue queue;
    private String buyerVisitListingId;
    private static final String TAG = "BuyerInfoFragment";
    private LinearLayout superLinearLayout;
    private ProgressBar loadingProgressBar;
    private EditText nameEditText, emailEditText, phoneEditText, alternatePhoneEditText, panEditText;
    private Spinner localitySpinner, buyerRtoSpinner, buyerTypeSpinner;
    private ArrayAdapter<String> buyerLocalityAdapter, buyerRtoAdapter, buyerTypeAdapter;
    private List<String> localityList = new ArrayList<>(), rtoList = new ArrayList<>(), buyerTypeList = new ArrayList<>();
    private SharedPreferences sharedPref;

    public BuyerInfoFragment() {
    }

    public interface BuyerInfoInterface {
        void OnCancelClick();
        void OnDealInfoClick();
        void OnFormValidationError(String error);
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (BuyerInfoInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BuyerInfoInterface");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_buyer_info, container, false);

        if (getActivity() == null || getContext() == null)
            return rootView;

        Helper.setupKeyboardHidingUI(rootView, getActivity());
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        sharedPref = getContext().getSharedPreferences("APP_PREFS", 0);

        loadingProgressBar = rootView.findViewById(R.id.fragment_buyer_info_progress_bar);
        superLinearLayout = rootView.findViewById(R.id.fragment_buyer_info_super_linear_layout);
        nameEditText = rootView.findViewById(R.id.fragment_buyer_info_name_edit_text);
        emailEditText = rootView.findViewById(R.id.fragment_buyer_info_email_edit_text);
        phoneEditText = rootView.findViewById(R.id.fragment_buyer_info_phone_text_view);
        alternatePhoneEditText = rootView.findViewById(R.id.fragment_buyer_info_alternate_phone_edit_text);
        buyerRtoSpinner = rootView.findViewById(R.id.fragment_buyer_info_buyer_rto_spinner);
        localitySpinner = rootView.findViewById(R.id.fragment_buyer_info_locality_spinner);
        buyerTypeSpinner = rootView.findViewById(R.id.fragment_buyer_info_buyer_type_spinner);
        panEditText = rootView.findViewById(R.id.fragment_buyer_info_buyer_pan_edit_text);
        Button cancelButton = rootView.findViewById(R.id.buyer_info_fragment_cancel_button);
        LinearLayout dealInfoLinearLayout = rootView.findViewById(R.id.buyer_info_fragment_deal_info_linear_layout);

        rtoList = Helper.getRTOList(getContext());
        rtoList.add(0, getString(R.string.buyer_rto_prompt));
        buyerRtoAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, rtoList);
        buyerRtoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        buyerRtoSpinner.setAdapter(buyerRtoAdapter);

        localityList = Helper.getLocalityList(getContext());
        localityList.add(0, getString(R.string.locality_prompt));
        buyerLocalityAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, localityList);
        buyerLocalityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        localitySpinner.setAdapter(buyerLocalityAdapter);

        buyerTypeList = Helper.getBuyerTypeList(getContext());
        buyerTypeList.add(0, getString(R.string.buyer_type_prompt));
        buyerTypeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, buyerTypeList);
        buyerTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        buyerTypeSpinner.setAdapter(buyerTypeAdapter);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.OnCancelClick();
            }
        });

        dealInfoLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isValidForm()) {
                    int buyerRTOId = Helper.getRtoId(buyerRtoSpinner.getSelectedItem().toString(), getContext());
                    int buyerLocalityId = Helper.getLocalityId(localitySpinner.getSelectedItem().toString(), getContext());
                    int buyerTypeId = Helper.getBuyerTypeId(buyerTypeSpinner.getSelectedItem().toString(), getContext());

                    postBuyerInfo(nameEditText.getText().toString(),
                        alternatePhoneEditText.getText().toString(),
                        emailEditText.getText().toString(),
                        buyerLocalityId,
                        buyerRTOId,
                        buyerTypeId,
                        panEditText.getText().toString());
                }
            }
        });

        if (getArguments() != null) {
            buyerVisitListingId = getArguments().getString("BUYER_VISIT_LISTING_ID");

            showLoadingProgress();
            getPreFilledBuyerInfoData(buyerVisitListingId);
        }

        return rootView;
    }

    void getPreFilledBuyerInfoData(final String buyerVisitListingId) {

        String url = Constants.Config.API_PATH + "/buyer_additional_info?buyer_visit_listing_id=" + buyerVisitListingId;

        if (getActivity() == null)
            return;

        if (queue == null)
            queue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hideLoadingProgress();
                parseBuyerInfoGetResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoadingProgress();
                final JSONObject infoJson = new JSONObject();
                try {
                    infoJson.put("BVL_ID", buyerVisitListingId);
                    infoJson.put("API","buyer_additional_info (GET)");
                    VolleyService.handleVolleyError(error, infoJson, true, getActivity());
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

    void postBuyerInfo(String name,
                       String buyerAlternateContact,
                       String buyerEmail,
                       int buyerLocalityId,
                       int buyerRTOId,
                       int buyerTypeId,
                       String buyerPan) {

        String url = Constants.Config.API_PATH + "/buyer_additional_info/";

        if (getActivity() == null)
            return;

        if (queue == null)
            queue = Volley.newRequestQueue(getActivity());

        final JSONObject params = new JSONObject();
        try {
            params.put("buyer_visit_listing_id", buyerVisitListingId);
            params.put("name", name);
            params.put("locality", String.valueOf(buyerLocalityId));
            params.put("rto", String.valueOf(buyerRTOId));
            params.put("pan", buyerPan);
            params.put("type", String.valueOf(buyerTypeId));
            params.put("email", buyerEmail);

            // Include alternate_mobile in POST params only if it is not null or empty
            if (buyerAlternateContact != null && !buyerAlternateContact.trim().isEmpty() && !buyerAlternateContact.trim().equals(""))
                params.put("alternate_mobile", buyerAlternateContact);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parseBuyerInfoPostResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    params.put("API","/buyer_additional_info (POST)");
                    VolleyService.handleVolleyError(error, params, true, getActivity());
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

    void parseBuyerInfoGetResponse(JSONObject response) {
        try {
            boolean status = response.getBoolean("status");
            if (!status) return;

            JSONObject buyerInfoJSONObject = response.getJSONObject("buyer_info");

            if (!buyerInfoJSONObject.isNull("name")) {
                String buyerName = buyerInfoJSONObject.getString("name");
                nameEditText.setText(buyerName);
            }
            if (!buyerInfoJSONObject.isNull("mobile")) {
                String buyerContact = buyerInfoJSONObject.getString("mobile");
                phoneEditText.setText(buyerContact);
            }
            if (!buyerInfoJSONObject.isNull("alternate_mobile")) {
                String buyerAlternateContact = buyerInfoJSONObject.getString("alternate_mobile");
                alternatePhoneEditText.setText(buyerAlternateContact);
            }
            if (!buyerInfoJSONObject.isNull("email")) {
                String buyerEmail = buyerInfoJSONObject.getString("email");
                emailEditText.setText(buyerEmail);
            }
            if(!buyerInfoJSONObject.isNull("pancard")) {
                String buyerPan = buyerInfoJSONObject.getString("pancard");
                panEditText.setText(buyerPan);
            }
            if (!buyerInfoJSONObject.isNull("rto")) {
                int buyerRTO = buyerInfoJSONObject.getInt("rto");
                String buyerRTOString = Helper.getRtoName(buyerRTO, getContext());
                int spinnerPosition = buyerRtoAdapter.getPosition(buyerRTOString);
                buyerRtoSpinner.setSelection(spinnerPosition);
            }
            if (!buyerInfoJSONObject.isNull("locality")) {
                int buyerLocality = buyerInfoJSONObject.getInt("locality");
                String buyerLocalityString = Helper.getLocalityName(buyerLocality, getContext());
                int spinnerPosition = buyerLocalityAdapter.getPosition(buyerLocalityString);
                localitySpinner.setSelection(spinnerPosition);
            }
            if (!buyerInfoJSONObject.isNull("type")) {
                int buyerType = buyerInfoJSONObject.getInt("type");
                String buyerTypeString = Helper.getBuyerTypeName(buyerType, getContext());
                int spinnerPosition = buyerTypeAdapter.getPosition(buyerTypeString);
                buyerTypeSpinner.setSelection(spinnerPosition);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void parseBuyerInfoPostResponse(JSONObject response) {
        try {
            boolean status = response.getBoolean("status");
            if (!status)
                return;

            Toast.makeText(getContext(), "Buyer Info Submitted", Toast.LENGTH_LONG).show();
            mCallback.OnDealInfoClick();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    boolean isValidForm() {

        if (nameEditText.getText().toString().isEmpty()) {
            mCallback.OnFormValidationError("Name is required");
            return false;
        }
        else if (phoneEditText.getText().toString().isEmpty()) {
            mCallback.OnFormValidationError("Phone is required");
            return false;
        }
        else if (emailEditText.getText().toString().isEmpty()) {
            mCallback.OnFormValidationError("Email is required");
            return false;
        }
        else if (localitySpinner.getSelectedItem().toString().equals(getString(R.string.locality_prompt))) {
            mCallback.OnFormValidationError("Locality is required");
            return false;
        }
        else if (buyerTypeSpinner.getSelectedItem().toString().equals(getString(R.string.buyer_type_prompt))) {
            mCallback.OnFormValidationError("Buyer Type is required");
            return false;
        }
        else if (buyerRtoSpinner.getSelectedItem().toString().equals(getString(R.string.buyer_rto_prompt))) {
            mCallback.OnFormValidationError("RTO is required");
            return false;
        }

        mCallback.OnFormValidationError("");
        return true;
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
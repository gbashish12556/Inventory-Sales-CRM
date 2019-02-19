package com.truebil.crm.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FeedbackDetailFragment extends Fragment {

    private static final String TAG = "FeedbackDetailFragment";
    private FeedbackDetailFragmentInterface mCallback;
    private EditText customerNoteEditText, buyerFinalOfferEditText, truebilFinalOfferEditText;
    private String buyerVisitListingId;
    private SharedPreferences sharedPref;
    private RequestQueue queue;
    int statusId;

    public FeedbackDetailFragment() {
    }

    public interface FeedbackDetailFragmentInterface {
        void OnCancelSelected();
        void OnFeedbackPostSuccess();
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (FeedbackDetailFragmentInterface) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FeedbackDetailFragmentInterface");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feedback_detail, container, false);
        Helper.setupKeyboardHidingUI(rootView, getActivity());

        sharedPref = getContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        buyerFinalOfferEditText = rootView.findViewById(R.id.feedback_detail_buyer_final_offer);
        truebilFinalOfferEditText = rootView.findViewById(R.id.feedback_detail_truebil_final_offer);
        customerNoteEditText = rootView.findViewById(R.id.feedback_detail_customer_note);

        if (getArguments() != null) {
            buyerVisitListingId = getArguments().getString("BUYER_VISIT_LISTING_ID");
            statusId = getArguments().getInt("STATUS_ID");

            // View or Hide edit text based on feedback type
            if (statusId >=1 && statusId <= 6) {
                buyerFinalOfferEditText.setVisibility(View.VISIBLE);
                truebilFinalOfferEditText.setVisibility(View.VISIBLE);
            }
            else {
                buyerFinalOfferEditText.setVisibility(View.GONE);
                truebilFinalOfferEditText.setVisibility(View.GONE);
            }
        }
        else {
            Toast.makeText(getContext(), "Arguments not passed to FeedbackDetailFragment", Toast.LENGTH_LONG).show();
            return rootView;
        }

        // Fetch Pre-filled Data
        String url = Constants.Config.API_PATH + "/feedback/?buyer_visit_listing_id=" + buyerVisitListingId + "&status_id=" + String.valueOf(statusId);
        fetchFeedback(url);

        Button cancelButton = rootView.findViewById(R.id.fragment_feedback_detail_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.OnCancelSelected();
            }
        });

        LinearLayout saveLinearLayout = rootView.findViewById(R.id.fragment_feedback_detail_save_linear_layout);
        saveLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customerNoteEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please fill Customer Note", Toast.LENGTH_SHORT).show();
                    customerNoteEditText.setError("Required");
                }
                else
                    postFeedback();
            }
        });

        return rootView;
    }

    public void parseFetchResponse(JSONObject response) {
        try {
            Boolean status = (Boolean) response.get("status");
            if (status) {
                JSONObject data = response.getJSONObject("data");
                if (!data.isNull("truebil_offer"))
                    truebilFinalOfferEditText.setText(String.valueOf(data.getInt("truebil_offer")));
                if (!data.isNull("buyer_offer"))
                    buyerFinalOfferEditText.setText(String.valueOf(data.getInt("buyer_offer")));
                if (!data.isNull("comments"))
                    customerNoteEditText.setText(response.getJSONObject("data").getString("comments"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void fetchFeedback(String URL) {

        if (getContext() == null)
            return;

        if (queue == null)
            queue = Volley.newRequestQueue(getContext());

        final JSONObject jsonObject = new JSONObject();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, URL, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseFetchResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            jsonObject.put("API","/feedback (GET)");
                            VolleyService.handleVolleyError(error,jsonObject,true, getActivity());
                        } catch (JSONException e) {
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
        queue.add(jsObjRequest);
    }

    public void postFeedback() {

        String url = Constants.Config.API_PATH + "/feedback/";

        int truebilFinalOffer = 0;
        if (!truebilFinalOfferEditText.getText().toString().isEmpty())
            truebilFinalOffer = Integer.parseInt(truebilFinalOfferEditText.getText().toString());

        int buyerFinalOffer = 0;
        if (!buyerFinalOfferEditText.getText().toString().isEmpty())
            buyerFinalOffer = Integer.parseInt(buyerFinalOfferEditText.getText().toString());

        final JSONObject params = new JSONObject();
        try {
            // Only include truebil_offer and buyer_offer if status_id is 2, 3 or 4.
            if (statusId >= 2 && statusId <= 6) {
                params.put("truebil_offer", String.valueOf(truebilFinalOffer));
                params.put("buyer_offer", String.valueOf(buyerFinalOffer));
            }
            params.put("comments", customerNoteEditText.getText().toString());
            params.put("status_id", String.valueOf(statusId));
            params.put("buyer_visit_listing_id", buyerVisitListingId);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        if (queue == null)
            queue = Volley.newRequestQueue(getContext());

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, params,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Boolean status = (Boolean) response.get("status");
                        if (status) {
                            mCallback.OnFeedbackPostSuccess();
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        params.put("API","/feedback (POST)");
                        VolleyService.handleVolleyError(error, params, true, getActivity());
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        ){
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

    @Override
    public void onStop() {
        super.onStop();
        if (queue != null)
            queue.cancelAll(TAG);
    }
}

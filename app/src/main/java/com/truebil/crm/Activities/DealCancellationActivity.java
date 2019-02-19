package com.truebil.crm.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DealCancellationActivity extends AppCompatActivity {

    RequestQueue requestQueue;
    LinearLayout refundApplicableLayout, superLinearLayout;
    ProgressBar dealCancellationProgressBar;
    String TAG = DealCancellationActivity.class.getName();
    EditText refundAmountEditText, refundNoteEditText;
    Spinner dealCancellationSpinner, cateredBySpinner;
    Switch refundSwitch;
    Boolean refundable = false;
    int buyerVisitListingId;
    SharedPreferences sharedPref;
    ArrayAdapter<String> dealCancellationAdapter, cateredByAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_cancellation);
        Helper.setupKeyboardHidingUI(findViewById(android.R.id.content), this);

        sharedPref = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        if (getIntent().getExtras() != null) { ;
            buyerVisitListingId = Integer.parseInt(getIntent().getStringExtra("BUYER_VISIT_LISTING_ID"));
        }

        dealCancellationSpinner = findViewById(R.id.delivery_activity_delivery_deal_cancellation_reason_spinner);
        ArrayList<String> reasonForCancellationList = Helper.getReasonForCancellationList(getApplicationContext());
        reasonForCancellationList.add(0, getString(R.string.deal_cancellation_prompt));
        dealCancellationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, reasonForCancellationList);
        dealCancellationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dealCancellationSpinner.setAdapter(dealCancellationAdapter);

        cateredBySpinner = findViewById(R.id.delivery_activity_delivery_deal_cancellation_catered_by_spinner);
        ArrayList<String> getCateredByList = Helper.getCateredByList(getApplicationContext());
        getCateredByList.add(0, getString(R.string.catered_by_prompt));
        cateredByAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getCateredByList);
        cateredByAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cateredBySpinner.setAdapter(cateredByAdapter);

        ImageButton backImageButton = findViewById(R.id.activity_deal_cancellation_back_arrow);
        refundAmountEditText = findViewById(R.id.deal_cancellation_activity_refund_amount_edittext);
        refundNoteEditText  = findViewById(R.id.deal_cancellation_activity_refund_note_edittext);
        refundApplicableLayout = findViewById(R.id.refund_applicable_layout);
        refundSwitch = findViewById(R.id.refund_applicable_switch);
        LinearLayout confirmRequestLinearLayout = findViewById(R.id.confirm_request_linear_layout);
        TextView cancelTextView = findViewById(R.id.navigate_to_invoice);
        dealCancellationProgressBar = findViewById(R.id.activity_deal_cancellation_progress_bar);
        superLinearLayout = findViewById(R.id.activity_deal_cancellation_super_linear_layout);

        dealCancellationProgressBar.setVisibility(View.VISIBLE);
        superLinearLayout.setVisibility(View.GONE);

        refundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                refundable = isChecked;
                if (isChecked) {
                    refundApplicableLayout.setVisibility(View.VISIBLE);
                }
                else {
                    refundApplicableLayout.setVisibility(View.GONE);
                }
            }
        });

        confirmRequestLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject params = new JSONObject();
                try {
                    params.put("catered_by", Helper.getCateredById(String.valueOf(cateredBySpinner.getSelectedItem().toString()), getApplicationContext()));
                    params.put("refundable", refundable);
                    params.put("buyer_visit_listing_id", String.valueOf(buyerVisitListingId));
                    params.put("reason", String.valueOf(Helper.getReasonsForCancellationId(dealCancellationSpinner.getSelectedItem().toString(), getApplicationContext())));
                    params.put("refund_amount", refundAmountEditText.getText().toString());
                    params.put("additional_comment", refundNoteEditText.getText().toString());
                    sendPostVolleyRequest(params);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        fetchDealCancellationData();
    }

    public void fetchDealCancellationData() {

        String url = Constants.Config.API_PATH+ "/deal_cancellation/?buyer_visit_listing_id=" + buyerVisitListingId;

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(getApplicationContext());

        JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    dealCancellationProgressBar.setVisibility(View.GONE);
                    superLinearLayout.setVisibility(View.VISIBLE);
                    try {
                        Boolean status = (Boolean) response.get("status");
                        if (status) {
                            parseJSON(response);
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
                    dealCancellationProgressBar.setVisibility(View.GONE);
                    JSONObject params = new JSONObject();
                    try {
                        params.put("API","/deal_cancellation (GET)");
                        VolleyService.handleVolleyError(error, params,true, DealCancellationActivity.this);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            })
        {
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

    public void sendPostVolleyRequest(final JSONObject params) {

        String url = Constants.Config.API_PATH + "/deal_cancellation/";

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(getApplicationContext());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, params,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Boolean status = (Boolean) response.get("status");
                        if (status) {
                            Toast.makeText(getApplicationContext(), "Deal has been cancelled", Toast.LENGTH_SHORT).show();
                            onBackPressed();
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
                        params.put("API","/deal_cancellation (POST)");
                        VolleyService.handleVolleyError(error, params,true, DealCancellationActivity.this);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            })
        {
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

    public void parseJSON(JSONObject response) {

        DealCancellationResponseParser parser = new DealCancellationResponseParser(response);

        refundNoteEditText.setText(parser.getRefundNote());
        refundAmountEditText.setText(parser.getRefundAmount());

        int spinnerPositionReason = dealCancellationAdapter.getPosition(parser.getReason());
        dealCancellationSpinner.setSelection(spinnerPositionReason);

        if (parser.getRefundable()) {
            refundSwitch.setChecked(true);
        }
        else {
            refundSwitch.setChecked(false);
        }

        String cateredByString = Helper.getCateredByName(parser.getCateredBy(), getApplicationContext());
        int spinnerPositionCateredBy = cateredByAdapter.getPosition(cateredByString);
        cateredBySpinner.setSelection(spinnerPositionCateredBy);
    }

    class DealCancellationResponseParser {

        String refundNote = "", refundAmount = "", reason = "";
        Boolean refundable = false;
        int cateredBy;

        DealCancellationResponseParser(JSONObject response) {
            try {
                if (!response.getJSONObject("data").isNull("additional_comment")) {
                    refundNote = response.getJSONObject("data").getString("additional_comment");
                }
                if (!response.getJSONObject("data").isNull("refund_amount")) {
                    refundAmount = response.getJSONObject("data").getString("refund_amount");
                }
                if (!response.getJSONObject("data").isNull("reason")) {
                    reason = response.getJSONObject("data").getString("reason").trim();
                }
                if (!response.getJSONObject("data").isNull("refundable")) {
                    refundable = response.getJSONObject("data").getBoolean("refundable");
                }
                if (!response.getJSONObject("data").isNull("catered_by")) {
                    cateredBy = response.getJSONObject("data").getInt("catered_by");
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String getRefundNote() {
            return refundNote;
        }

        String getRefundAmount() {
            return refundAmount;
        }

        String getReason() {
            return reason;
        }

        Boolean getRefundable() {
            return refundable;
        }

        int getCateredBy() {
            return cateredBy;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (requestQueue != null)
            requestQueue.cancelAll(TAG);
    }
}

package com.truebil.crm.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.crm.Constants;
import com.truebil.crm.FormValidation;
import com.truebil.crm.Network.VolleyService;
import com.truebil.crm.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MakePaymentDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "MakePaymentDialogBox";
    RadioButton onlineRadioButton, cashRadioButton, chequeRadioButton, mSwipeRadioButton, carExchangeRadioButton;
    String paymentMethod = "";
    ImageButton dismissDialogButton;
    ProgressBar paymentProgressBar;
    EditText paymentForCarTextView, paymentForServiceTextView, referenceNumberTextView, noteTextView;
    LinearLayout capturePaymentLinearLayout;
    int buyerVisitListingId, carPaymentPending, servicesPaymentPending;
    SharedPreferences sharedPref;
    private MakePaymentDialogInterface mCallback;

    public interface MakePaymentDialogInterface {
        void onMakePaymentFinish();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (MakePaymentDialogInterface) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement MakePaymentDialogInterface");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.dialog_fragment_make_payment, container, false);

        // Auto hide keyboard
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        if (getArguments() != null && getContext() != null) {
            buyerVisitListingId = getArguments().getInt("buyer_visit_listing_id");
            carPaymentPending = getArguments().getInt("car_payment_pending");
            servicesPaymentPending = getArguments().getInt("services_payment_pending");
        }
        else {
            Toast.makeText(getContext(), "No Buyer Visit Listing ID present", Toast.LENGTH_SHORT).show();
            dismiss();
            return rootView;
        }

        setupKeyboardHidingUI(rootView);
        sharedPref = getContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        dismissDialogButton = rootView.findViewById(R.id.close_dialog_cross_button);
        paymentForCarTextView = rootView.findViewById(R.id.dialog_fragment_make_payment_payment_for_car);
        paymentForServiceTextView = rootView.findViewById(R.id.dialog_fragment_make_payment_payment_for_service);
        referenceNumberTextView = rootView.findViewById(R.id.dialog_fragment_make_payment_reference_edit_text);
        noteTextView = rootView.findViewById(R.id.dialog_fragment_make_payment_note);
        capturePaymentLinearLayout = rootView.findViewById(R.id.dialog_fragment_make_payment_capture_linear_layout);

        TextView carPaymentPendingTextView = rootView.findViewById(R.id.dialog_fragment_make_payment_car_pending_text_view);
        carPaymentPendingTextView.setText("Car Payment Pending: Rs. " + carPaymentPending);
        TextView servicesPaymentPendingTextView = rootView.findViewById(R.id.dialog_fragment_make_payment_service_pending_text_view);
        servicesPaymentPendingTextView.setText("Services Payment Pending: Rs. " + servicesPaymentPending);

        paymentProgressBar = rootView.findViewById(R.id.dialog_fragment_make_payment_progress_bar);
        paymentProgressBar.setVisibility(View.GONE);

        capturePaymentLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (checkValidation()) {

                        if (!paymentMethod.equalsIgnoreCase("")) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("buyer_visit_listing_id", String.valueOf(buyerVisitListingId));
                            jsonObject.put("car_amount", paymentForCarTextView.getText().toString());
                            jsonObject.put("service_amount", paymentForServiceTextView.getText().toString());
                            jsonObject.put("payment_mode", paymentMethod);
                            jsonObject.put("payment_reference_number", referenceNumberTextView.getText().toString());
                            jsonObject.put("remarks", noteTextView.getText().toString());
                            updatePaymentData(jsonObject);

                            Log.d("makePaymentJsonObject", String.valueOf(jsonObject));
                            capturePaymentLinearLayout.setEnabled(false);
                            paymentProgressBar.setVisibility(View.VISIBLE);
                        }
                        else {
                            Toast.makeText(getActivity(), "Empty payment method", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        onlineRadioButton = rootView.findViewById(R.id.online);
        cashRadioButton = rootView.findViewById(R.id.cash);
        chequeRadioButton = rootView.findViewById(R.id.cheque);
        mSwipeRadioButton = rootView.findViewById(R.id.mSwipe);
        carExchangeRadioButton = rootView.findViewById(R.id.car_exchange);

        onlineRadioButton.setOnClickListener(this);
        cashRadioButton.setOnClickListener(this);
        chequeRadioButton.setOnClickListener(this);
        mSwipeRadioButton.setOnClickListener(this);
        carExchangeRadioButton.setOnClickListener(this);

        dismissDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
    }

    @Override
    public void onClick(View v) {
        onlineRadioButton.setChecked(false);
        cashRadioButton.setChecked(false);
        chequeRadioButton.setChecked(false);
        mSwipeRadioButton.setChecked(false);
        carExchangeRadioButton.setChecked(false);

        switch (v.getId()) {
            case R.id.online:
                onlineRadioButton.setChecked(true);
                paymentMethod = "online";
                break;
            case R.id.cash:
                cashRadioButton.setChecked(true);
                paymentMethod = "cash";
                break;
            case R.id.cheque:
                chequeRadioButton.setChecked(true);
                paymentMethod = "cheque";
                break;
            case R.id.mSwipe:
                mSwipeRadioButton.setChecked(true);
                paymentMethod = "mSwipe";
                break;
            case R.id.car_exchange:
                carExchangeRadioButton.setChecked(true);
                paymentMethod = "exchange";
                break;
            default:
                onlineRadioButton.setChecked(true);
                paymentMethod = "online";
                break;
        }
    }

    private boolean checkValidation() {
        boolean ret = true;
        if (!FormValidation.isNumber(paymentForCarTextView, true)) ret = false;
        if (!FormValidation.isNumber(paymentForServiceTextView, true)) ret = false;

        if (!FormValidation.isRequired(referenceNumberTextView, Constants.Config.TEXT_MAXLEN)) ret = false;
        if (!FormValidation.isRequired(noteTextView, Constants.Config.TEXT_MAXLEN)) ret = false;
        return ret;
    }

    public void updatePaymentData(final JSONObject jsonObject) {

        if (getContext() == null)
            return;

        String url = Constants.Config.API_PATH + "/add_payment/";
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Boolean status = (Boolean) response.get("status");
                        if (status) {
                            Toast.makeText(getContext(), "Payment Submitted", Toast.LENGTH_SHORT).show();
                            capturePaymentLinearLayout.setEnabled(true);
                            paymentProgressBar.setVisibility(View.GONE);
                            mCallback.onMakePaymentFinish();
                            dismiss();
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
                    capturePaymentLinearLayout.setEnabled(true);
                    paymentProgressBar.setVisibility(View.GONE);
                    try {
                        jsonObject.put("API","update_payment_post_api");
                        VolleyService.handleVolleyError(error, jsonObject, true, getContext());
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

        jsonRequest.setTag(TAG);
        requestQueue.add(jsonRequest);
    }

    public void setupKeyboardHidingUI(View view) {
        if(!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard(v);
                    v.performClick();
                    return false;
                }
            });
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupKeyboardHidingUI(innerView);
            }
        }
    }

    void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive())
            imm.hideSoftInputFromWindow(v.getWindowToken() , 0);
    }
}

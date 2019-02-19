package com.truebil.crm.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.crm.Activities.UserProfileActivity;
import com.truebil.crm.Constants;
import com.truebil.crm.Network.VolleyService;
import com.truebil.crm.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CreateAppointmentDialogFragment extends DialogFragment {

    //Variable
    public static final String TAG = "CreateAppointmentDialogFragment";
    //Other
    private RequestQueue queue;
    private SharedPreferences sharedPref;
    //Views
    private TextView errorTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.dialog_fragment_create_appointment, container, false);

        sharedPref = getContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        ImageButton closeImageButton = rootView.findViewById(R.id.dialog_fragment_create_appointment_close_image_button);
        closeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        final EditText customerNameEditText = rootView.findViewById(R.id.dialog_fragment_create_appointment_customer_name_edit_text);
        final EditText customerNumberEditText = rootView.findViewById(R.id.dialog_fragment_create_appointment_customer_number_edit_text);

        LinearLayout saveAppointmentLinearLayout = rootView.findViewById(R.id.dialog_fragment_create_appointment_linear_layout);
        saveAppointmentLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = customerNameEditText.getText().toString();
                String phoneNumber = customerNumberEditText.getText().toString();
                createNewUser(userName, phoneNumber);
            }
        });

        errorTextView = rootView.findViewById(R.id.dialog_fragment_create_appointment_error_text_view);
        errorTextView.setVisibility(View.GONE);

        if (getArguments() != null) {
            String userPhone = getArguments().getString("USER_PHONE");
            customerNumberEditText.setText(userPhone);
        }

        return rootView;
    }

    void createNewUser(String userName, String phoneNumber) {

        if (getActivity() == null) return;

        if (userName.isEmpty() || phoneNumber.isEmpty()) {
            errorTextView.setText("Empty Values");
            return;
        }
        else {
            errorTextView.setVisibility(View.GONE);
        }

        String url = Constants.Config.API_PATH + "/buyer_create/";
        queue = Volley.newRequestQueue(getActivity());

        final JSONObject params = new JSONObject();
        try {
            params.put("mobile", phoneNumber);
            params.put("name", userName);
            params.put("source_id", "7"); // Hard coded string 7 to indicate that user was created through CRM App
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, params,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    parseCreateUserResponse(response);
                    errorTextView.setVisibility(View.GONE);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        params.put("API","create_new_user_post_api");
                        VolleyService.handleVolleyError(error,params,true, getActivity());
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
        jsonRequest.setTag(TAG);
        queue.add(jsonRequest);
    }

    void parseCreateUserResponse(JSONObject response) {
        try {
            if (!response.getBoolean("status"))
                return;

            int buyerId = response.getInt("buyer_id");
            startUserProfileActivity(buyerId);
            Toast.makeText(getContext(), "Appointment Created", Toast.LENGTH_SHORT).show();
            dismiss();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void startUserProfileActivity(int userId) {
        Bundle bundle = new Bundle();
        bundle.putString("BUYER_ID", String.valueOf(userId));
        Intent intent = new Intent(getContext(), UserProfileActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (queue != null)
            queue.cancelAll(TAG);
    }
}

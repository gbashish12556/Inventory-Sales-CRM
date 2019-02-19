package com.truebil.crm.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.crm.Constants;
import com.truebil.crm.Fragments.LoginFragment;
import com.truebil.crm.Fragments.OtpVerificationFragment;
import com.truebil.crm.Network.VolleyService;
import com.truebil.crm.R;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements LoginFragment.LoginFragmentListener, OtpVerificationFragment.OtpVerificationFragmentListener{

    public FragmentManager fragmentManager;
    LoginFragment loginFragment;
    OtpVerificationFragment otpVerificationFragment;
    private String mobileNo;
    private SharedPreferences sharedPref;
    private static final String TAG = "LoginActivity";
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPref = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        fragmentManager = getSupportFragmentManager();
        loginFragment = new LoginFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.activity_login_frame_layout, loginFragment)
                .commit();
    }

    @Override
    public void requestMobileOTP(String mobile) {
        mobileNo = mobile;

        if (queue == null)
            queue = Volley.newRequestQueue(getApplicationContext());

        String url = Constants.Config.API_PATH + "/generate_otp/";
        final JSONObject apiParams = new JSONObject();
        try {
            apiParams.put("mobile", String.valueOf(mobile));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonRequest = new JsonObjectRequest(url, apiParams,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("response", String.valueOf(response));
                    parseLoginResponse(response);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        apiParams.put("API","request_mobile_otp_api");
                        VolleyService.handleVolleyError(error, apiParams, false, LoginActivity.this);
                        NetworkResponse response = error.networkResponse;
                        JSONObject object = new JSONObject(new String(response.data));
                        loginFragment.sentLoginError(object.getString("message"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        );

        jsonRequest.setTag(TAG);
        queue.add(jsonRequest);
    }

    void parseLoginResponse(JSONObject response) {

        try {
            boolean status = response.getBoolean("status");
            String message = response.getString("message");

            if (status) {
                otpVerificationFragment = new OtpVerificationFragment();
                Bundle bundle = new Bundle();
                bundle.putString("mobile",mobileNo);
                otpVerificationFragment.setArguments(bundle);
                fragmentManager.beginTransaction()
                        .replace(R.id.activity_login_frame_layout, otpVerificationFragment)
                        .commit();
            }
            else {
                loginFragment.sentLoginError(message);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void verifyOtp(String mobile, String otp) {

        if (queue != null)
            queue = Volley.newRequestQueue(getApplicationContext());

        String url = Constants.Config.API_PATH + "/verify_otp/";

        final JSONObject apiParams = new JSONObject();
        try {
            apiParams.put("mobile", String.valueOf(mobile));
            apiParams.put("otp", String.valueOf(otp));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(url, apiParams,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    parseOtpVerificationResponse(response);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        apiParams.put("API","request_mobile_otp_api");
                        VolleyService.handleVolleyError(error, apiParams, false, LoginActivity.this);
                        NetworkResponse response = error.networkResponse;
                        JSONObject object = new JSONObject(new String(response.data));
                        otpVerificationFragment.sendOtpVerificationError(object.getString("message"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        );

        jsonRequest.setTag(TAG);
        queue.add(jsonRequest);
    }

    void parseOtpVerificationResponse(JSONObject response) {

        try {
            boolean status = response.getBoolean("status");
            String message = response.getString("message");

            if (status) {

                JSONObject userInfo = response.getJSONObject("user_info");
                int userId = userInfo.getInt("user_id");
                String userName = userInfo.getString("name");
                String userMobile = userInfo.getString("mobile");
                String token = response.getString("token");

                //Save info in shared preferences
                SharedPreferences.Editor editor = sharedPref.edit();

                editor.putString(Constants.SharedPref.JWT_TOKEN, token);
                editor.putInt(Constants.SharedPref.USER_ID, userId);
                editor.putString(Constants.SharedPref.USER_NAME, userName);
                editor.putString(Constants.SharedPref.USER_MOBILE, userMobile);
                editor.putBoolean(Constants.SharedPref.HAS_LOGGED_IN_BEFORE, true); //Set this for later checks.
                editor.apply();

                Intent intent = new Intent(this, SalesActivity.class);
                startActivity(intent);

                finish();
            }
            else {
                otpVerificationFragment.sendOtpVerificationError(message);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resendOtp(String mobile) {
        requestMobileOTP(mobile);
    }

    @Override
    public void onEditMobileButtonClicked() {
        loginFragment = new LoginFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.activity_login_frame_layout, loginFragment)
                .commit();
    }
}
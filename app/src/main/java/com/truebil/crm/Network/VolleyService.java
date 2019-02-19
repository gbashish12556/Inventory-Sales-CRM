package com.truebil.crm.Network;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.truebil.crm.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VolleyService {

    private static final String TAG = "VolleyService";
    private RequestQueue requestQueue;
    private static VolleyService instance = null;
    private SharedPreferences sharedPref;
    private Context context;

    private VolleyService(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
        sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
    }

    public static synchronized VolleyService getInstance(Context context) {
        if (instance == null)
            instance = new VolleyService(context);
        return instance;
    }

    public static synchronized VolleyService getInstance() {
        if (instance == null)
            throw new IllegalStateException(VolleyService.class.getSimpleName() + " is not initialized, call getInstance(...) first");
        return instance;
    }

    public void volleyRequest(final String apiSuffix, final int requestType, final JSONObject params, final VolleyInterface callback, String TAG) {

        String url = Constants.Config.API_PATH + apiSuffix;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(requestType, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (callback != null)
                    callback.onResult(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (callback != null)
                    callback.onError(error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String salesRepJWTToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");
                headers.put("Authorization", "jwt " + salesRepJWTToken);
                return headers;
            }
        };

        jsonObjectRequest.setTag(TAG);
        requestQueue.add(jsonObjectRequest);
    }

    public static String handleVolleyError(VolleyError error, JSONObject additionalLogData, boolean displayToast, Context context) {

        String toastMessage;

        /*
         * 1) Either time out or
         * 2) there is no connection or
         * 3) there was network error while performing the request
         */
        if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
            toastMessage = "Unstable internet Connection! Please check your connection.";
        }

        /*
         * 1) server responded with a error response or
         * 2) there was an Authentication Failure while performing the request or
         * 3) the server response could not be parsed
         */
        else if (error instanceof ServerError || error instanceof AuthFailureError || error instanceof ParseError) {
            toastMessage = getVolleyErrorMessage(error);
        }
        else {
            toastMessage = "Please retry";
        }

        logVolleyError(error, additionalLogData, context);

        if (displayToast)
            displayToast(toastMessage, context);

        return getVolleyErrorMessage(error);
    }

    private static String getVolleyErrorMessage(VolleyError error) {
        NetworkResponse response = error.networkResponse;
        if (response != null && response.data != null) {
            try {
                JSONObject obj = new JSONObject(new String(response.data));
                return obj.getString("message");
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "NULL";
    }

    private static int getVolleyErrorStatus(VolleyError error) {
        NetworkResponse response = error.networkResponse;
        if (response != null)
            return response.statusCode;
        return -1;
    }

    // Send info to Crashlytics along with user id, bid value, car id, etc.
    private static void logVolleyError(VolleyError error, JSONObject additionalLogData, @NonNull Context context ) {

        // Find status code, network response from VolleyError object
        int statusCode = getVolleyErrorStatus(error);
        String networkResponseMessage = getVolleyErrorMessage(error);

        // Get Dealer Id
        SharedPreferences sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        int userId = sharedPref.getInt(Constants.SharedPref.USER_ID, -1);

        StringBuilder logString = new StringBuilder();
        logString.append("Status Code: ").append(statusCode);
        logString.append(". Network Response: ").append(networkResponseMessage);
        logString.append(". USER_ID: ").append(userId);

        // Fill remaining information passed in function
        if (additionalLogData != null) {
            try {
                for (int i = 0; i < additionalLogData.names().length(); i++) {
                    String key = additionalLogData.names().getString(i);
                    String value = additionalLogData.getString(key);

                    logString.append(". ").append(key).append(": ").append(value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Log error and dispatch to Crashlytics
        Log.d(TAG, "Volley Error: " + logString.toString());

        if (userId != -1) {
            Crashlytics.setUserIdentifier(String.valueOf(userId));
        }

        Crashlytics.logException(new Exception(logString.toString()));
    }

    private static void displayToast(String toastString, Context context) {
        Toast.makeText(context, toastString, Toast.LENGTH_LONG).show();
    }
}
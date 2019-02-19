package com.truebil.crm.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.crm.Constants;
import com.truebil.crm.Fragments.UserSearchListFragment;
import com.truebil.crm.Network.VolleyService;
import com.truebil.crm.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SalesActivity extends AppCompatActivity {

    private UserSearchListFragment fragment;
    public static final String TAG = SalesActivity.class.getName();
    private RequestQueue queue;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);

        sharedPref = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        /*
         * Load the UserSearchListFragment by default whenever the activity launches
         * Pass parameters to differentiate bw Visits and Follow ups (USER_SEARCH_TYPE)
         */
        fragment = new UserSearchListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("USER_SEARCH_TYPE", "VISITS");
        fragment.setArguments(bundle);
        loadFragment(fragment);

        final TextView headerTextView = findViewById(R.id.activity_sales_header_text_view);
        headerTextView.setText("Appointments");

        TextView userDetailsTextView = findViewById(R.id.activity_sales_user_details_text_view);
        String salesRepName = sharedPref.getString(Constants.SharedPref.USER_NAME, "");
        String salesRepMobile = sharedPref.getString(Constants.SharedPref.USER_MOBILE, "");
        userDetailsTextView.setText(salesRepName + " | " + salesRepMobile);

        BottomNavigationView navigation = findViewById(R.id.activity_sales_bottom_navigation_view);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Bundle bundle = new Bundle();

                switch (item.getItemId()) {
                    case R.id.nav_visits:
                        fragment = new UserSearchListFragment();
                        bundle.putString("USER_SEARCH_TYPE", "VISITS");
                        fragment.setArguments(bundle);
                        headerTextView.setText("Appointments");
                        break;

                    case R.id.nav_followup:
                        fragment = new UserSearchListFragment();
                        bundle.putString("USER_SEARCH_TYPE", "FOLLOW_UP");
                        fragment.setArguments(bundle);
                        headerTextView.setText("Follow Up");
                        break;

                    default:
                        break;
                }

                return loadFragment(fragment);
            }
        });

        fetchConfig();
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_sales_frame_layout, fragment)
                .commit();

            return true;
        }
        return false;
    }

    void fetchConfig() {

        String url = Constants.Config.API_PATH + "/config/";

        if (queue == null)
            queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Boolean status = (Boolean) response.get("status");
                            if (status) {
                                parseConfig(response);
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
                        JSONObject additionalLogJson = new JSONObject();
                        try {
                            additionalLogJson.put("API", "/config (GET)");
                            VolleyService.handleVolleyError(error, additionalLogJson, false, getApplicationContext());
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

    public void parseConfig(JSONObject response){
        try {
            JSONObject data = response.getJSONObject("data");

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(Constants.Keys.LOCALITIES_LIST, String.valueOf(data.getJSONArray("localities")));
            editor.putString(Constants.Keys.RTO_LIST, String.valueOf(data.getJSONArray("rto_list")));
            editor.putString(Constants.Keys.BANKS, String.valueOf(data.getJSONArray("banks")));
            editor.putString(Constants.Keys.SALES_REPRESENTATIVE_LIST, String.valueOf(data.getJSONArray("city_sales_representatives_list")));
            editor.putString(Constants.Keys.FORM_INFO, String.valueOf(String.valueOf(data.getJSONObject("form_info"))));
            editor.putString(Constants.Keys.MAKES_LIST, String.valueOf(data.getJSONArray("makes")));
            editor.apply();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
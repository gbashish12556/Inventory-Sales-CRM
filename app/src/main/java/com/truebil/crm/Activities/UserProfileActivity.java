package com.truebil.crm.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;
import com.truebil.crm.Adapters.UserProfileFragmentAdapter;
import com.truebil.crm.Constants;
import com.truebil.crm.Network.VolleyService;
import com.truebil.crm.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private String buyerId;
    private RequestQueue queue;
    private static final String TAG = "UserProfileActivity";
    private TextView headerTextView, headerPhoneTextView;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        sharedPref = getApplicationContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        if (getIntent() != null) {
            buyerId = getIntent().getStringExtra("BUYER_ID");
        }
        else {
            Toast.makeText(getApplicationContext(), "Buyer ID not passed", Toast.LENGTH_SHORT).show();
        }

        UserProfileFragmentAdapter userProfileFragmentAdapter = new UserProfileFragmentAdapter(getSupportFragmentManager(), buyerId);
        ViewPager userProfileViewPager = findViewById(R.id.activity_user_profile_view_pager);
        userProfileViewPager.setAdapter(userProfileFragmentAdapter);

        TabLayout userProfileTabLayout = findViewById(R.id.activity_user_profile_tab_layout);
        userProfileTabLayout.setupWithViewPager(userProfileViewPager);

        ImageButton backImageButton = findViewById(R.id.activity_user_profile_back_image_button);
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        headerTextView = findViewById(R.id.activity_user_profile_header_text_view);
        headerPhoneTextView = findViewById(R.id.activity_user_profile_header_phone_text_view);
        headerTextView.setVisibility(View.GONE);
        headerPhoneTextView.setVisibility(View.GONE);

        ImageButton feedbackFlagImageButton = findViewById(R.id.activity_user_profile_feedback_image_button);
        feedbackFlagImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getApplicationContext(), v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.user_profile_menu, popup.getMenu());
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        Intent intent;
                        switch (item.getItemId()) {
                            case R.id.user_profile_menu_car_unavailable:
                                intent = new Intent(getApplicationContext(), FeedbackActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.user_profile_menu_requirement_mismatch:
                                intent = new Intent(getApplicationContext(), FeedbackActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.user_profile_menu_others:
                                intent = new Intent(getApplicationContext(), FeedbackActivity.class);
                                startActivity(intent);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (buyerId != null)
            displayUserInfo(buyerId);
    }

    void displayUserInfo(String buyerId) {

        String url = Constants.Config.API_PATH + "/buyer_info?buyer_id=" + buyerId;

        if (queue == null)
            queue = Volley.newRequestQueue(getApplicationContext());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parseUserInfo(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Volley Error: " + error.toString());

                JSONObject additionalLogJson = new JSONObject();
                try {
                    additionalLogJson.put("API", "/buyer_info (GET)");
                    VolleyService.handleVolleyError(error, additionalLogJson, false, getApplicationContext());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String salesRepToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");
                headers.put("Authorization", "jwt " + salesRepToken);
                return headers;
            }
        };

        jsonObjectRequest.setTag(TAG);
        queue.add(jsonObjectRequest);
    }

    void parseUserInfo(JSONObject response) {
        try {
            boolean status = response.getBoolean("status");
            if (!status) return;

            String mobile = response.getJSONObject("buyer_info").getString("mobile");
            String userName = response.getJSONObject("buyer_info").getString("name");

            fillUserInfo(userName, mobile);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void fillUserInfo(String userName, String mobile) {
        headerTextView.setText(userName + "'s Profile");
        headerTextView.setVisibility(View.VISIBLE);

        headerPhoneTextView.setText(mobile);
        headerPhoneTextView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (queue != null)
            queue.cancelAll(TAG);
    }
}

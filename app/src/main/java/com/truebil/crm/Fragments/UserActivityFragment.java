package com.truebil.crm.Fragments;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.crm.Constants;
import com.truebil.crm.Network.VolleyService;
import com.truebil.crm.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserActivityFragment extends Fragment {

    public static final String TAG = "UserActivityFragment";
    private RequestQueue queue;
    private String buyerId;
    private LinearLayout activitiesLinearLayout, emptyStateLinearLayout;
    private SharedPreferences sharedPref;
    private TextView emptyStateTextView;

    public UserActivityFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_actvity, container, false);
        sharedPref = getContext().getSharedPreferences("APP_PREFS", 0);

        activitiesLinearLayout = rootView.findViewById(R.id.fragment_user_activity_linear_layout);
        emptyStateLinearLayout = rootView.findViewById(R.id.item_empty_state_linear_layout);
        emptyStateTextView = rootView.findViewById(R.id.item_empty_state_header_text_view);

        if (getArguments() != null) {
            buyerId = getArguments().getString("BUYER_ID");
        } else {
            Toast.makeText(getContext(), "No Buyer ID passed for UserPreferenceFragment", Toast.LENGTH_SHORT).show();
            return rootView;
        }
        
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (activitiesLinearLayout.getChildCount() > 0)
                activitiesLinearLayout.removeAllViews();
            fetchUserActivities();
        }
    }

    public void fetchUserActivities() {

        String url = Constants.Config.API_PATH + "/buyer_activities?buyer_id=" + buyerId;

        if (getContext() == null)
            return;

        if (queue == null)
            queue = Volley.newRequestQueue(getContext());

        final JSONObject jsonObject = new JSONObject();
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parseUserActivityInfo(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    jsonObject.put("API","fetch_user_activities_get_api");
                    VolleyService.handleVolleyError(error,jsonObject,true, getActivity());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }) {
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

    void parseUserActivityInfo(JSONObject response) {
        try {
            if (!response.getBoolean("status"))
                return;

            JSONArray data = response.getJSONArray("activities");
            if (data.length() == 0) {
                emptyStateLinearLayout.setVisibility(View.VISIBLE);
                emptyStateTextView.setText(R.string.no_activity);
            }

            else {
                emptyStateLinearLayout.setVisibility(View.GONE);
                for (int i = 0; i < data.length(); i++) {

                    String date = data.getJSONObject(i).getString("date");
                    addDateToLinearLayout(date);

                    JSONArray activities = data.getJSONObject(i).getJSONArray("activities");

                    for (int j = 0; j < activities.length(); j++) {
                        Activity activity = new Activity(activities.getJSONObject(j));
                        addActivityToLinearLayout(activity);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class Activity {
        private String title;
        private String description;
        private String type;

        Activity(JSONObject response) {
            try {
                type = response.getString("type");
                description = response.getString("description");
                title = response.getString("title");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private String getTitle() {
            return title;
        }

        private String getDescription() {
            return description;
        }

        private String getType() {
            return type;
        }
    }

    void addDateToLinearLayout(String date) {
        TextView dateHeaderTextView = (TextView)getLayoutInflater().inflate(R.layout.item_date_header, activitiesLinearLayout, false);
        dateHeaderTextView.setText(date);
        activitiesLinearLayout.addView(dateHeaderTextView);
    }

    void addActivityToLinearLayout(Activity activity) {
        View activityView = getLayoutInflater().inflate(R.layout.item_user_activity, activitiesLinearLayout, false);
        ImageView activityIconImageView = activityView.findViewById(R.id.item_user_activity_icon_image_view);
        TextView activityHeadingTextView = activityView.findViewById(R.id.item_user_activity_heading_text_view);
        TextView activitySubHeadingTextView = activityView.findViewById(R.id.item_user_activity_sub_heading_text_view);

        activityHeadingTextView.setText(activity.getTitle());
        activitySubHeadingTextView.setText(activity.getDescription());
        activitiesLinearLayout.addView(activityView);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (queue != null)
            queue.cancelAll(TAG);
    }
}
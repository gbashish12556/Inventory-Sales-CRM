package com.truebil.crm.Fragments;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.crm.Adapter.BuyerListAdapter;
import com.truebil.crm.Constants;
import com.truebil.crm.Helper;
import com.truebil.crm.Models.BuyersListModel;
import com.truebil.crm.Network.VolleyService;
import com.truebil.crm.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserSearchListFragment extends Fragment {

    //Views
    private View noBuyerFoundLayout;
    private LinearLayout createNewAppointment;
    private ListView userListingListView;
    private ProgressBar progressBar;
    //Variables
    private static final String TAG = "UserSearchListFragment";
    private String searchType;
    private String nextUrl;
    private int preLast;
    private ArrayList<BuyersListModel> buyerDataList = new ArrayList<>();
    //Adapters
    private BuyerListAdapter buyerListAdapter;
    //Others
    private RequestQueue queue;
    private SharedPreferences sharedPref;

    public UserSearchListFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_user_search_list, container, false);

        if (getArguments() != null) {
            searchType = getArguments().getString("USER_SEARCH_TYPE");
        }

        // Disable automatic keyboard popup upon fragment start
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPref = getContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        buyerListAdapter = new BuyerListAdapter(getActivity(), R.layout.item_users_list_view, buyerDataList);
        createNewAppointment = rootView.findViewById(R.id.layout_create_appointment_linear_layout);
        noBuyerFoundLayout = rootView.findViewById(R.id.ragment_user_search_no_buyer_found_layout);
        progressBar = rootView.findViewById(R.id.fragment_user_search_list_progress_bar);
        noBuyerFoundLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        createNewAppointment.setVisibility(View.GONE); // Initially GONE, but enabled later based on getArgument and
        userListingListView = rootView.findViewById(R.id.fragment_user_search_list_view);
        userListingListView.setAdapter(buyerListAdapter);

        Helper.setupKeyboardHidingUI(rootView,getActivity());

        refreshVisitUserDetails();

        final ImageButton clearSearchImageButton = rootView.findViewById(R.id.fragment_user_search_clear_image_button);
        clearSearchImageButton.setVisibility(View.GONE);

        final EditText searchUserEditText = rootView.findViewById(R.id.fragment_user_search_edit_text);
        searchUserEditText.setCursorVisible(false);

        clearSearchImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Refresh UserSearchListFragment to default state
                refreshVisitUserDetails();

                // Hide the clear button
                clearSearchImageButton.setVisibility(View.GONE);

                // Clear the search edit text
                searchUserEditText.setText("");

                // Hide the cursor
                searchUserEditText.setCursorVisible(false);

                // Hide the keyboard
                hideKeyboard();
            }
        });

        searchUserEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String searchText = searchUserEditText.getText().toString();

                // 1. Handle the clear search button visibility
                if (searchText.length() > 0)
                    clearSearchImageButton.setVisibility(View.VISIBLE);
                else
                    clearSearchImageButton.setVisibility(View.GONE);

                // 2. Stop all previous Volley calls
                queue.cancelAll(TAG);

                // 3. Call GET apis to fetch search data
                String url = "";
                if (searchType.equals("VISITS")) {
                    url = Constants.Config.API_PATH + "/buyer_visits?mobile=" + searchText;
                }
                else if (searchType.equals("FOLLOW_UP")) {
                    url = Constants.Config.API_PATH + "/buyer_follow_ups?mobile=" + searchText;
                }

                // Clear the already filled list
                noBuyerFoundLayout.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                buyerDataList.clear();
                buyerListAdapter.notifyDataSetChanged();

                fetchUsers(url);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        searchUserEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUserEditText.setCursorVisible(true);
            }
        });

        createNewAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateAppointmentDialogFragment createAppointmentDialogFragment = new CreateAppointmentDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString("USER_PHONE", searchUserEditText.getText().toString());
                createAppointmentDialogFragment.setArguments(bundle);
                createAppointmentDialogFragment.show(getChildFragmentManager(),"CreateAppointmentDialogFragment");
            }
        });

        userListingListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                final int lastItem = firstVisibleItem + visibleItemCount;

                if (lastItem == totalItemCount) {
                    if (preLast != lastItem) { //to avoid multiple calls for last item
                        Log.d(TAG, "Reached end of list. Fetching more cars...");
                        preLast = lastItem;

                        if (nextUrl != null && !nextUrl.equals("null")) {
                            fetchUsers(nextUrl);
                        }
                    }
                }
            }
        });

        return rootView;
    }

    /*
     * Make GET call to fetch list of users which match the search string
     */
    void fetchUsers(String url) {

        if (getActivity() == null)
            return;

        queue = Volley.newRequestQueue(getActivity());
        JsonObjectRequest jsonRequest = new JsonObjectRequest(url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    parseUserSearchResponse(response);
                    progressBar.setVisibility(View.GONE);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("API","fetch_users_get_api");
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
                Log.d("dealerJWTToken",dealerJWTToken);
                headers.put("Authorization", "jwt " + dealerJWTToken);
                return headers;
            }
        };
        jsonRequest.setTag(TAG);
        queue.add(jsonRequest);
    }

    void parseUserSearchResponse(JSONObject response) {

        // Parsing begins
        try {
            //boolean status = response.getBoolean("status");
            //if (!status)
            //    return;

            nextUrl = response.getString("next");
            JSONArray userListJsonArray = response.getJSONArray("results");

            // Show "No Buyer Found" layout if zero results
            // are returned.
            if (userListJsonArray.length() == 0) {
                noBuyerFoundLayout.setVisibility(View.VISIBLE);
                userListingListView.setVisibility(View.GONE);

                if (searchType.equals("VISITS")) {
                    createNewAppointment.setVisibility(View.VISIBLE);
                }
                else if (searchType.equals("FOLLOW UP")) {
                    createNewAppointment.setVisibility(View.GONE);
                }
            }
            else {
                noBuyerFoundLayout.setVisibility(View.GONE);
                userListingListView.setVisibility(View.VISIBLE);
            }

            for (int i=0; i<userListJsonArray.length(); i++) {
                JSONObject userJson = userListJsonArray.getJSONObject(i);
                BuyersListModel buyersListModel = new BuyersListModel(userJson);
                buyerDataList.add(buyersListModel);
            }

            buyerListAdapter.notifyDataSetChanged();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void refreshVisitUserDetails() {
        progressBar.setVisibility(View.VISIBLE);
        buyerDataList.clear();
        buyerListAdapter.notifyDataSetChanged();

        // Display the recycler view and hide the "No Buyer Found" layout
        noBuyerFoundLayout.setVisibility(View.GONE);
        userListingListView.setVisibility(View.VISIBLE);

        String url = "";
        if (searchType.equals("VISITS")) {
            url = Constants.Config.API_PATH + "/buyer_visits/";
        }
        else if (searchType.equals("FOLLOW_UP")) {
            url = Constants.Config.API_PATH + "/buyer_follow_ups/";
        }
        fetchUsers(url);
    }

    /*
     * Extremely important to stop all volley calls otherwise the
     * Toast message in onErrorResponse may get invoked after the
     * fragment has been destroyed
     * Just make sure the call happens between onAttach(Activity activity) and onDetach()
     * otherwise there's no Activity attached and you'll get an Exception
     */
    @Override
    public void onStop() {
        super.onStop();
        queue.cancelAll(TAG);
    }

    void hideKeyboard() {
        if (getActivity() == null) return;
        View view = getActivity().getCurrentFocus();
        if (view == null || getContext() == null) return;
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}

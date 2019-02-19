package com.truebil.crm.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.truebil.crm.Activities.DedicatedActivity;
import com.truebil.crm.Constants;
import com.truebil.crm.Helper;
import com.truebil.crm.Models.FilterCarModel;
import com.truebil.crm.Models.ListingModel;
import com.truebil.crm.Network.VolleyService;
import com.truebil.crm.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddNewListingDialogFragment extends DialogFragment implements ModelFilterDialogFragment.ModelFilterFragmentInterface {

    private RequestQueue queue;
    private static final String TAG = "AddListingDialog";
    private ArrayList<ListingModel> listingModelList = new ArrayList<>();
    private int preLast;
    private String nextURL;
    private ListingAdapter listingAdapter;
    private AddNewListingFragmentInterface mCallback;
    private SharedPreferences sharedPref;
    private String buyerId;
    private ProgressBar loaderProgressBar;
    private ListView listingListView;
    private LinearLayout emptyStateLinearLayout;
    private Button carFilterButton;

    public AddNewListingDialogFragment() {
    }

    @Override
    public void onSelectModel(FilterCarModel carModel, String listingMake) {
        listingModelList.clear();
        Log.d(TAG, "onSelectModel: " + buyerId);
        String url = Constants.Config.API_PATH + "/listings/?buyer_id=" + buyerId + "&model_id=" + carModel.getId();
        listingListView.setVisibility(View.GONE);
        emptyStateLinearLayout.setVisibility(View.GONE);
        loaderProgressBar.setVisibility(View.VISIBLE);
        carFilterButton.setText(listingMake + " " + carModel.getName());
        fetchListings(url);
    }

    public interface AddNewListingFragmentInterface {
        void onAddNewListingButtonClick(ListingModel listingModel);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (AddNewListingFragmentInterface) context;
        }
        catch (ClassCastException e) {
            Log.d(TAG, "Calling class must implement AddNewListingFragmentInterface");
        }

        // Check if parent Fragment implements listener
        if (getParentFragment() instanceof AddNewListingFragmentInterface) {
            mCallback = (AddNewListingFragmentInterface) getParentFragment();
        } else {
            throw new RuntimeException("The parent fragment must implement AddNewListingFragmentInterface");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.dialog_fragment_add_listing, container, false);

        sharedPref = getContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (getArguments() != null) {
            buyerId = getArguments().getString("BUYER_ID");
        }
        else {
            Toast.makeText(getContext(), "No Buyer Id provided", Toast.LENGTH_SHORT).show();
        }

        emptyStateLinearLayout = rootView.findViewById(R.id.item_empty_state_linear_layout);
        emptyStateLinearLayout.setVisibility(View.GONE);
        carFilterButton = rootView.findViewById(R.id.dialog_fragment_add_listing_car_filter_button);
        loaderProgressBar = rootView.findViewById(R.id.dialog_fragment_add_listing_progress_bar);
        loaderProgressBar.setVisibility(View.VISIBLE);

        ImageButton closeAddListingDialogFragment = rootView.findViewById(R.id.dialog_fragment_add_listing_close_image_button);
        closeAddListingDialogFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        final ImageButton clearSearchImageButton = rootView.findViewById(R.id.dialog_fragment_add_listing_clear_image_button);
        clearSearchImageButton.setVisibility(View.GONE);

        final EditText searchListingEditText = rootView.findViewById(R.id.dialog_fragment_add_listing_search_edit_text);
        searchListingEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Clear listingModelList and notifyDatasetChanged.
                listingModelList.clear();
                listingAdapter.notifyDataSetChanged();

                String url = Constants.Config.API_PATH + "/listings/?number=" + s.toString().toUpperCase();
                fetchListings(url);

                clearSearchImageButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        clearSearchImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Refresh UserSearchListFragment to default state
                refreshListings();

                // Hide the clear button
                clearSearchImageButton.setVisibility(View.GONE);

                // Clear the search edit text
                searchListingEditText.setText("");

                // Hide the cursor
                searchListingEditText.setCursorVisible(false);

                // Hide the keyboard
                hideKeyboard();
            }
        });

        listingAdapter = new ListingAdapter(getContext(), R.layout.item_car_listing, listingModelList);
        listingListView = rootView.findViewById(R.id.dialog_fragment_add_listing_list_view);
        listingListView.setAdapter(listingAdapter);

        listingListView.setOnScrollListener(new AbsListView.OnScrollListener() {
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

                        if (nextURL != null && !nextURL.equals("null")) {
                            fetchListings(nextURL);
                        }
                    }
                }
            }
        });

        carFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getChildFragmentManager().beginTransaction().addToBackStack(null);
                DialogFragment dialogFragment = new ModelFilterDialogFragment();
                dialogFragment.show(ft, "Model Dialog");
                loaderProgressBar.setVisibility(View.GONE);
            }
        });

        refreshListings();

        return rootView;
    }

    void refreshListings() {
        listingModelList.clear();
        listingAdapter.notifyDataSetChanged();

        String url = Constants.Config.API_PATH + "/listings?buyer_id=" + buyerId;
        fetchListings(url);
    }

    void fetchListings(String url) {

        if (getActivity() == null)
            return;

        if (queue != null)
            queue.cancelAll(TAG);

        if (queue == null)
            queue = Volley.newRequestQueue(getActivity());

        final JSONObject jsonObject = new JSONObject();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parseListingResponse(response);
                listingListView.setVisibility(View.VISIBLE);
                loaderProgressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    jsonObject.put("API","fetch_listings_get_api");
                    VolleyService.handleVolleyError(error,jsonObject,true, getActivity());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                loaderProgressBar.setVisibility(View.GONE);
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

        jsonObjectRequest.setTag(TAG);
        queue.add(jsonObjectRequest);
    }

    void parseListingResponse(JSONObject response) {
        try {
            nextURL = response.getString("next");
            JSONArray results = response.getJSONArray("results");

            if (results.length() == 0) {
                emptyStateLinearLayout.setVisibility(View.VISIBLE);
            }

            else {
                for (int i = 0; i < results.length(); i++) {
                    JSONObject listingJson = results.getJSONObject(i);
                    ListingModel listingModel = new ListingModel(listingJson);
                    listingModelList.add(listingModel);
                }
                emptyStateLinearLayout.setVisibility(View.GONE);
                listingAdapter.notifyDataSetChanged();
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class ListingAdapter extends ArrayAdapter<ListingModel> {

        Context context;

        ListingAdapter(@NonNull Context context, int resource, ArrayList<ListingModel> data) {
            super(context, resource, data);
            this.context = context;
        }

        @NonNull @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            final ListingModel listingModel = getItem(position);

            if (listingModel != null) {

                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.item_car_listing, parent, false);
                }

                ImageView showcaseImageView = convertView.findViewById(R.id.item_car_listing_image_view);
                TextView manufacturingYearTextView = convertView.findViewById(R.id.item_car_listing_manufacturing_year_text_view);
                TextView variantNameTextView = convertView.findViewById(R.id.item_car_listing_variant_name_text_view);
                TextView fuelTypeTextView = convertView.findViewById(R.id.item_car_listing_fuel_type_text_view);
                TextView mileageTextView = convertView.findViewById(R.id.item_car_listing_mileage_text_view);
                TextView ownerDetailsTextView = convertView.findViewById(R.id.item_car_listing_owner_details_text_view);
                TextView offerCountTextView = convertView.findViewById(R.id.item_car_listing_offer_count_text_view);
                TextView testdriveCountTextView = convertView.findViewById(R.id.item_car_listing_testdrive_count_text_view);
                TextView listingPriceTextView = convertView.findViewById(R.id.item_car_listing_price_text_view);
                ImageButton addCarImageButton = convertView.findViewById(R.id.item_car_listing_add_image_button);
                //TextView visitDateTextView = convertView.findViewById(R.id.item_car_listing_visit_date_text_view);
                TextView userListingStatusTextView = convertView.findViewById(R.id.item_car_listing_user_status_text_view);

                Picasso.with(context).load(listingModel.getImageUrl()).into(showcaseImageView);
                manufacturingYearTextView.setText(listingModel.getManufacturingYear());
                variantNameTextView.setText(listingModel.getVariantName());
                fuelTypeTextView.setText(listingModel.getFuelType());
                mileageTextView.setText(listingModel.getMileage() + " Km");
                ownerDetailsTextView.setText(listingModel.getOwnerDetails());
                offerCountTextView.setText(listingModel.getOfferCount() + " Offers");
                testdriveCountTextView.setText(listingModel.getTestDriveCount() + " Testdrives");
                listingPriceTextView.setText("₹" + Helper.round((double) listingModel.getListingPrice()/100000, 2) + "L");
                //visitDateTextView.setVisibility(View.GONE);
                userListingStatusTextView.setVisibility(View.GONE);

                addCarImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO: Add car to PreferenceFragment
                        // TODO: Make POST call to save this listing as an interested car for user.
                        saveUserInterestedListing(listingModel);
                    }
                });

                showcaseImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), DedicatedActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("LISTING_ID", String.valueOf(listingModel.getListingId()));
                        bundle.putString("BUYER_ID", "43531");
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });
            }

            return convertView;
        }
    }

    void saveUserInterestedListing(final ListingModel listingModel) {

        String url = Constants.Config.API_PATH + "/add_buyer_visit_listing/";

        if (getActivity() == null)
            return;

        if (queue == null)
            queue = Volley.newRequestQueue(getActivity());

        /*
         * This POST call requires listing_id and buyer_visit_id as parameters
         */
        final JSONObject apiParams = new JSONObject();
        try {
            apiParams.put("listing_id", listingModel.getListingId());
            apiParams.put("buyer_id", buyerId);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, apiParams, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parseAddInterestedListingResponse(response, listingModel);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    apiParams.put("API","save_user_interested_listing_post_api");
                    VolleyService.handleVolleyError(error,apiParams,true, getActivity());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
        queue.add(jsonObjectRequest);
    }

    void parseAddInterestedListingResponse(JSONObject response, ListingModel listingModel) {

        try {
            if (!response.getBoolean("status"))
                return;

            String status = response.getJSONObject("data").getJSONObject("status").getString("display");
            String statusColor = response.getJSONObject("data").getJSONObject("status").getString("color");
            int buyerVisitListingId = response.getJSONObject("data").getInt("buyer_visit_listing_id");
            JSONArray menuOptions = response.getJSONObject("data").getJSONArray("options");
            int salesRepresentativeId = response.getJSONObject("data").getInt("sales_representative_id");

            listingModel.setStatus(status);
            listingModel.setStatusColor(statusColor);
            listingModel.setBuyerVisitListingId(buyerVisitListingId);
            listingModel.setMenuOptions(menuOptions);
            listingModel.setSalesRepresentativeId(salesRepresentativeId);

            Toast.makeText(getContext(), "Listing Added", Toast.LENGTH_SHORT).show();
            mCallback.onAddNewListingButtonClick(listingModel);
            getDialog().dismiss();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (queue != null)
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

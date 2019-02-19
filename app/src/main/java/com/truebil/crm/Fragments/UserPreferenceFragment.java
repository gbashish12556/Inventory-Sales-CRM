package com.truebil.crm.Fragments;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.squareup.picasso.Picasso;
import com.truebil.crm.Activities.DealTermsActivity;
import com.truebil.crm.Activities.DedicatedActivity;
import com.truebil.crm.Activities.DeliveryActivity;
import com.truebil.crm.Activities.FeedbackActivity;
import com.truebil.crm.Activities.PaymentActivity;
import com.truebil.crm.Activities.DealCancellationActivity;
import com.truebil.crm.Constants;
import com.truebil.crm.Controllers.ListingController;
import com.truebil.crm.Controllers.AddListingInterface;
import com.truebil.crm.Controllers.ReplaceRepresentativeInterface;
import com.truebil.crm.Helper;
import com.truebil.crm.Models.ListingModel;
import com.truebil.crm.Network.VolleyService;
import com.truebil.crm.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserPreferenceFragment extends Fragment implements AddNewListingDialogFragment.AddNewListingFragmentInterface {

    private LinearLayout interestedCarsLinearLayout, recommendedCarsLinearLayout, superLinearLayout;
    private RequestQueue queue;
    private SharedPreferences sharedPref;
    private static final String TAG = "UserPreferenceFragment";
    private String buyerId;
    private ProgressBar preferencesProgressBar;
    private TextView noInterestedCarsTextView, noRecommendedCarsTextView;
    private Button addAnotherCarButton;

    public UserPreferenceFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_preference, container, false);
        sharedPref = getContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        if (getArguments() != null) {
            buyerId = getArguments().getString("BUYER_ID");
        }
        else {
            Toast.makeText(getContext(), "No Buyer ID passed for UserPreferenceFragment", Toast.LENGTH_SHORT).show();
        }

        superLinearLayout = rootView.findViewById(R.id.fragment_user_preferences_super_linear_layout);
        interestedCarsLinearLayout = rootView.findViewById(R.id.fragment_user_preference_interested_cars_linear_layout);
        recommendedCarsLinearLayout = rootView.findViewById(R.id.fragment_user_preference_recommended_cars_linear_layout);
        addAnotherCarButton = rootView.findViewById(R.id.fragment_user_preference_add_car_button);
        preferencesProgressBar = rootView.findViewById(R.id.fragment_user_preferences_progress_bar);
        noInterestedCarsTextView = rootView.findViewById(R.id.fragment_user_preferences_no_interest_text_view);
        noRecommendedCarsTextView = rootView.findViewById(R.id.fragment_user_preferences_no_recommendation_text_view);

        hideNoResultsTextViews();

        addAnotherCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getChildFragmentManager().beginTransaction().addToBackStack(null);
                DialogFragment dialogFragment = new AddNewListingDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString("BUYER_ID", buyerId);
                dialogFragment.setArguments(bundle);
                dialogFragment.show(ft, "dialog");
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        showLoadingProgress();
        fetchUserPreferences();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ListingController.getInstance(getContext());
    }

    void fetchUserPreferences() {

        if (interestedCarsLinearLayout.getChildCount() > 0)
            interestedCarsLinearLayout.removeAllViews();

        if (recommendedCarsLinearLayout.getChildCount() > 0)
             recommendedCarsLinearLayout.removeAllViews();

        if (getActivity() == null)
            return;

        if (queue == null)
            queue = Volley.newRequestQueue(getActivity());

        String url = Constants.Config.API_PATH + "/buyer_preferences?buyer_id=" + buyerId;

        final JSONObject jsonObject = new JSONObject();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, jsonObject,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    hideLoadingProgress();
                    parseUserPreferences(response);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hideLoadingProgress();
                    try {
                        jsonObject.put("API","fetch_user_preference_get_api");
                        VolleyService.handleVolleyError(error,jsonObject,true, getActivity());
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        ){
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

    void parseUserPreferences(JSONObject response) {
        try {
            boolean status = response.getBoolean("status");
            if (!status) return;

            JSONArray testDrivenCarsJsonArray = response.getJSONObject("data").getJSONArray("test_driven_cars");
            for (int i=0; i<testDrivenCarsJsonArray.length(); i++) {
                JSONObject listingJson = testDrivenCarsJsonArray.getJSONObject(i);
                ListingModel listingModel = new ListingModel(listingJson);
                addListingToLinearLayout(listingModel, interestedCarsLinearLayout);
            }

            JSONArray recommendedCarsJsonArray = response.getJSONObject("data").getJSONArray("recommended_cars");
            for (int i=0; i<recommendedCarsJsonArray.length(); i++) {
                JSONObject listingJson = recommendedCarsJsonArray.getJSONObject(i);
                ListingModel listingModel = new ListingModel(listingJson);
                addListingToLinearLayout(listingModel, recommendedCarsLinearLayout);
            }

            // Display No Interested Cars text
            if (testDrivenCarsJsonArray.length() == 0) {
                noInterestedCarsTextView.setVisibility(View.VISIBLE);
                interestedCarsLinearLayout.setVisibility(View.GONE);
            }
            else {
                noInterestedCarsTextView.setVisibility(View.GONE);
                interestedCarsLinearLayout.setVisibility(View.VISIBLE);
            }

            // Display No Recommended Cars text
            if (recommendedCarsJsonArray.length() == 0) {
                noRecommendedCarsTextView.setVisibility(View.VISIBLE);
                recommendedCarsLinearLayout.setVisibility(View.GONE);
            }
            else {
                noRecommendedCarsTextView.setVisibility(View.GONE);
                recommendedCarsLinearLayout.setVisibility(View.VISIBLE);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void addListingToLinearLayout(final ListingModel listingModel, LinearLayout linearLayout) {

        if (getContext() == null)
            return;

        View listingView = LayoutInflater.from(getContext()).inflate(R.layout.item_car_listing, linearLayout, false);

        ImageView showcaseImageView = listingView.findViewById(R.id.item_car_listing_image_view);
        TextView manufacturingYearTextView = listingView.findViewById(R.id.item_car_listing_manufacturing_year_text_view);
        TextView variantNameTextView = listingView.findViewById(R.id.item_car_listing_variant_name_text_view);
        TextView fuelTypeTextView = listingView.findViewById(R.id.item_car_listing_fuel_type_text_view);
        TextView mileageTextView = listingView.findViewById(R.id.item_car_listing_mileage_text_view);
        TextView ownerDetailsTextView = listingView.findViewById(R.id.item_car_listing_owner_details_text_view);
        TextView offerCountTextView = listingView.findViewById(R.id.item_car_listing_offer_count_text_view);
        TextView testdriveCountTextView = listingView.findViewById(R.id.item_car_listing_testdrive_count_text_view);
        TextView listingPriceTextView = listingView.findViewById(R.id.item_car_listing_price_text_view);
        ImageButton addCarImageButton = listingView.findViewById(R.id.item_car_listing_add_image_button);
        TextView userListingStatusTextView = listingView.findViewById(R.id.item_car_listing_user_status_text_view);
        LinearLayout listingInfoLinearLayout = listingView.findViewById(R.id.item_car_listing_info_linear_layout);
        ImageButton menuDropDownButton = listingView.findViewById(R.id.item_car_listing_menu_dropdown_button);

        Picasso.with(getContext()).load(listingModel.getImageUrl()).into(showcaseImageView);
        manufacturingYearTextView.setText(listingModel.getManufacturingYear());
        variantNameTextView.setText(listingModel.getVariantName());
        fuelTypeTextView.setText(listingModel.getFuelType());
        mileageTextView.setText(listingModel.getMileage() + " Km");
        ownerDetailsTextView.setText(listingModel.getOwnerDetails());
        offerCountTextView.setText(listingModel.getOfferCount() + " Offers");
        testdriveCountTextView.setText(listingModel.getTestDriveCount() + " Testdrives");
        listingPriceTextView.setText("₹" + Helper.round((double) listingModel.getListingPrice()/100000, 2) + "L");

        if (listingModel.getStatus() == null)
            userListingStatusTextView.setVisibility(View.GONE);
        else {
            userListingStatusTextView.setVisibility(View.VISIBLE);
            userListingStatusTextView.setText(listingModel.getStatus());
            userListingStatusTextView.setBackgroundColor(Color.parseColor(listingModel.getStatusColor()));
        }

        // Recommended Car
        if (listingModel.getMenuOptions() == null) {
            menuDropDownButton.setVisibility(View.GONE);
            addCarImageButton.setVisibility(View.VISIBLE);
        }
        // Test Driven Car
        else {
            addCarImageButton.setVisibility(View.GONE);
            if (listingModel.getMenuOptions().length() == 0)
                menuDropDownButton.setVisibility(View.GONE);
            else
                menuDropDownButton.setVisibility(View.VISIBLE);
        }

        menuDropDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 * If menu options are sent from the api, display as is.
                 * Note: Menu option are sent even for Make Offer cars but with a null buyer_visit_listing_id
                 */
                if (listingModel.getMenuOptions() != null)
                    createPopupMenu(listingModel, v);
            }
        });

        addCarImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addListing(listingModel);
            }
        });

        showcaseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), DedicatedActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("LISTING_ID", String.valueOf(listingModel.getListingId()));
                bundle.putString("BUYER_ID", buyerId);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        showcaseImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Check if current sales rep ID is different than the assigned sales rep ID
                int loggedInSalesRepId = sharedPref.getInt(Constants.SharedPref.USER_ID, -1);
                if (listingModel.getSalesRepresentativeId() != -1 &&
                    loggedInSalesRepId != -1 &&
                    listingModel.getSalesRepresentativeId() != loggedInSalesRepId) {
                    createSalesRepSwitchDialog(listingModel.getBuyerVisitListingId());
                }
                else if (listingModel.getSalesRepresentativeId() == -1) {
                    createRepSwitchNotApplicableDialog("No Sales representative has been assigned to this buyer listing yet");
                }
                else if (listingModel.getSalesRepresentativeId() == loggedInSalesRepId) {
                    createRepSwitchNotApplicableDialog("You are already the sales representative for this buyer listing");
                }
                else {
                    createRepSwitchNotApplicableDialog("Sorry! Sales representatives cannot be switched for this buyer listing currently");
                }
                return true;
            }
        });

        // Make sure that the linearlayout is set to Visible on adding a listing
        linearLayout.setVisibility(View.VISIBLE);

        // Finally add the listing to the linearlayout
        linearLayout.addView(listingView);
    }

    void addListing(ListingModel listingModel) {
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
                parseConvertToTestDriven(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    apiParams.put("API","save_user_interested_listing_post_api");
                    VolleyService.handleVolleyError(error,apiParams,true, getActivity());
                }
                catch (JSONException e) {
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

    void createPopupMenu(final ListingModel listingModel, View view) {

        final int buyerVisitListingId = listingModel.getBuyerVisitListingId();
        final int listingId = listingModel.getListingId();
        final int salesRepresentativeId = listingModel.getSalesRepresentativeId();
        JSONArray menuOptions = listingModel.getMenuOptions();

        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        try {
            for (int i=0; i<menuOptions.length(); i++) {
                JSONObject menuOption = menuOptions.getJSONObject(i);
                String key = menuOption.getString("key");
                boolean isActive = menuOption.getBoolean("active");
                popupMenu.getMenu().add(Menu.NONE, i, i, key).setEnabled(isActive);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int loggedInSalesRepId = sharedPref.getInt(Constants.SharedPref.USER_ID, -1);

                /*
                 * If current representative is not same as the listing sales representative,
                 * then show a dialog box and request a switch.
                 * Both representatives need to be non-null.
                 */
                if (salesRepresentativeId != -1 &&
                        loggedInSalesRepId != -1 &&
                        salesRepresentativeId != loggedInSalesRepId) {
                    createSalesRepSwitchDialog(buyerVisitListingId);
                }

                /*
                 * Else let the current representative proceed. This may happen if the assigned
                 * representative is same as current rep or if either one of them is null.
                 */
                else {
                    if (item.getItemId() == 0 && item.getTitle().toString().toLowerCase().contains("add")) {
                        ListingController.addListingToTestDriven(String.valueOf(listingId), String.valueOf(buyerId), TAG, new AddListingInterface() {
                            @Override
                            public void onSuccess(JSONObject response) {
                                parseConvertToTestDriven(response);
                            }

                            @Override
                            public void onError(VolleyError error) {
                                try {
                                    JSONObject params = new JSONObject();
                                    params.put("LISTING_ID", String.valueOf(listingId));
                                    params.put("BUYER_ID", buyerId);
                                    params.put("API", "/add_listing (POST)");
                                    VolleyService.handleVolleyError(error, params, true, getContext());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    else {
                        Intent intent;
                        switch (item.getItemId()) {
                            case 0:
                                intent = new Intent(getContext(), FeedbackActivity.class);
                                break;
                            case 1:
                                intent = new Intent(getContext(), FeedbackActivity.class);
                                intent.putExtra("DETAILED_FEEDBACK_REQUIRED", true);
                                break;
                            case 2:
                                intent = new Intent(getContext(), DealTermsActivity.class);
                                break;
                            case 3:
                                intent = new Intent(getContext(), PaymentActivity.class);
                                break;
                            case 4:
                                intent = new Intent(getContext(), DeliveryActivity.class);
                                break;
                            case 5:
                                intent = new Intent(getContext(), DealCancellationActivity.class);
                                break;
                            default:
                                intent = new Intent(getContext(), FeedbackActivity.class); // Default case
                                break;
                        }
                        intent.putExtra("BUYER_VISIT_LISTING_ID", String.valueOf(buyerVisitListingId));
                        intent.putExtra("BUYER_ID", String.valueOf(buyerId));
                        startActivity(intent);
                    }
                }

                return true;
            }
        });
    }

    void createRepSwitchNotApplicableDialog(String message) {
        if (!this.isRemoving() && getActivity() != null) {
            /*
             * Display the AlertDialog only when the activity/fragment is NOT finishing.
             */
            new AlertDialog.Builder(getActivity())
                    .setTitle("Cannot Switch Representative")
                    .setMessage(message)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .create()
                    .show();
        }
    }

    void createSalesRepSwitchDialog(final int buyerVisitListingId) {
        if (!this.isRemoving() && getActivity() != null) {
            /*
             * Display the AlertDialog only when the activity/fragment is NOT finishing.
             */
            new AlertDialog.Builder(getActivity())
                    .setTitle("Replace Representative?")
                    .setMessage("Do you wish to assign yourself as the representative for this buyer listing?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            replaceSalesRepresentative(buyerVisitListingId, dialog);
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .create()
                    .show();
        }
    }

    void replaceSalesRepresentative(final int buyerVisitListingId, final DialogInterface dialog) {
        ListingController.replaceRepresentative(String.valueOf(buyerVisitListingId), TAG, new ReplaceRepresentativeInterface() {
            @Override
            public void onSuccess(JSONObject response) {
                Toast.makeText(getContext(), "Sales Representative Replaced", Toast.LENGTH_SHORT).show();
                dialog.cancel();
                showLoadingProgress();
                fetchUserPreferences();
            }

            @Override
            public void onError(VolleyError error) {
                dialog.cancel();
                try {
                    JSONObject params = new JSONObject();
                    params.put("BUYER_VISIT_LISTING_ID", buyerVisitListingId);
                    params.put("API", "/replace_sales_representative (GET)");
                    VolleyService.handleVolleyError(error, params,true, getContext());
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void showLoadingProgress() {
        preferencesProgressBar.setVisibility(View.VISIBLE);
        superLinearLayout.setVisibility(View.GONE);
        noInterestedCarsTextView.setVisibility(View.GONE);
        noRecommendedCarsTextView.setVisibility(View.GONE);
        addAnotherCarButton.setVisibility(View.GONE);
    }

    void hideLoadingProgress() {
        preferencesProgressBar.setVisibility(View.GONE);
        addAnotherCarButton.setVisibility(View.VISIBLE);
        superLinearLayout.setVisibility(View.VISIBLE);
    }

    void hideNoResultsTextViews() {
        noInterestedCarsTextView.setVisibility(View.GONE);
        noRecommendedCarsTextView.setVisibility(View.GONE);
    }

    @Override
    public void onAddNewListingButtonClick(ListingModel listingModel) {
        noInterestedCarsTextView.setVisibility(View.GONE);
        addListingToLinearLayout(listingModel, interestedCarsLinearLayout);
    }

    void parseConvertToTestDriven(JSONObject response) {

        try {
            if (!response.getBoolean("status"))
                return;

            Toast.makeText(getActivity(), "Listing Added", Toast.LENGTH_SHORT).show();
            interestedCarsLinearLayout.removeAllViews();
            recommendedCarsLinearLayout.removeAllViews();

            showLoadingProgress();
            fetchUserPreferences();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

package com.truebil.crm.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.truebil.crm.Adapters.GridViewAdapter;
import com.truebil.crm.Constants;
import com.truebil.crm.CustomLayouts.StaticExpandedGridView;
import com.truebil.crm.Helper;
import com.truebil.crm.Models.Features;
import com.truebil.crm.Network.VolleyService;
import com.truebil.crm.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CarDetailsFragment extends Fragment {

    private View rootView;
    private SharedPreferences sharedPref;
    private RequestQueue queue;
    private static final String TAG = "CarDetailsFragment";
    private CarDetailsFragmentInterface mCallback;
    private String listingId;
    private LinearLayout superLinearLayout;
    private ProgressBar loaderProgressBar;

    public CarDetailsFragment() {
    }

    public interface CarDetailsFragmentInterface {
        void OnListingInfoAvailable(String listingId, String listingName, String listingStatus);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (CarDetailsFragmentInterface)context;
        }
        catch (ClassCastException e) {
            Log.d(TAG, context.toString() + " must implement CarDetailsFragmentInterface");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_car_details, container, false);
        sharedPref = getContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        loaderProgressBar = rootView.findViewById(R.id.fragment_car_details_progress_bar);
        loaderProgressBar.setVisibility(View.VISIBLE);


        if (getArguments() != null) {
            listingId = getArguments().getString("LISTING_ID");
            fetchListingDetails(listingId);
        }

        superLinearLayout = rootView.findViewById(R.id.fragment_car_details_super_linear_layout);
        superLinearLayout.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (queue != null)
            queue.cancelAll(TAG);
    }

    void fetchListingDetails(String listingId) {

        String url = Constants.Config.API_PATH + "/listing?listing_id=" + listingId;

        if (getActivity() == null)
            return;

        if (queue == null)
            queue = Volley.newRequestQueue(getActivity());
        final JSONObject jo = new JSONObject();
        JsonObjectRequest jsonObject = new JsonObjectRequest(Request.Method.GET, url, jo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parseListingResponse(response);
                loaderProgressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    jo.put("API","fetch_listing_details_get_api");
                    VolleyService.handleVolleyError(error,jo,true, getActivity());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                loaderProgressBar.setVisibility(View.GONE);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String salesRepToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");
                headers.put("Authorization", "jwt " + salesRepToken);
                return headers;
            }
        };

        jsonObject.setTag(TAG);
        queue.add(jsonObject);
    }

    void parseListingResponse(JSONObject response) {

        // Show Super Linear Layout
        superLinearLayout.setVisibility(View.VISIBLE);

        // Create Model
        ListingModel listingModel = new ListingModel(response);

        fillListingOverview(listingModel);
        fillListingSummary(listingModel.getInspectionSummary());
        fillListingFeatures(listingModel.getFeatureJSONArray());
        fillInspectionReport(listingModel.getInspectionReport());

        // Send info back to activity to display listing info in Header
        mCallback.OnListingInfoAvailable(listingId, listingModel.getVariantName(), listingModel.getListingStatus());
    }

    void fillListingOverview(ListingModel listingModel) {
        ImageView listingImageView = rootView.findViewById(R.id.fragment_car_details_car_image_view);
        TextView listingStatusTextView = rootView.findViewById(R.id.fragment_car_details_car_status_text_view);
        TextView fuelTextView = rootView.findViewById(R.id.fragment_car_details_fuel_type_text_view);
        TextView transmissionTextView = rootView.findViewById(R.id.fragment_car_details_transmission_text_view);
        TextView insuranceTextView = rootView.findViewById(R.id.fragment_car_details_insurance_text_view);
        TextView insuranceValidityTextView = rootView.findViewById(R.id.fragment_car_details_insurance_validity_text_view);
        TextView rcTypeTextView = rootView.findViewById(R.id.fragment_car_details_rc_type_text_view);
        TextView registrationNoTextView = rootView.findViewById(R.id.fragment_car_details_registration_number_text_view);

        Picasso.with(getContext()).load(listingModel.getShowcaseImageURLList().get(0)).into(listingImageView);
        listingStatusTextView.setText(listingModel.getCarStatus());
        fuelTextView.setText(listingModel.getFuelType());
        transmissionTextView.setText(listingModel.getTransmissionType());
        rcTypeTextView.setText(listingModel.getRCType());
        insuranceTextView.setText("No Information");
        insuranceValidityTextView.setText("No Information");
        registrationNoTextView.setText("N.A.");

        if (listingModel.getRegistrationNumber() != null) {
            registrationNoTextView.setText(listingModel.getRegistrationNumber());
        }

        if (listingModel.getInstaveritasVerificationDetails() != null && listingModel.getInstaveritasVerificationDetails().length() != 0) {
            for (int i=0; i<listingModel.getInstaveritasVerificationDetails().length(); i++) {
                try {
                    JSONObject rtoVerifiedJson = listingModel.getInstaveritasVerificationDetails().getJSONObject(i);
                    String key = rtoVerifiedJson.names().get(0).toString();
                    if (key.equals("Insurance Validity")) {
                        insuranceValidityTextView.setText(rtoVerifiedJson.getString(key));
                    }
                    if (key.equals("Insurance Type")) {
                        insuranceTextView.setText(rtoVerifiedJson.getString(key));
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void fillListingSummary(JSONObject inspectionSummary) {
        JSONArray positiveInspectionComments = null;
        try {
            positiveInspectionComments = inspectionSummary.getJSONArray("positive_comments");
            JSONArray negativeInspectionComments = inspectionSummary.getJSONArray("negative_comments");
            Log.d(TAG, "Inspection summary: " + inspectionSummary);

            // Set summary
            LinearLayout summaryLinearLayout = rootView.findViewById(R.id.fragment_car_details_summary_linear_layout);
            for (int i=0; i<positiveInspectionComments.length(); i++) {
                View summaryItemView = getLayoutInflater().inflate(R.layout.item_inspection_report_comment, summaryLinearLayout, false);
                ImageView summaryItemImageView = summaryItemView.findViewById(R.id.item_inspection_report_comment_image_view);
                TextView summaryItemTextView = summaryItemView.findViewById(R.id.item_inspection_report_comment_text_view);
                summaryItemImageView.setImageResource(R.drawable.tick);
                summaryItemTextView.setText(positiveInspectionComments.getString(i));
                summaryLinearLayout.addView(summaryItemView);
            }
            for (int i=0; i<negativeInspectionComments.length(); i++) {
                View summaryItemView = getLayoutInflater().inflate(R.layout.item_inspection_report_comment, summaryLinearLayout, false);
                ImageView summaryItemImageView = summaryItemView.findViewById(R.id.item_inspection_report_comment_image_view);
                TextView summaryItemTextView = summaryItemView.findViewById(R.id.item_inspection_report_comment_text_view);
                summaryItemImageView.setImageResource(R.drawable.close_red);
                summaryItemTextView.setText(negativeInspectionComments.getString(i));
                summaryLinearLayout.addView(summaryItemView);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }

    void fillListingFeatures(JSONArray featureJSONArray) {
        Features features = new Features(getContext(), featureJSONArray);

        GridViewAdapter gridViewAdapter = new GridViewAdapter(getContext(), features.getActiveFeatures());
        final StaticExpandedGridView featuresGridView = rootView.findViewById(R.id.fragment_car_details_features_grid_view);
        featuresGridView.setAdapter(gridViewAdapter);
    }

    void fillInspectionReport(JSONObject report) {

        LinearLayout reportLinearLayout = rootView.findViewById(R.id.fragment_car_details_inspection_report_linear_layout);
        LayoutInflater inflater = getLayoutInflater();

        try {
            for (int i = 0; i < report.names().length(); i++) {
                Log.d(TAG, "key = " + report.names().getString(i) + " value = " + report.get(report.names().getString(i)));

                String reportHeader = report.names().getString(i);
                JSONObject correspondingReport = (JSONObject) report.get(report.names().getString(i));

                String reportRating = correspondingReport.getString("rating");
                JSONArray reportDetailsArray = correspondingReport.getJSONArray("details");

                Log.d(TAG, "Heading: " + reportHeader);
                Log.d(TAG, "Rating: " + reportRating);

                final View headerView = inflater.inflate(R.layout.item_inspection_report, reportLinearLayout, false);
                TextView headerTextView = headerView.findViewById(R.id.item_inspection_report_title_text_view);
                headerTextView.setText(reportHeader);
                TextView headerRatingTextView = headerView.findViewById(R.id.item_inspection_report_score_text_view);
                headerRatingTextView.setText(reportRating);
                final ImageButton animationImageButton = headerView.findViewById(R.id.item_inspection_report_animation_image_button);
                final LinearLayout detailsLinearLayout = headerView.findViewById(R.id.item_inspection_report_details_linear_layout);

                for (int j = 0; j < reportDetailsArray.length(); j++) {
                    JSONObject reportDetail = reportDetailsArray.getJSONObject(j);

                    for (int k = 0; k < reportDetail.names().length(); k++) {
                        String subReportHeading = reportDetail.names().getString(k);
                        JSONObject subReportDetails = (JSONObject) reportDetail.get(reportDetail.names().getString(k));

                        JSONArray images = subReportDetails.getJSONArray("images");
                        JSONArray positiveComments = subReportDetails.getJSONArray("positive_comments");
                        JSONArray negativeComments = subReportDetails.getJSONArray("negative_comments");
                        JSONArray neutralComments = new JSONArray();

                        if (subReportDetails.has("neutral_comments"))
                            neutralComments = subReportDetails.getJSONArray("neutral_comments");

                            /*
                            Log.d(TAG, "Subheading: " + subReportHeading);
                            Log.d(TAG, "images: " + images);
                            Log.d(TAG, "positive: " + positiveComments);
                            Log.d(TAG, "negative: " + negativeComments);
                            */

                        View subheaderLinearLayout = inflater.inflate(R.layout.item_inspection_report_subheader, reportLinearLayout, false);
                        TextView subheaderTextView = subheaderLinearLayout.findViewById(R.id.item_inspection_report_sub_header_text_view);
                        subheaderTextView.setText(subReportHeading);

                        detailsLinearLayout.addView(subheaderLinearLayout);

                        for (int l = 0; l < positiveComments.length(); l++) {
                            View commentView = inflater.inflate(R.layout.item_inspection_report_comment, reportLinearLayout, false);

                            TextView commentTextView = commentView.findViewById(R.id.item_inspection_report_comment_text_view);
                            commentTextView.setText(positiveComments.getString(l));

                            ImageView commentTickImageView = commentView.findViewById(R.id.item_inspection_report_comment_image_view);
                            commentTickImageView.setImageResource(R.drawable.tick);

                            detailsLinearLayout.addView(commentView);
                        }

                        for (int l = 0; l < neutralComments.length(); l++) {
                            View commentView = inflater.inflate(R.layout.item_inspection_report_comment, reportLinearLayout, false);

                            TextView commentTextView = commentView.findViewById(R.id.item_inspection_report_comment_text_view);
                            commentTextView.setText(neutralComments.getString(l));

                            ImageView commentTickImageView = commentView.findViewById(R.id.item_inspection_report_comment_image_view);
                            commentTickImageView.setImageResource(R.drawable.ic_tick_neutral);

                            detailsLinearLayout.addView(commentView);
                        }

                        for (int l = 0; l < negativeComments.length(); l++) {
                            View commentView = inflater.inflate(R.layout.item_inspection_report_comment, reportLinearLayout, false);

                            TextView commentTextView = commentView.findViewById(R.id.item_inspection_report_comment_text_view);
                            commentTextView.setText(negativeComments.getString(l));

                            ImageView commentTickImageView = commentView.findViewById(R.id.item_inspection_report_comment_image_view);
                            commentTickImageView.setImageResource(R.drawable.close_red);

                            detailsLinearLayout.addView(commentView);
                        }


                        View errorImagesLinearLayout = inflater.inflate(R.layout.item_inspection_report_error_images, detailsLinearLayout, false);

                        for (int l = 0; l < images.length(); l++) {
                            String imageUrl = "https:" + images.getJSONObject(l).getString("url");

                            if (l == 0) {
                                ImageView firstErrorImageView = errorImagesLinearLayout.findViewById(R.id.item_inspection_report_1_error_image_view);
                                Picasso.with(getContext()).load(imageUrl).into(firstErrorImageView);
                            } else if (l == 1) {
                                ImageView secondErrorImageView = errorImagesLinearLayout.findViewById(R.id.item_inspection_report_2_error_image_view);
                                Picasso.with(getContext()).load(imageUrl).into(secondErrorImageView);
                            } else if (l == 2) {
                                ImageView thirdErrorImageView = errorImagesLinearLayout.findViewById(R.id.item_inspection_report_3_error_image_view);
                                Picasso.with(getContext()).load(imageUrl).into(thirdErrorImageView);
                            } else {
                                TextView extraCountTextView = errorImagesLinearLayout.findViewById(R.id.item_inspection_report_extra_count_image_view);
                                extraCountTextView.setText("+" + String.valueOf(l - 2));
                            }
                        }

                        if (images.length() != 0)
                            detailsLinearLayout.addView(errorImagesLinearLayout);
                    }
                }

                reportLinearLayout.addView(headerView);

                // Assign expand-contract button
                detailsLinearLayout.setVisibility(View.GONE);
                headerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (detailsLinearLayout.getVisibility() == View.GONE) {
                            detailsLinearLayout.setVisibility(View.VISIBLE);
                            animationImageButton.setImageResource(R.drawable.chevron_up);
                        } else {
                            detailsLinearLayout.setVisibility(View.GONE);
                            animationImageButton.setImageResource(R.drawable.chevron_down);
                        }
                    }
                });
                animationImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        headerView.performClick();
                    }
                });

                if (i == 0) {
                    detailsLinearLayout.setVisibility(View.VISIBLE);
                    animationImageButton.setImageResource(R.drawable.chevron_up);
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class ListingModel {
        private String variantName,
                manufacturingYear,
                owner,
                fuelType,
                rtoName,
                mileage,
                cityName,
                truebilScore,
                carStatus,
                transmissionType,
                rcType,
                listingStatus,
                registrationNumber;

        private JSONObject inspectionReport,
                overview,
                inspectionSummary,
                listingApiResponse;

        private JSONArray refurbDetails,
                featureJSONArray,
                cargoNegativeComments,
                instaveritasVerificationDetails;

        private ArrayList<String> showcaseImageURLList;

        ListingModel(JSONObject apiResponse) {

            try {
                listingApiResponse = apiResponse;
                listingStatus = apiResponse.getString("listing_status");
                overview = apiResponse.getJSONObject("overview");
                inspectionReport = apiResponse.getJSONObject("inspection_report").getJSONObject("report");
                refurbDetails = apiResponse.getJSONObject("inspection_report").getJSONArray("refurb_details");
                JSONObject basicInfo = apiResponse.getJSONObject("basic_info");
                JSONObject sellerInfo = overview.getJSONObject("seller_info");
                JSONObject carInfo = overview.getJSONObject("car_info");
                String yearVariantName = basicInfo.getString("variant_name");
                manufacturingYear = yearVariantName.substring(0, yearVariantName.indexOf(' '));
                variantName = yearVariantName.substring(yearVariantName.indexOf(' ') + 1);
                owner = sellerInfo.getString("Owner");
                fuelType = carInfo.getString("Fuel");
                rtoName = carInfo.getString("RTO");
                mileage = basicInfo.getString("mileage");
                transmissionType = carInfo.getString("Transmission");
                rcType = carInfo.getString("RC Type");
                cityName = apiResponse.getJSONObject("city_details").getString("City Name");

                int interestedPeopleCount = apiResponse.getInt("interested_people_count");
                int testDriveCount = apiResponse.getInt("test_driven_count");
                carStatus = "This car has been test driven " + testDriveCount + " times. At least " + interestedPeopleCount + " people are interested in this car.";
                truebilScore = basicInfo.getString("rating");
                JSONArray showcaseImageURLs = basicInfo.getJSONArray("showcase_image_urls");
                showcaseImageURLList = new ArrayList<>();
                for (int i=0; i<showcaseImageURLs.length(); i++) {
                    showcaseImageURLList.add("https:" + showcaseImageURLs.getString(i));
                }
                featureJSONArray = apiResponse.getJSONArray("features");
                inspectionSummary = apiResponse.getJSONObject("inspection_report").getJSONObject("summary");
                cargoNegativeComments = new JSONArray();
                try {
                    cargoNegativeComments = apiResponse.getJSONObject("inspection_report").getJSONArray("listing_negative_comments");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                if (!apiResponse.isNull("instaveritas_verification_details")) {
                    instaveritasVerificationDetails = apiResponse.getJSONArray("instaveritas_verification_details");
                }
                registrationNumber = apiResponse.getString("registration_number");

            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private String getVariantName() {
            return variantName;
        }

        private String getManufacturingYear() {
            return manufacturingYear;
        }

        private String getOwner() {
            return owner;
        }

        private String getFuelType() {
            return fuelType;
        }

        private String getRtoName() {
            return rtoName;
        }

        private String getMileage() {
            return mileage;
        }

        private String getCityName() {
            return cityName;
        }

        private String getTruebilScore() {
            return truebilScore;
        }

        private String getCarStatus() {
            return carStatus;
        }

        private JSONObject getInspectionReport() {
            return inspectionReport;
        }

        private JSONObject getOverview() {
            return overview;
        }

        private JSONObject getInspectionSummary() {
            return inspectionSummary;
        }

        private JSONArray getRefurbDetails() {
            return refurbDetails;
        }

        private JSONArray getFeatureJSONArray() {
            return featureJSONArray;
        }

        private JSONArray getCargoNegativeComments() {
            return cargoNegativeComments;
        }

        private ArrayList<String> getShowcaseImageURLList() {
            return showcaseImageURLList;
        }

        private JSONObject getListingApiResponse() {
            return listingApiResponse;
        }

        private String getTransmissionType() {
            return transmissionType;
        }

        private String getRCType() {
            return rcType;
        }

        public String getListingStatus() {
            return listingStatus;
        }

        public JSONArray getInstaveritasVerificationDetails() {
            return instaveritasVerificationDetails;
        }

        public String getRegistrationNumber() {
            return registrationNumber;
        }
    }
}

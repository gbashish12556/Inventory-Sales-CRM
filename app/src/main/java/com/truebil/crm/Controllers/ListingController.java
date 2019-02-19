package com.truebil.crm.Controllers;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.truebil.crm.Network.VolleyInterface;
import com.truebil.crm.Network.VolleyService;

import org.json.JSONException;
import org.json.JSONObject;

public class ListingController {

    private static AddListingInterface controllerCallback;
    private static ListingController instance = null;
    private static Context context;

    private ListingController() {
    }

    public static void getInstance(AddListingInterface callback, Context ctx) {
        controllerCallback = callback;
        context = ctx;
        if (instance == null)
            instance = new ListingController();
    }

    public static void getInstance(Context ctx) {
        context = ctx;
        if (instance == null)
            instance = new ListingController();
    }

    public static void addListingToTestDriven(String listingId, String buyerId, String activityTag, final AddListingInterface addListingInterface) {

        VolleyInterface callback = new VolleyInterface() {
            @Override
            public void onResult(JSONObject response) {
                addListingInterface.onSuccess(response);
            }

            @Override
            public void onError(VolleyError error) {
                addListingInterface.onError(error);
            }
        };

        String apiSuffix = "/add_listing/";

        final JSONObject apiParams = new JSONObject();
        try {
            apiParams.put("listing_id", listingId);
            apiParams.put("buyer_id", buyerId);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        VolleyService.getInstance(context).volleyRequest(apiSuffix, Request.Method.POST, apiParams, callback, activityTag);
    }

    public static void replaceRepresentative(final String buyerVisitListingId, String activityTag, final ReplaceRepresentativeInterface replaceRepresentativeInterface) {

        VolleyInterface callback = new VolleyInterface() {
            @Override
            public void onResult(JSONObject response) {
                replaceRepresentativeInterface.onSuccess(response);
            }

            @Override
            public void onError(VolleyError error) {
                replaceRepresentativeInterface.onError(error);
            }
        };

        String apiSuffix = "/replace_sales_representative/";
        final JSONObject apiParams = new JSONObject();
        try {
            apiParams.put("buyer_visit_listing_id", buyerVisitListingId);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        VolleyService.getInstance(context).volleyRequest(apiSuffix, Request.Method.POST, apiParams, callback, activityTag);
    }

    public static void setOnCallbackResult(AddListingInterface callback) {
        controllerCallback = callback;
    }
}

package com.truebil.crm.Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ListingModel {
    private String manufacturingYear;
    private String variantName;
    private int buyerVisitListingId;
    private int listingId;
    private int mileage;
    private String fuelType;
    private String ownerDetails;
    private int offerCount;
    private int testDriveCount;
    private int listingPrice;
    private int salesRepresentativeId = -1;
    private String imageUrl;
    private String status;
    private String statusColor;
    private JSONArray menuOptions;

    public ListingModel(JSONObject response) {
        try {
            variantName = response.getString("name");
            manufacturingYear = response.getString("year");
            listingId = response.getInt("listing_id");
            mileage = response.getInt("mileage");
            ownerDetails = response.getString("owner");
            offerCount = response.getInt("offers");
            testDriveCount = response.getInt("test_driven");
            listingPrice = response.getInt("price");
            fuelType = response.getString("fuel_type");
            imageUrl = "https:" + response.getString("image_url");
            if (!response.isNull("status_test_driven")) {
                status = response.getJSONObject("status_test_driven").getString("display");
                statusColor = response.getJSONObject("status_test_driven").getString("color");
            }
            if (response.has("options"))
                menuOptions = response.getJSONArray("options");
            if (response.has("buyer_visit_listing_id") && !response.isNull("buyer_visit_listing_id"))
                buyerVisitListingId = response.getInt("buyer_visit_listing_id");

            if (!response.getString("status").equals("active")) {
                status = "Sold";
                statusColor = "#FF0000";
            }
            if (!response.isNull("sales_representative_id"))
                salesRepresentativeId = response.getInt("sales_representative_id");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getManufacturingYear() {
        return manufacturingYear;
    }

    public String getVariantName() {
        return variantName;
    }

    public int getListingId() {
        return listingId;
    }

    public int getMileage() {
        return mileage;
    }

    public String getOwnerDetails() {
        return ownerDetails;
    }

    public int getOfferCount() {
        return offerCount;
    }

    public int getTestDriveCount() {
        return testDriveCount;
    }

    public int getListingPrice() {
        return listingPrice;
    }

    public String getStatus() {
        return status;
    }

    public JSONArray getMenuOptions() {
        return menuOptions;
    }

    public int getBuyerVisitListingId() {
        return buyerVisitListingId;
    }

    public String getFuelType() {
        return fuelType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setBuyerVisitListingId(int buyerVisitListingId) {
        this.buyerVisitListingId = buyerVisitListingId;
    }

    public void setMenuOptions(JSONArray menuOptions) {
        this.menuOptions = menuOptions;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
    }

    public int getSalesRepresentativeId() {
        return salesRepresentativeId;
    }

    public void setSalesRepresentativeId(int salesRepresentativeId) {
        this.salesRepresentativeId = salesRepresentativeId;
    }
}

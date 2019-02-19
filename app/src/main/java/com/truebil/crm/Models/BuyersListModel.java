package com.truebil.crm.Models;

import org.json.JSONException;
import org.json.JSONObject;


public class BuyersListModel {

    private String visitDate,
            buyerName,
            buyerStatus,
            statusColor,
            salesRepresentativeName;
    private long buyerMobile;
    private int buyerId,
            buyerOffer = 0;

    public BuyersListModel(JSONObject jsonObject){
        try {
            buyerName = jsonObject.getString("name");
            buyerMobile = jsonObject.getLong("mobile");
            buyerId = jsonObject.getInt("buyer_id");
            if (jsonObject.has("visit_date"))
                visitDate = jsonObject.getString("visit_date");
            if (!jsonObject.isNull("status")) {
                JSONObject buyerStatusJson = jsonObject.getJSONObject("status");
                buyerStatus = buyerStatusJson.getString("display");
                statusColor = buyerStatusJson.getString("color");
            }
            if (jsonObject.has("offer")) {
                buyerOffer = jsonObject.getInt("offer");
            }
            if (!jsonObject.isNull("sales_representative_info") && !jsonObject.getJSONObject("sales_representative_info").isNull("name")) {
                salesRepresentativeName = jsonObject.getJSONObject("sales_representative_info").getString("name");
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getBuyerId() {
        return buyerId;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public long getBuyerMobile() {
        return buyerMobile;
    }

    public String getBuyerStatus() {
        return buyerStatus;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public int getBuyerOffer() {
        return buyerOffer;
    }

    public String getSalesRepresentativeName() {
        return salesRepresentativeName;
    }
}


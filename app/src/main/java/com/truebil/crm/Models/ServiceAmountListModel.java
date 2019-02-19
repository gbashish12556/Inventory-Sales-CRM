package com.truebil.crm.Models;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;

public class ServiceAmountListModel  implements Serializable{

    private String serviceName;
    private String serviceKey;
    private int serviceAmount = 0;
    private Boolean isSelected = false;
    private Boolean isInclusive = false;

    public ServiceAmountListModel(JSONObject jsonObject){
        try {
            serviceName = jsonObject.getString("name");

            if (!jsonObject.isNull("amount")) {
                serviceAmount = jsonObject.getInt("amount");
            }

            if (!jsonObject.isNull("is_selected")) {
                isSelected = jsonObject.getBoolean("is_selected");
            }

            if (!jsonObject.isNull("id")) {
                serviceKey = jsonObject.getString("id");
            }

            if (!jsonObject.isNull("is_inclusive")) {
                isInclusive = jsonObject.getBoolean("is_inclusive");
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public void setServiceAmount(int serviceAmount) {
        this.serviceAmount = serviceAmount;
    }

    public void setInclusive(Boolean inclusive) {
        isInclusive = inclusive;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getServiceAmount() {
        return serviceAmount;
    }

    public Boolean getIsSelected() {
        return isSelected;
    }

    public Boolean getInclusive() {
        return isInclusive;
    }
}
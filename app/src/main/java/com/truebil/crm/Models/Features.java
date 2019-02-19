package com.truebil.crm.Models;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Features {

    private List<FeatureItem> mSafetyFeatures = new ArrayList<>();
    private List<FeatureItem> mComfortFeatures = new ArrayList<>();
    private List<FeatureItem> mAccessoriesFeatures = new ArrayList<>();
    private List<FeatureItem> mActiveFeatures = new ArrayList<>();

    public Features(Context mContext, JSONArray features) {

        try {
            JSONObject safety = features.getJSONObject(0).getJSONObject("Safety");
            JSONObject comfort = features.getJSONObject(1).getJSONObject("Comfort");
            JSONObject accessories = features.getJSONObject(2).getJSONObject("Accessories");

            JSONArray activeSafety = safety.getJSONArray("Active");
            JSONArray inactiveSafety = safety.getJSONArray("Inactive");
            JSONArray activeComfort = comfort.getJSONArray("Active");
            JSONArray inactiveComfort = comfort.getJSONArray("Inactive");
            JSONArray activeAccessories = accessories.getJSONArray("Active");
            JSONArray inactiveAccessories = accessories.getJSONArray("Inactive");

            for (int i=0; i<activeSafety.length(); i++) {
                mSafetyFeatures.add(new FeatureItem(mContext, activeSafety.getString(i), true));
                mActiveFeatures.add(new FeatureItem(mContext, activeSafety.getString(i), true));
            }

            for (int i=0; i<inactiveSafety.length(); i++) {
                mSafetyFeatures.add(new FeatureItem(mContext, inactiveSafety.getString(i), false));
            }

            for (int i=0; i<activeComfort.length(); i++) {
                mComfortFeatures.add(new FeatureItem(mContext, activeComfort.getString(i), true));
                mActiveFeatures.add(new FeatureItem(mContext, activeComfort.getString(i), true));
            }

            for (int i=0; i<inactiveComfort.length(); i++) {
                mComfortFeatures.add(new FeatureItem(mContext, inactiveComfort.getString(i), false));
            }

            for (int i=0; i<activeAccessories.length(); i++) {
                mAccessoriesFeatures.add(new FeatureItem(mContext, activeAccessories.getString(i), true));
                mActiveFeatures.add(new FeatureItem(mContext, activeAccessories.getString(i), true));
            }

            for (int i=0; i<inactiveAccessories.length(); i++) {
                mAccessoriesFeatures.add(new FeatureItem(mContext, inactiveAccessories.getString(i), false));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<FeatureItem> getSafetyFeatures() {
        return mSafetyFeatures;
    }

    public List<FeatureItem> getComfortFeatures() {
        return mComfortFeatures;
    }

    public List<FeatureItem> getAccessoriesFeatures() {
        return mAccessoriesFeatures;
    }

    public List<FeatureItem> getActiveFeatures() {
        return mActiveFeatures;
    }
}
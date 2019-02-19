package com.truebil.crm.Models;

import android.content.Context;

public class FeatureItem {

    private String featureText;
    private int featureIcon;
    private boolean isActive;

    FeatureItem(Context mContext, String featureText, Boolean isActive) {
        this.featureText = featureText;
        this.isActive = isActive;

        String iconName = featureText.replace(' ', '_');
        iconName = iconName.replace('-', '_');
        iconName = iconName.toLowerCase();

        this.featureIcon = mContext.getResources().getIdentifier(iconName , "drawable", mContext.getPackageName());
    }

    public String getFeatureText() {
        return featureText;
    }

    public int getFeatureIcon() {
        return featureIcon;
    }

    public boolean isActive() {
        return isActive;
    }
}
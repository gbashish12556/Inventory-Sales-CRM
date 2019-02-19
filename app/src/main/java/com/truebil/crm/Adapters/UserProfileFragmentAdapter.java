package com.truebil.crm.Adapters;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.truebil.crm.Fragments.UserActivityFragment;
import com.truebil.crm.Fragments.UserPreferenceFragment;

public class UserProfileFragmentAdapter extends FragmentPagerAdapter {

    private String buyerId;

    public UserProfileFragmentAdapter(FragmentManager fm, String buyerId) {
        super(fm);
        this.buyerId = buyerId;
    }

    @Override
    public Fragment getItem(int position) {

        Bundle bundle = new Bundle();
        bundle.putString("BUYER_ID", buyerId);

        switch (position) {
            case 0:
                UserPreferenceFragment userPreferenceFragment = new UserPreferenceFragment();
                userPreferenceFragment.setArguments(bundle);
                return userPreferenceFragment;
            case 1:
                UserActivityFragment userActivityFragment = new UserActivityFragment();
                userActivityFragment.setArguments(bundle);
                return userActivityFragment;
            default:
                return null;
        }
    }

    @Nullable @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Preference";
            case 1:
                return "Activity";
            default:
                return "";
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}

package com.truebil.crm.Adapters;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.truebil.crm.Fragments.CarDetailsFragment;
import com.truebil.crm.Fragments.OfferDetailFragment;

public class DedicatedFragmentAdapter extends FragmentPagerAdapter {

    private String listingId;

    public DedicatedFragmentAdapter(FragmentManager fm, String listingId) {
        super(fm);
        this.listingId = listingId;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                CarDetailsFragment carDetailsFragment = new CarDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("LISTING_ID", listingId);
                carDetailsFragment.setArguments(bundle);
                return carDetailsFragment;
            case 1:
                OfferDetailFragment offerDetailFragment = new OfferDetailFragment();
                bundle = new Bundle();
                bundle.putString("LISTING_ID", listingId);
                offerDetailFragment.setArguments(bundle);
                return offerDetailFragment;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Car Details";
            case 1:
                return "Offer Details";
            default:
                return "";
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}

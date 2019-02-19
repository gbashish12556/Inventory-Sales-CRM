package com.truebil.crm.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.truebil.crm.Fragments.AllInvoiceFragment;
import com.truebil.crm.Fragments.DealTermsFragment;
import com.truebil.crm.Fragments.MakePaymentDialogFragment;
import com.truebil.crm.R;


public class PaymentActivity extends AppCompatActivity implements DealTermsFragment.DealTermsFragmentInterface, MakePaymentDialogFragment.MakePaymentDialogInterface{

    ViewPager viewPager;
    CustomPagerAdapter mCustomPagerAdapter;
    int buyerVisitListingId, buyerId;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        if (getIntent() != null && getIntent().getExtras() != null) {
            buyerVisitListingId = Integer.parseInt(getIntent().getExtras().getString("BUYER_VISIT_LISTING_ID"));
            buyerId = Integer.parseInt(getIntent().getExtras().getString("BUYER_ID"));
        }
        sharedPref = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        ImageView backArrowImageView = findViewById(R.id.activity_feedback_back_arrow);
        backArrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        viewPager = findViewById(R.id.deal_terms_activity_view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        mCustomPagerAdapter = new CustomPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mCustomPagerAdapter);
    }

    @Override
    public void onMakePaymentClick(int carPaymentPending, int servicesPaymentPending) {
        MakePaymentDialogFragment makePaymentDialogFragment = new MakePaymentDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("buyer_visit_listing_id", buyerVisitListingId);
        bundle.putInt("car_payment_pending", carPaymentPending);
        bundle.putInt("services_payment_pending", servicesPaymentPending);
        makePaymentDialogFragment.setArguments(bundle);
        makePaymentDialogFragment.show(getSupportFragmentManager(), "makePaymentDialogFragment");
    }

    @Override
    public void onMakePaymentFinish() {
        viewPager.setAdapter(null);
        mCustomPagerAdapter = new CustomPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mCustomPagerAdapter);
        viewPager.setCurrentItem(1);
    }

    class CustomPagerAdapter extends FragmentStatePagerAdapter {

        private CustomPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            Bundle bundle = new Bundle();

            switch (position) {
                case 0:
                    fragment = new DealTermsFragment();
                    bundle.putInt("buyer_visit_listing_id", buyerVisitListingId);
                    break;
                case 1:
                    bundle.putInt("buyer_visit_listing_id", buyerVisitListingId);
                    fragment = new AllInvoiceFragment();
                    break;
                default:
                    return null;
            }

            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Deal Terms";
                case 1:
                    return "All Invoices";
                default:
                    return "";
            }
        }

        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }
    }
}

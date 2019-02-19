package com.truebil.crm.Activities;

import android.content.Intent;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.truebil.crm.Fragments.BuyerInfoFragment;
import com.truebil.crm.Fragments.DealInfoFragment;
import com.truebil.crm.Fragments.LoanInfoFragment;
import com.truebil.crm.R;

public class DealTermsActivity extends AppCompatActivity implements BuyerInfoFragment.BuyerInfoInterface,
        DealInfoFragment.DealInfoInterface,
        LoanInfoFragment.LoanInfoInterface {

    private String buyerVisitListingId, buyerId;
    private static final String TAG = "DealTermsActivity";
    private ViewPager dealTermsFragmentViewPager;
    private TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_terms);

        if (getIntent() != null) {
            buyerVisitListingId = getIntent().getStringExtra("BUYER_VISIT_LISTING_ID");
            buyerId = getIntent().getStringExtra("BUYER_ID");
        }

        TabLayout tabLayout = findViewById(R.id.activity_deal_terms_tab_layout);
        dealTermsFragmentViewPager = findViewById(R.id.activity_deal_terms_view_pager);
        DealTermsFragmentAdapter adapter = new DealTermsFragmentAdapter(getSupportFragmentManager());
        dealTermsFragmentViewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(dealTermsFragmentViewPager);

        ImageButton backImageButton = findViewById(R.id.activity_deal_terms_back_image_button);
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        errorTextView = findViewById(R.id.activity_deal_terms_error_text_view);
        errorTextView.setVisibility(View.GONE);
    }

    class DealTermsFragmentAdapter extends FragmentStatePagerAdapter {
        DealTermsFragmentAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {

            Bundle bundle = new Bundle();
            bundle.putString("BUYER_VISIT_LISTING_ID", buyerVisitListingId);

            switch (position) {
                case 0:
                    BuyerInfoFragment buyerInfoFragment = new BuyerInfoFragment();
                    buyerInfoFragment.setArguments(bundle);
                    return buyerInfoFragment;

                case 1:
                    DealInfoFragment dealInfoFragment = new DealInfoFragment();
                    dealInfoFragment.setArguments(bundle);
                    return dealInfoFragment;

                case 2:
                    LoanInfoFragment loanInfoFragment = new LoanInfoFragment();
                    loanInfoFragment.setArguments(bundle);
                    return loanInfoFragment;

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Buyer Info";
                case 1:
                    return "Deal Info";
                case 2:
                    return "Loan Info";
            }
            return "";
        }
    }

    /*
     * We wish to display the previous page in the user journey whenever we press back or cancel.
     * Therefore, do not finish the 'last' activity.
     * */
    @Override
    public void OnCancelClick() {
        this.finish();
    }

    @Override
    public void OnDealInfoClick() {
        dealTermsFragmentViewPager.setCurrentItem(1);
    }

    @Override
    public void OnFormValidationError(String error) {

        if (error.isEmpty()) {
            errorTextView.setVisibility(View.GONE);
            return;
        }

        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(error);
    }

    @Override
    public void OnBuyerInfoClick() {
        dealTermsFragmentViewPager.setCurrentItem(0);
    }

    @Override
    public void OnLoanInfoClick() {
        dealTermsFragmentViewPager.setCurrentItem(2);
    }

    @Override
    public void OnPayTokenClick() {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("BUYER_VISIT_LISTING_ID", buyerVisitListingId);
        intent.putExtra("BUYER_ID", buyerId);
        startActivity(intent);
    }
}

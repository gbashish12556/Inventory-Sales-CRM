package com.truebil.crm.Activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.truebil.crm.Adapters.DedicatedFragmentAdapter;
import com.truebil.crm.Fragments.CarDetailsFragment;
import com.truebil.crm.R;

public class DedicatedActivity extends AppCompatActivity implements CarDetailsFragment.CarDetailsFragmentInterface{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dedicated);

        String listingId = getIntent().getStringExtra("LISTING_ID");

        DedicatedFragmentAdapter dedicatedFragmentAdapter = new DedicatedFragmentAdapter(getSupportFragmentManager(), listingId);
        ViewPager dedicatedViewPager = findViewById(R.id.activity_dedicated_view_pager);
        dedicatedViewPager.setAdapter(dedicatedFragmentAdapter);

        TabLayout dedicatedTabLayout = findViewById(R.id.activity_dedicated_tab_layout);
        dedicatedTabLayout.setupWithViewPager(dedicatedViewPager);

        ImageButton backImageButton = findViewById(R.id.activity_dedicated_back_image_button);
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void OnListingInfoAvailable(String listingId, String listingName, String listingStatus) {
        TextView headerTextView = findViewById(R.id.activity_dedicated_header_text_view);
        headerTextView.setText(listingName + " (#" + listingId + ")");

        TextView statusTextView = findViewById(R.id.activity_dedicated_info_text_view);
        if (listingStatus != null && !listingStatus.equals("null")) {
            statusTextView.setVisibility(View.VISIBLE);
            statusTextView.setText(listingStatus);
        }
        else {
            statusTextView.setVisibility(View.GONE);
        }
    }


}

package com.truebil.crm.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.truebil.crm.Constants;
import com.truebil.crm.Fragments.FeedbackDetailFragment;
import com.truebil.crm.Fragments.FeedbackOptionsFragment;
import com.truebil.crm.R;

public class FeedbackActivity extends AppCompatActivity implements FeedbackOptionsFragment.FeedbackOptionsFragmentInterface, FeedbackDetailFragment.FeedbackDetailFragmentInterface {

    //Views
    private TextView headerTextView;
    private ImageButton backImageButton;
    //Primitives
    private static final String TAG = "FeedbackActivity";
    private String buyerVisitListingId, buyerId;
    private boolean isDetailedFeedbackNeeded = false;
    //Other
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        mFragmentManager = getSupportFragmentManager();

        if ((getIntent() != null) && (getIntent().getExtras() != null)) {
            Bundle bundle = getIntent().getExtras();
            buyerVisitListingId = bundle.getString("BUYER_VISIT_LISTING_ID");
            buyerId = bundle.getString("BUYER_ID");
            isDetailedFeedbackNeeded = bundle.getBoolean("DETAILED_FEEDBACK_REQUIRED");
        }
        else {
            Toast.makeText(getApplicationContext(), "No Buyer Id passed", Toast.LENGTH_LONG).show();
            return;
        }

        headerTextView = findViewById(R.id.activity_feedback_header_textview);
        backImageButton = findViewById(R.id.activity_feedback_back_arrow);

        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mFragmentManager.getBackStackEntryCount() == 2) {
                    mFragmentManager.popBackStack();
                    headerTextView.setText("FEEDBACK");
                }
                else if (mFragmentManager.getBackStackEntryCount() > 1) {
                    mFragmentManager.popBackStack();
                }
                else {
                    finish();
                }
            }
        });

        loadInitialFragment();
    }

    private void loadInitialFragment() {
        if (isDetailedFeedbackNeeded) {
            loadDetailFragment(Constants.Keys.TOKEN_PENDING, 2);
        }
        else {
            loadOptionsFragment();
        }
    }

    private void loadOptionsFragment() {
        Fragment feedbackOptionsFragment = new FeedbackOptionsFragment();
        loadFragment(feedbackOptionsFragment);
        headerTextView.setText("FEEDBACK");
    }

    private void loadDetailFragment(String listName, int statusId) {
        Fragment feedbackDetailFragment = new FeedbackDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("BUYER_VISIT_LISTING_ID", buyerVisitListingId);
        bundle.putInt("STATUS_ID", statusId);
        feedbackDetailFragment.setArguments(bundle);

        loadFragment(feedbackDetailFragment);
        headerTextView.setText(listName.toUpperCase());
    }

    void loadFragment(Fragment fragment) {
        mFragmentManager
                .beginTransaction()
                .replace(R.id.activity_feedback_frame_layout, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void OnFeedbackOptionSelected(String selectedList) {

        int statusId = 0;

        switch (selectedList) {

            case Constants.Keys.TOKEN_GIVEN:
                statusId = 1;
                break;
            case Constants.Keys.TOKEN_PENDING :
                statusId = 2;
                break;
            case Constants.Keys.NEGOTIATION_PENDING :
                statusId = 3;
                break;
            case Constants.Keys.DECISION_PENDING :
                statusId = 4;
                break;
            case Constants.Keys.WAITING_FOR_OTHER_CARS :
                statusId = 5;
                break;
            case Constants.Keys.NEGOTIATION_UNSUCUCESSFUL :
                statusId = 6;
                break;
            case Constants.Keys.CAR_QUALITY_ISSUE :
                statusId = 7;
                break;
            case Constants.Keys.OTHER_REASON :
                statusId = 8;
                break;
        }

        if (selectedList.equalsIgnoreCase(Constants.Keys.TOKEN_GIVEN)) {
            startDealTermsActivity();
        }
        else {
            loadDetailFragment(selectedList, statusId);
        }
    }

    @Override
    public void OnCancelSelected() {
        backImageButton.performClick();
    }

    @Override
    public void OnFeedbackPostSuccess() {
        Toast.makeText(getApplicationContext(), "Feedback Submitted", Toast.LENGTH_SHORT).show();
        finish();
    }

    void startDealTermsActivity() {
        Intent intent = new Intent(getApplicationContext(), DealTermsActivity.class);
        intent.putExtra("BUYER_VISIT_LISTING_ID", buyerVisitListingId);
        intent.putExtra("BUYER_ID", buyerId);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        backImageButton.performClick();
    }
}
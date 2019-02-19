package com.truebil.crm.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.truebil.crm.Constants;
import com.truebil.crm.R;

public class FeedbackOptionsFragment extends Fragment {

    FeedbackOptionsFragmentInterface mCallback;

    public FeedbackOptionsFragment() {
    }

    public interface FeedbackOptionsFragmentInterface {
        void OnFeedbackOptionSelected(String listName);
    }

    public void onAttach(Context context){
        super.onAttach(context);
        try {
            mCallback = (FeedbackOptionsFragmentInterface) context;
        }
        catch(ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FeedbackOptionsFragmentInterface");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feedback_options, container, false);

        LinearLayout tokenGiverLinearLayout = rootView.findViewById(R.id.feedback_token_given);
        LinearLayout tokenPendingLinearLayout = rootView.findViewById(R.id.feedback_token_pending);
        LinearLayout negotiationPendingLinearLayout = rootView.findViewById(R.id.feedback_negotiation_pending);
        LinearLayout decisionPendingLinearLayout = rootView.findViewById(R.id.feedback_decision_pending);
        LinearLayout negotiationUnsuccessfulLinearLayout = rootView.findViewById(R.id.feedback_negotiation_unsuccessful);
        LinearLayout carQualityLinearLayout = rootView.findViewById(R.id.feedback_car_quality_issue);
        LinearLayout othersLinearLayout = rootView.findViewById(R.id.feedback_others);

        tokenGiverLinearLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.OnFeedbackOptionSelected(Constants.Keys.TOKEN_GIVEN);
            }
        });

        tokenPendingLinearLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.OnFeedbackOptionSelected(Constants.Keys.TOKEN_PENDING);
            }
        });

        negotiationPendingLinearLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.OnFeedbackOptionSelected(Constants.Keys.NEGOTIATION_PENDING);
            }
        });

        decisionPendingLinearLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.OnFeedbackOptionSelected(Constants.Keys.DECISION_PENDING);
            }
        });

        negotiationUnsuccessfulLinearLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.OnFeedbackOptionSelected(Constants.Keys.NEGOTIATION_UNSUCUCESSFUL);
            }
        });

        carQualityLinearLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.OnFeedbackOptionSelected(Constants.Keys.CAR_QUALITY_ISSUE);
            }
        });

        othersLinearLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.OnFeedbackOptionSelected(Constants.Keys.OTHER_REASON);
            }
        });

        return rootView;
    }
}
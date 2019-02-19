package com.truebil.crm.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.truebil.crm.Helper;
import com.truebil.crm.R;

public class OtpVerificationFragment extends Fragment {

    String mobileNumber;
    private final static String TAG = "OTPVerificationFragment";
    TextView wrongOTPTextView;
    OtpVerificationFragmentListener mCallback;
    ProgressBar loaderProgressBar;
    TextView verifyOTPTextView;

    public interface OtpVerificationFragmentListener {
        void verifyOtp(String mobile, String otp);
        void resendOtp(String mobile);
        void onEditMobileButtonClicked();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OtpVerificationFragmentListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OTPVerificationFragmentListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_otp_verification, container, false);

        if (getArguments() != null) {
            mobileNumber = getArguments().getString("mobile");
        }

        loaderProgressBar = rootView.findViewById(R.id.fragment_otp_verification_progress_bar);
        TextView mobileNumberTextView = rootView.findViewById(R.id.fragment_otp_verification_phone_number_textview);
        mobileNumberTextView.setText(mobileNumber);

        TextView editMobileTextView = rootView.findViewById(R.id.fragment_otp_verification_edit_phone_textview);
        editMobileTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onEditMobileButtonClicked(); //Basically restart the LoginModalFragment
            }
        });

        final EditText otpEditText = rootView.findViewById(R.id.fragment_otp_verification_otp_edittext);
        TextView resendTextView = rootView.findViewById(R.id.fragment_otp_verification_resend_textview);
        resendTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.resendOtp(mobileNumber); // Call resend mobile otp api again.
            }
        });

        wrongOTPTextView = rootView.findViewById(R.id.fragment_otp_verification_wrong_otp_textview);
        wrongOTPTextView.setVisibility(View.GONE);

        verifyOTPTextView = rootView.findViewById(R.id.fragment_otp_verification_verify_otp_text_view);
        verifyOTPTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = otpEditText.getText().toString();
                loaderProgressBar.setVisibility(View.VISIBLE);
                verifyOTPTextView.setEnabled(false);
                Helper.hideSoftKeyboard(getActivity());
                mCallback.verifyOtp(mobileNumber, otp);
                // Hide during retry
                wrongOTPTextView.setVisibility(View.GONE);
            }
        });

        Helper.setupKeyboardHidingUI(rootView, getActivity());
        return rootView;
    }

    public void sendOtpVerificationError(String message){
        loaderProgressBar.setVisibility(View.GONE);
        verifyOTPTextView.setEnabled(true);
        wrongOTPTextView.setText(message);
        wrongOTPTextView.setVisibility(View.VISIBLE);
    }
}

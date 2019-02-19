package com.truebil.crm.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.truebil.crm.Helper;
import com.truebil.crm.R;

/**
 * A simple {@link Fragment} subclass.
 */
 public class LoginFragment extends Fragment {

    View rootView;
    LoginFragmentListener mCallback;
    TextView verifyMobileTextView;
    ProgressBar loaderProgressBar;

    public LoginFragment() {
        // Required empty public constructor
    }


    public interface LoginFragmentListener {
        void requestMobileOTP(String mobileNumber);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (LoginFragmentListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement LoginFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_login, container, false);
        final EditText mobileEditText = rootView.findViewById(R.id.fragment_login_modal_mobile_number_edittext);
        loaderProgressBar = rootView.findViewById(R.id.fragment_login_progress_bar);
        verifyMobileTextView = rootView.findViewById(R.id.fragment_login_modal_verify_mobile_button);
        verifyMobileTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.hideSoftKeyboard(getActivity());
                loaderProgressBar.setVisibility(View.VISIBLE);
//                Log.d("request_otp", mobileEditText.getText().toString());
                mCallback.requestMobileOTP(mobileEditText.getText().toString());
                //Disable the button to stop multiple requests.
                verifyMobileTextView.setEnabled(false);
            }
        });

        Helper.setupKeyboardHidingUI(rootView, getActivity());

        return rootView;

    }

    public void sentLoginError(String message) {
        loaderProgressBar.setVisibility(View.GONE);
        verifyMobileTextView.setEnabled(true);
        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
    }

}

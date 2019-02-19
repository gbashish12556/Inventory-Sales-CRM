package com.truebil.crm.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.truebil.crm.Constants;
import com.truebil.crm.Helper;
import com.truebil.crm.Network.VolleyService;
import com.truebil.crm.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LoanInfoFragment extends Fragment {

    private LoanInfoInterface mCallback;
    private static final String TAG = "LoanInfoFragment";
    private RequestQueue queue;
    private String buyerVisitListingId;
    private ProgressBar loadingProgressBar;
    private LinearLayout superLinearLayout;
    private Spinner accountTypeSpinner, employmentTypeSpinner, residenceTypeSpinner, currentBankSpinner, loanTenureSpinner;
    private EditText loanAmountEditText, ageEditText, annualIncomeEditText, loanNoteEditText, workingSinceEditText, stayingSinceEditText, otherTenureEditText;
    private ArrayAdapter<String> accountTypeAdapter, employmentTypeAdapter, residenceTypeAdapter, currentBankAdapter, loanTenureAdapter;
    private Switch loanFormSwitch;
    private SharedPreferences sharedPref;
    private Boolean isLoanRequired = true;

    public LoanInfoFragment() {
    }

    public interface LoanInfoInterface {
        void OnDealInfoClick();
        void OnPayTokenClick();
        void OnFormValidationError(String error);
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (LoanInfoInterface) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement LoanInfoInterface");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_loan_info, container, false);

        if (getActivity() == null || getContext() == null)
            return rootView;

        Helper.setupKeyboardHidingUI(rootView, getActivity());
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPref = getContext().getSharedPreferences("APP_PREFS", 0);

        superLinearLayout = rootView.findViewById(R.id.fragment_loan_info_super_linear_layout);
        loadingProgressBar = rootView.findViewById(R.id.fragment_loan_info_progress_bar);
        loanFormSwitch = rootView.findViewById(R.id.fragment_loan_info_need_loan_switch);
        accountTypeSpinner = rootView.findViewById(R.id.fragment_load_info_account_type_spinner);
        employmentTypeSpinner = rootView.findViewById(R.id.fragment_loan_info_employment_type_spinner);
        workingSinceEditText = rootView.findViewById(R.id.fragment_loan_info_working_since_edit_text);
        stayingSinceEditText = rootView.findViewById(R.id.fragment_loan_info_stay_years_edit_text);
        otherTenureEditText = rootView.findViewById(R.id.fragment_loan_info_tenure_other_edit_text);
        residenceTypeSpinner = rootView.findViewById(R.id.fragment_loan_info_stay_type_spinner);
        loanAmountEditText = rootView.findViewById(R.id.fragment_load_info_loan_amount_edit_text);
        ageEditText = rootView.findViewById(R.id.fragment_loan_info_age_edit_text);
        annualIncomeEditText = rootView.findViewById(R.id.fragment_loan_info_annual_income_edit_text);
        loanTenureSpinner = rootView.findViewById(R.id.fragment_load_info_loan_tenure_spinner);
        currentBankSpinner = rootView.findViewById(R.id.fragment_load_info_current_bank_spinner);
        loanNoteEditText = rootView.findViewById(R.id.fragment_load_info_loan_note_edit_text);
        final LinearLayout loanFormLinearLayout = rootView.findViewById(R.id.fragment_loan_info_loan_form_linear_layout);
        LinearLayout dealInfoLinearLayout = rootView.findViewById(R.id.fragment_loan_info_deal_info_linear_layout);
        LinearLayout payTokenLinearLayout = rootView.findViewById(R.id.fragment_loan_info_pay_token_linear_layout);
        otherTenureEditText.setVisibility(View.GONE);

        loanFormSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    loanFormLinearLayout.setVisibility(View.VISIBLE);
                    isLoanRequired = true;
                }
                else {
                    loanFormLinearLayout.setVisibility(View.GONE);
                    isLoanRequired = false;
                }
            }
        });

        // Employment Type
        ArrayList<String> employmentTypeArray = Helper.getEmploymentTypeList(getContext());
        employmentTypeArray.add(0, getString(R.string.employment_type_prompt));
        employmentTypeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, employmentTypeArray);
        employmentTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        employmentTypeSpinner.setAdapter(employmentTypeAdapter);

        // Stay Type
        ArrayList<String> residenceTypeArray = Helper.getResidenceTypeList(getContext());
        residenceTypeArray.add(0, getString(R.string.currently_staying_prompt));
        residenceTypeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, residenceTypeArray);
        residenceTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        residenceTypeSpinner.setAdapter(residenceTypeAdapter);

        // Bank Type
        ArrayList<String> banksList = Helper.getBankList(getActivity());
        banksList.add(0, getString(R.string.bank_name_prompt));
        currentBankAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, banksList);
        currentBankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currentBankSpinner.setAdapter(currentBankAdapter);

        // Account Type
        ArrayList<String> accountTypeArray = Helper.getAccountTypeList(getContext());
        accountTypeArray.add(0, getString(R.string.account_type_prompt));
        accountTypeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, accountTypeArray);
        accountTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountTypeSpinner.setAdapter(accountTypeAdapter);

        // Loan Tenure
        ArrayList<String> loanTenureArray = Helper.getLoanTenureList(getContext());
        loanTenureArray.add(0, getString(R.string.loan_tenure_prompt));
        loanTenureAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, loanTenureArray);
        loanTenureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        loanTenureSpinner.setAdapter(loanTenureAdapter);

        // Display tenure edit text if "other" selected in tenure
        loanTenureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (loanTenureSpinner.getSelectedItem().toString().toLowerCase().contains("other")) {
                    otherTenureEditText.setVisibility(View.VISIBLE);
                }
                else {
                    otherTenureEditText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        dealInfoLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.OnDealInfoClick();
            }
        });

        payTokenLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidForm()) {
                    int employmentTypeId = Helper.getEmploymentTypeId(employmentTypeSpinner.getSelectedItem().toString(), getContext());
                    int currentStayTypeId = Helper.getResidenceTypeId(residenceTypeSpinner.getSelectedItem().toString(), getContext());
                    int bankId = Helper.getBankId(currentBankSpinner.getSelectedItem().toString(), getContext());
                    int accountTypeId = Helper.getAccountTypeId(accountTypeSpinner.getSelectedItem().toString(), getContext());

                    String loanTenure;
                    if (otherTenureEditText.getVisibility() == View.VISIBLE) {
                        loanTenure = otherTenureEditText.getText().toString();
                    }
                    else {
                        loanTenure = loanTenureSpinner.getSelectedItem().toString();
                    }

                    postLoanInfo(
                            isLoanRequired,
                            employmentTypeId,
                            workingSinceEditText.getText().toString(),
                            currentStayTypeId,
                            stayingSinceEditText.getText().toString(),
                            ageEditText.getText().toString(),
                            annualIncomeEditText.getText().toString(),
                            loanAmountEditText.getText().toString(),
                            loanTenure,
                            bankId,
                            accountTypeId,
                            loanNoteEditText.getText().toString()
                    );
                }
            }
        });

        if (getArguments() != null) {
            buyerVisitListingId = getArguments().getString("BUYER_VISIT_LISTING_ID");

            showLoadingProgress();
            getPreFilledLoanInfoData(buyerVisitListingId);
        }

        return rootView;
    }

    void getPreFilledLoanInfoData(final String buyerVisitListingId) {
        String url = Constants.Config.API_PATH + "/loan_info?buyer_visit_listing_id=" + buyerVisitListingId;

        if (getActivity() == null)
            return;

        if (queue == null)
            queue = Volley.newRequestQueue(getActivity());

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hideLoadingProgress();
                parseLoanInfoGetResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoadingProgress();
                final JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("API","/loan_info (GET)");
                    jsonObject.put("BVL_ID", buyerVisitListingId);
                    VolleyService.handleVolleyError(error,jsonObject,true, getActivity());
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String dealerJWTToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");
                Log.d("dealerJWTToken",dealerJWTToken);
                headers.put("Authorization", "jwt " + dealerJWTToken);
                return headers;
            }
        };

        jsonRequest.setTag(TAG);
        queue.add(jsonRequest);
    }

    void postLoanInfo(boolean loanRequest,
                      int employmentType,
                      String workingSince,
                      int currentStayType,
                      String currentStayPeriod,
                      String buyerAge,
                      String annualIncome,
                      String loanAmount,
                      String loanTenure,
                      int bankId,
                      int accountType,
                      String loanNote) {

        String url = Constants.Config.API_PATH + "/loan_info/";

        if (getActivity() == null)
            return;

        if (queue == null)
            queue = Volley.newRequestQueue(getActivity());

        final JSONObject params = new JSONObject();
        try {
            params.put("buyer_visit_listing_id", buyerVisitListingId);
            params.put("employment_type", String.valueOf(employmentType));
            params.put("work_stability", workingSince);
            params.put("residence_type", String.valueOf(currentStayType));
            params.put("residence_stability", currentStayPeriod);
            params.put("age_of_applicant", buyerAge);
            params.put("annual_income", annualIncome);
            params.put("loan_amount_required", loanAmount);
            params.put("loan_tenure", loanTenure);
            params.put("current_bank", String.valueOf(bankId));
            params.put("account_type", String.valueOf(accountType));
            params.put("additional_remarks", loanNote);
            params.put("loan_request", loanRequest);
            Log.d("params", String.valueOf(params));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parseLoanInfoPostResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    params.put("API","/loan_info (POST)");
                    VolleyService.handleVolleyError(error,params,true, getActivity());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String dealerJWTToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");
                Log.d("dealerJWTToken",dealerJWTToken);
                headers.put("Authorization", "jwt " + dealerJWTToken);
                return headers;
            }
        };

        jsonRequest.setTag(TAG);
        queue.add(jsonRequest);
    }

    private class LoanInfo {

        private int employmentType = -1,
                currentStayType = -1,
                bankId = -1,
                accountType = -1,
                loanAmount = 0;

        private String workingSince,
                currentStayPeriod,
                buyerAge,
                annualIncome,
                loanTenure,
                loanNote;

        private boolean loanRequest;

        LoanInfo(JSONObject response) {
            try {
                boolean status = response.getBoolean("status");
                if (!status) return;

                if (!response.getJSONObject("buyer_loan").isNull("loan_request")) {
                    loanRequest = response.getJSONObject("buyer_loan").getBoolean("loan_request");
                }
                if (!response.getJSONObject("buyer_loan").isNull("employment_type")) {
                    employmentType = response.getJSONObject("buyer_loan").getInt("employment_type");
                }
                if (!response.getJSONObject("buyer_loan").isNull("work_stability")) {
                    workingSince = response.getJSONObject("buyer_loan").getString("work_stability");
                }
                // years
                if (!response.getJSONObject("buyer_loan").isNull("residence_type")) {
                    currentStayType = response.getJSONObject("buyer_loan").getInt("residence_type");
                }
                if (!response.getJSONObject("buyer_loan").isNull("residence_stability")) {
                    currentStayPeriod = response.getJSONObject("buyer_loan").getString("residence_stability");
                }
                // years
                if (!response.getJSONObject("buyer_loan").isNull("age")) {
                    buyerAge = response.getJSONObject("buyer_loan").getString("age");
                }
                if (!response.getJSONObject("buyer_loan").isNull("annual_income")) {
                    annualIncome = response.getJSONObject("buyer_loan").getString("annual_income"); // in Lacs
                }
                if (!response.getJSONObject("buyer_loan").isNull("loan_amount_required")) {
                    loanAmount = response.getJSONObject("buyer_loan").getInt("loan_amount_required"); // in Lacs
                }
                if (!response.getJSONObject("buyer_loan").isNull("loan_tenure")) {
                    loanTenure = response.getJSONObject("buyer_loan").getString("loan_tenure"); // "48", "49", "100 Months", ..
                }
                if (!response.getJSONObject("buyer_loan").isNull("current_bank")) {
                    bankId = response.getJSONObject("buyer_loan").getInt("current_bank");
                }
                if (!response.getJSONObject("buyer_loan").isNull("account_type_id")) {
                    accountType = response.getJSONObject("buyer_loan").getInt("account_type_id");
                }
                if (!response.getJSONObject("buyer_loan").isNull("additional_remarks")) {
                    loanNote = response.getJSONObject("buyer_loan").getString("additional_remarks");
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        int getEmploymentType() {
            return employmentType;
        }

        int getCurrentStayType() {
            return currentStayType;
        }

        int getBankId() {
            return bankId;
        }

        int getAccountType() {
            return accountType;
        }

        String getWorkingSince() {
            return workingSince;
        }

        String getCurrentStayPeriod() {
            return currentStayPeriod;
        }

        String getBuyerAge() {
            return buyerAge;
        }

        String getAnnualIncome() {
            return annualIncome;
        }

        int getLoanAmount() {
            return loanAmount;
        }

        String getLoanTenure() {
            return loanTenure;
        }

        String getLoanNote() {
            return loanNote;
        }

        boolean isLoanRequest() {
            return loanRequest;
        }
    }

    void parseLoanInfoGetResponse(JSONObject response) {

        LoanInfo loanInfo = new LoanInfo(response);

        loanFormSwitch.setChecked(loanInfo.isLoanRequest());
        ageEditText.setText(loanInfo.getBuyerAge());
        annualIncomeEditText.setText(loanInfo.getAnnualIncome());
        loanAmountEditText.setText(String.valueOf(loanInfo.getLoanAmount()));
        loanNoteEditText.setText(loanInfo.getLoanNote());
        workingSinceEditText.setText(loanInfo.getWorkingSince());
        stayingSinceEditText.setText(loanInfo.getCurrentStayPeriod());

        if (loanInfo.getEmploymentType() != -1) {
            String employmentTypeString = Helper.getEmploymentTypeName(loanInfo.getEmploymentType(), getContext());
            int spinnerPosition = employmentTypeAdapter.getPosition(employmentTypeString);
            employmentTypeSpinner.setSelection(spinnerPosition);
        }

        if (loanInfo.getCurrentStayType() != -1) {
            String currentStayTypeString = Helper.getResidenceTypeName(loanInfo.getCurrentStayType(), getContext());
            int spinnerPosition = residenceTypeAdapter.getPosition(currentStayTypeString);
            residenceTypeSpinner.setSelection(spinnerPosition);
        }

        if (loanInfo.getBankId() != -1) {
            String bankString = Helper.getBankName(loanInfo.getBankId(), getContext());
            int spinnerPosition = currentBankAdapter.getPosition(bankString);
            currentBankSpinner.setSelection(spinnerPosition);
        }

        if (loanInfo.getAccountType() != -1) {
            String accountTypeString = Helper.getAccountTypeName(loanInfo.getAccountType(), getContext());
            int spinnerPosition = accountTypeAdapter.getPosition(accountTypeString);
            accountTypeSpinner.setSelection(spinnerPosition);
        }

        if (loanInfo.getLoanTenure() != null) {
            int spinnerPosition = loanTenureAdapter.getPosition(loanInfo.getLoanTenure());
            if (spinnerPosition < 0) {
                otherTenureEditText.setVisibility(View.VISIBLE);
                otherTenureEditText.setText(loanInfo.getLoanTenure());

                /** Set spinner to "Others" */
                // 1. Iterate through the loan tenure list
                for (String loanTenureOption: Helper.getLoanTenureList(getContext())) {
                    // 2. Find the option that resembles "other" string
                    if (loanTenureOption.toLowerCase().contains("other")) {
                        // 3. Set that option as spinner selection
                        spinnerPosition = loanTenureAdapter.getPosition(loanTenureOption);
                        loanTenureSpinner.setSelection(spinnerPosition);
                    }
                }
            }
            else {
                loanTenureSpinner.setSelection(spinnerPosition);
            }
        }
    }

    void parseLoanInfoPostResponse(JSONObject response) {
        try {
            boolean status = response.getBoolean("status");
            if (!status)
                return;

            Toast.makeText(getContext(), "Loan Info Submitted", Toast.LENGTH_SHORT).show();
            mCallback.OnPayTokenClick();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    boolean isValidForm() {

        if (!loanFormSwitch.isChecked()) {
            mCallback.OnFormValidationError("");
            return true;
        }
        else if (employmentTypeSpinner.getSelectedItem().toString().equals(getString(R.string.employment_type_prompt))) {
            mCallback.OnFormValidationError("Employment Type is required");
            return false;
        }
        else if (workingSinceEditText.getText().toString().isEmpty()) {
            mCallback.OnFormValidationError("Working Since is required");
            return false;
        }
        else if (residenceTypeSpinner.getSelectedItem().toString().equals(getString(R.string.currently_staying_prompt))) {
            mCallback.OnFormValidationError("Current Stay Type is required");
            return false;
        }
        else if (stayingSinceEditText.getText().toString().isEmpty()) {
            mCallback.OnFormValidationError("Staying Since is required");
            return false;
        }
        else if (annualIncomeEditText.getText().toString().isEmpty()) {
            mCallback.OnFormValidationError("Annual income is required");
            return false;
        }
        if (ageEditText.getText().toString().isEmpty()) {
            mCallback.OnFormValidationError("Age is required");
            return false;
        }
        else if (loanAmountEditText.getText().toString().isEmpty()) {
            mCallback.OnFormValidationError("Loan Amount is required");
            return false;
        }
        else if (loanTenureSpinner.getSelectedItem().toString().equals(getString(R.string.loan_tenure_prompt))) {
            mCallback.OnFormValidationError("Loan Tenure is required");
            return false;
        }
        else if (currentBankSpinner.getSelectedItem().toString().equals(getString(R.string.bank_name_prompt))) {
            mCallback.OnFormValidationError("Current Bank is required");
            return false;
        }
        else if (accountTypeSpinner.getSelectedItem().toString().equals(getString(R.string.account_type_prompt))) {
            mCallback.OnFormValidationError("Account Type is required");
            return false;
        }
        else if (otherTenureEditText.getVisibility() == View.VISIBLE && otherTenureEditText.getText().toString().isEmpty()) {
            mCallback.OnFormValidationError("Loan Tenure not filled");
            return false;
        }

        mCallback.OnFormValidationError("");
        return true;
    }

    void showLoadingProgress() {
        superLinearLayout.setVisibility(View.INVISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);
    }

    void hideLoadingProgress() {
        superLinearLayout.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.GONE);
    }
}
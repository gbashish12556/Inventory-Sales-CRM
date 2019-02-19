package com.truebil.crm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Helper {

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static String getIndianCurrencyFormat(int money) {

        String amount = String.valueOf(money);
        StringBuilder stringBuilder = new StringBuilder();
        char amountArray[] = amount.toCharArray();
        int a = 0, b = 0;
        for (int i = amountArray.length - 1; i >= 0; i--) {
            if (a < 3) {
                stringBuilder.append(amountArray[i]);
                a++;
            } else if (b < 2) {
                if (b == 0) {
                    stringBuilder.append(",");
                    stringBuilder.append(amountArray[i]);
                    b++;
                } else {
                    stringBuilder.append(amountArray[i]);
                    b = 0;
                }
            }
        }
        return "\u20B9"+stringBuilder.reverse().toString();
    }

    public static void setupKeyboardHidingUI(View view, final Activity activity) {
        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(activity);
                    return false;
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupKeyboardHidingUI(innerView, activity);
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        if (inputMethodManager != null && activity.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static int convertDpToPixel(int dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = (int) (dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
    public static void putPreference(Context context, String key,String value){
        SharedPreferences sharedPreferences = context.getSharedPreferences("APP_PREFS", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
    }

    public static String getPreference(Context context, String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences("APP_PREFS", 0);
        return sharedPreferences.getString(key,"");
    }

    //0. Bank Functions
    public static String getBankName(int bankId, Context context) {
        return convertJsonArrayIdToName(Constants.Keys.BANKS, bankId, context);
    }

    public static int getBankId(String bankName, Context context){
        return convertJsonArrayNameToId(Constants.Keys.BANKS, bankName, context);
    }

    public static ArrayList<String> getBankList(Context ctx) {
        return createListFromJsonArray(Constants.Keys.BANKS, ctx);
    }

    //1. Locality Functions
    public static String getLocalityName(int localityId, Context context){
        return convertJsonArrayIdToName(Constants.Keys.LOCALITIES_LIST, localityId, context);
    }

    public static int getLocalityId(String localityName, Context context){
        return convertJsonArrayNameToId(Constants.Keys.LOCALITIES_LIST, localityName, context);
    }

    public static ArrayList<String> getLocalityList(Context ctx) {
        return createListFromJsonArray(Constants.Keys.LOCALITIES_LIST, ctx);
    }

    //2. RTO functions
    public static String getRtoName(int rtoId, Context context){
        return convertJsonArrayIdToName(Constants.Keys.RTO_LIST, rtoId, context);
    }

    public static int getRtoId(String rtoName, Context context) {
        return convertJsonArrayNameToId(Constants.Keys.RTO_LIST, rtoName, context);
    }

    public static ArrayList<String> getRTOList(Context ctx) {
        return createListFromJsonArray(Constants.Keys.RTO_LIST, ctx);
    }

    public static ArrayList<String> getCateredByList(Context ctx){
        return createListFromJsonArray(Constants.Keys.SALES_REPRESENTATIVE_LIST, ctx);
    }

    public static int getCateredById(String cateredByName, Context context){
        return convertJsonArrayNameToId(Constants.Keys.SALES_REPRESENTATIVE_LIST, cateredByName, context);
    }

    public static String getCateredByName(int cateredId, Context ctx) {
        return convertJsonArrayIdToName(Constants.Keys.SALES_REPRESENTATIVE_LIST, cateredId, ctx);
    }

    private static int convertJsonArrayNameToId(String optionCategory, String optionName, Context context) {
        int id = 0;
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences("APP_PREFS", 0);
            JSONArray localityArray = new JSONArray(sharedPreferences.getString(optionCategory, ""));
            for (int i=0; i<localityArray.length(); i++) {
                if (localityArray.getJSONObject(i).getString("name").equalsIgnoreCase(optionName)) {
                    id = localityArray.getJSONObject(i).getInt("id");
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    private static String convertJsonArrayIdToName(String optionCategory, int optionId, Context context) {
        String name = "";
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences("APP_PREFS", 0);
            JSONArray rtoArray = new JSONArray(sharedPreferences.getString(optionCategory, ""));
            for(int i=0; i<rtoArray.length(); i++) {
                if (rtoArray.getJSONObject(i).getInt("id") == optionId) {
                    name = rtoArray.getJSONObject(i).getString("name");
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return name;
    }

    private static ArrayList<String> createListFromJsonArray(String optionCategory, Context context) {
        ArrayList<String> cateredByList = new ArrayList<>();
        SharedPreferences sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        try {
            JSONArray cateredByArray = new JSONArray(sharedPref.getString(optionCategory, ""));
            for (int i=0; i<cateredByArray.length(); i++) {
                    cateredByList.add(cateredByArray.getJSONObject(i).getString("name"));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return cateredByList;
    }

    private static ArrayList<String> createListFromJson(String formType, String optionCategory, Context context) {
        ArrayList<String> list = new ArrayList<>();
        try {
            // Fetch JSON from shared preferences
            SharedPreferences sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
            String formString = sharedPref.getString(Constants.Keys.FORM_INFO, "");
            JSONObject formJson = new JSONObject(formString);
            JSONObject requiredFormJson = formJson.getJSONObject(formType);
            JSONObject optionCategoryJson = requiredFormJson.getJSONObject(optionCategory);

            JSONArray keys = optionCategoryJson.names();
            for (int i=0; i<keys.length(); i++) {
                String key = keys.getString(i);
                list.add(key);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    private static String convertFormIdToName(String formType, String optionCategory, int optionId, Context context) {
        try {
            // Fetch JSON from shared preferences
            SharedPreferences sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
            String formString = sharedPref.getString(Constants.Keys.FORM_INFO, "");
            JSONObject formJson = new JSONObject(formString);
            JSONObject requiredFormJson = formJson.getJSONObject(formType);
            JSONObject optionCategoryJson = requiredFormJson.getJSONObject(optionCategory);

            Iterator<?> keys = optionCategoryJson.keys();
            while (keys.hasNext()) {
                String key = keys.next().toString();
                if (optionCategoryJson.getInt(key) == optionId) {
                    return key;
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }

    private static int convertFormNameToId(String formType, String optionCategory, String optionName, Context context) {
        try {
            SharedPreferences sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
            String formString = sharedPref.getString(Constants.Keys.FORM_INFO, "");
            JSONObject formJson = new JSONObject(formString);
            JSONObject requiredFormJson = formJson.getJSONObject(formType);
            JSONObject optionCategoryJson = requiredFormJson.getJSONObject(optionCategory);

            return optionCategoryJson.getInt(optionName);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ************* BuyerType Functions *************

    public static String getBuyerTypeName(int buyerTypeId, Context context){
        return convertFormIdToName("buyerInfo", "buyer_type", buyerTypeId, context);
    }

    public static int getBuyerTypeId(String buyerTypeName, Context context) {
        return convertFormNameToId("buyerInfo", "buyer_type", buyerTypeName, context);
    }

    public static ArrayList<String> getBuyerTypeList(Context context) {
        return createListFromJson("buyerInfo", "buyer_type", context);
    }

    //************* Amount Collection Paper Transfer Functions *************

    public static String getAmountCollectionPaperTransferName(int amountCollectionPaperTransferId, Context context) {
        return convertFormIdToName("dealInfo", "amount_collection_for_paper_transfer", amountCollectionPaperTransferId, context);
    }

    public static int getAmountCollectionPaperTransferId(String amountCollectionPaperTransferName, Context context) {
        return convertFormNameToId("dealInfo", "amount_collection_for_paper_transfer", amountCollectionPaperTransferName, context);
    }

    public static ArrayList<String> getAmountCollectionPaperTransferList(Context context) {
        return createListFromJson("dealInfo", "amount_collection_for_paper_transfer", context);
    }

    //************* Category Of Transfer Functions *************

    public static String getCategoryOfTransferName(int categoryOfTransferId, Context context) {
        return convertFormIdToName("dealInfo", "category_of_transfer", categoryOfTransferId, context);
    }

    public static int getCategoryOfTransferId(String categoryOfTransferName, Context context) {
        return convertFormNameToId("dealInfo", "category_of_transfer", categoryOfTransferName, context);
    }

    public static ArrayList<String> getCategoryOfTransferList(Context context) {
        return createListFromJson("dealInfo", "category_of_transfer", context);
    }

    // ************* Employment Type Functions *************

    public static String getEmploymentTypeName(int employmentTypeId, Context context) {
        return convertFormIdToName("loanInfo", "employment_type", employmentTypeId, context);
    }

    public static int getEmploymentTypeId(String employmentTypeName, Context context) {
        return convertFormNameToId("loanInfo", "employment_type", employmentTypeName, context);
    }

    public static ArrayList<String> getEmploymentTypeList(Context context) {
        return createListFromJson("loanInfo", "employment_type", context);
    }

    // ************* Residence Type Functions *************

    public static String getResidenceTypeName(int residenceTypeId, Context context) {
        return convertFormIdToName("loanInfo", "residence_type", residenceTypeId, context);
    }

    public static int getResidenceTypeId(String residenceTypeName, Context context) {
        return convertFormNameToId("loanInfo", "residence_type", residenceTypeName, context);
    }

    public static ArrayList<String> getResidenceTypeList(Context context) {
        return createListFromJson("loanInfo", "residence_type", context);
    }

    // ************* Loan Tenure Functions *************

    public static String getLoanTenureName(int loanTenureId, Context context) {
        return convertFormIdToName("loanInfo", "loan_tenure", loanTenureId, context);
    }

    public static int getLoanTenureId(String loanTenureName, Context context) {
        return convertFormNameToId("loanInfo", "loan_tenure", loanTenureName, context);
    }

    public static ArrayList<String> getLoanTenureList(Context context) {
        return createListFromJson("loanInfo", "loan_tenure", context);
    }

    // ************* Account Type Functions *************

    public static String getAccountTypeName(int accountTypeId, Context context) {
        return convertFormIdToName("loanInfo", "account_type", accountTypeId, context);
    }

    public static int getAccountTypeId(String accountTypeName, Context context) {
        return convertFormNameToId("loanInfo", "account_type", accountTypeName, context);
    }

    public static ArrayList<String> getAccountTypeList(Context context) {
        return createListFromJson("loanInfo", "account_type", context);
    }

    // *************  Hypothecation Functions *************

    public static String getHypothecationName(int hypothecationId, Context context) {
        return convertFormIdToName("deliveryInfo", "hypothecation", hypothecationId, context);
    }

    public static int getHypothecationId(String hypothecationName, Context context) {
        return convertFormNameToId("deliveryInfo", "hypothecation", hypothecationName, context);
    }

    public static ArrayList<String> getHypothecationList(Context context) {
        return createListFromJson("deliveryInfo", "hypothecation", context);
    }

    // ************* Twelve Month Rsa Functions *************

    public static String getTwelveMonthRsaName(int twelveMonthRsaId, Context context) {
        return convertFormIdToName("deliveryInfo", "rsa_12_month_service", twelveMonthRsaId, context);
    }

    public static int getTwelveMonthRsaId(String twelveMonthRsaName, Context context) {
        return convertFormNameToId("deliveryInfo", "rsa_12_month_service", twelveMonthRsaName, context);
    }

    public static ArrayList<String> getTwelveMonthRsaList(Context context) {
        return createListFromJson("deliveryInfo", "rsa_12_month_service", context);
    }

    // ************* Reasons For Cancellation Functions *************

    public static String getReasonsForCancellationName(int reasonsForCancellationId, Context context) {
        return convertFormIdToName("dealCancellationInfo", "reasons_for_cancellation", reasonsForCancellationId, context);
    }

    public static int getReasonsForCancellationId(String reasonsForCancellationName, Context context) {
        return convertFormNameToId("dealCancellationInfo", "reasons_for_cancellation", reasonsForCancellationName, context);
    }

    public static ArrayList<String> getReasonForCancellationList(Context context) {
        return createListFromJson("dealCancellationInfo", "reasons_for_cancellation", context);
    }
}
package com.truebil.crm.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.truebil.crm.R;
import com.truebil.crm.Utils.RemoteConfigUpdateCheck;

import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity implements RemoteConfigUpdateCheck.RemoteConfigUpdateCheckInterface {

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.splashScreenTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        /*
         * Perform playstore app version check using firebase remote config.
         * If force_update == true, then show alert dialog prompting app update.
         * Otherwise, now proceed to check "is user verified by admin" using dealer_status api
         * Depending on response, either direct user to LoginActivity or BiddingActivity.
         */
        new RemoteConfigUpdateCheck(this);
    }

    @Override
    public void onAppUpdateNotRequired() {
        boolean hasLoggedInBefore = sharedPref.getBoolean("HAS_LOGGED_IN_BEFORE", false);
        // Logged in and verified

        if (hasLoggedInBefore) {
            Intent intent = new Intent(this, SalesActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onForcedAppUpdateRequired() {
        if (!MainActivity.this.isFinishing()) {
            /*
             * Display the AlertDialog only when the activity is NOT finishing.
             * This bug was detected through crashlytics
             */
            new AlertDialog.Builder(this)
                    .setTitle("Update Required")
                    .setMessage("Truebil Sales CRM needs to be updated to continue. Update to the latest version?")
                    .setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            openPlaystoreListing();
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .create()
                    .show();
        }
    }

    private void openPlaystoreListing() {
        String playstoreUrl = "https://play.google.com/store/apps/details?id=com.truebil.crm";
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(playstoreUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

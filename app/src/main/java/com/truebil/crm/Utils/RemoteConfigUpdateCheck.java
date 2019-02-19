package com.truebil.crm.Utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.truebil.crm.BuildConfig;

import java.util.HashMap;
import java.util.Map;

public class RemoteConfigUpdateCheck {

    private static final String KEY_FORCE_UPDATE = "force_update";
    private static final String KEY_MINIMUM_STABLE_ANDROID_APK_VERSION = "minimum_stable_android_apk_version";
    private static final String TAG = "RemoteConfigUpdateCheck";
    private RemoteConfigUpdateCheckInterface mCallback;
    private FirebaseRemoteConfig remoteConfig;

    public interface RemoteConfigUpdateCheckInterface {
        void onForcedAppUpdateRequired();
        void onAppUpdateNotRequired();
    }

    public RemoteConfigUpdateCheck(final Context context) {
        remoteConfig = FirebaseRemoteConfig.getInstance();

        //Set defaults
        Map<String, Object> remoteConfigDefaults = new HashMap<>();
        remoteConfigDefaults.put(KEY_FORCE_UPDATE, false);
        remoteConfigDefaults.put(KEY_MINIMUM_STABLE_ANDROID_APK_VERSION, "1.0.0");
        remoteConfig.setDefaults(remoteConfigDefaults);

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        FirebaseRemoteConfig.getInstance().setConfigSettings(configSettings);

        long cacheExpiration = 1800;
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        remoteConfig.fetch(cacheExpiration).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // After config data is successfully fetched, it must be activated before newly fetched values are returned.
                    remoteConfig.activateFetched();
                }
                else {
                    //Toast.makeText(context, "Fetch Failed", Toast.LENGTH_SHORT).show();
                    Crashlytics.logException(new Exception("Firebase Remote Config fetch failed"));
                }
                check();
            }
        });

        try {
            mCallback = (RemoteConfigUpdateCheckInterface) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException("RemoteConfigUpdateCheck class must implement RemoteConfigUpdateCheckInterface");
        }
    }

    private void check() {

        if (remoteConfig.getBoolean(KEY_FORCE_UPDATE)) {

            String minimumStableAppVersionString = remoteConfig.getString(KEY_MINIMUM_STABLE_ANDROID_APK_VERSION);
            String appVersionString = BuildConfig.VERSION_NAME;

            // Error Checking (In case value not present in Remote Config)
            if (minimumStableAppVersionString == null) {
                mCallback.onAppUpdateNotRequired();
            }

            Version minimumStableAppVersion = new Version(minimumStableAppVersionString);
            Version appVersion = new Version(appVersionString);

            if (mCallback != null) {
                if (appVersion.compareTo(minimumStableAppVersion) < 0) {
                    mCallback.onForcedAppUpdateRequired();
                }
                else {
                    mCallback.onAppUpdateNotRequired();
                }
            }
        }
        else {
            mCallback.onAppUpdateNotRequired();
        }
    }

    private class Version implements Comparable<Version> {

        private String version;

        Version(String version) {
            if (version == null)
                throw new IllegalArgumentException("Version can not be null");
            if (!version.matches("[0-9]+(\\.[0-9]+)*"))
                throw new IllegalArgumentException("Invalid version format");
            this.version = version;
        }

        public final String get() {
            return this.version;
        }

        @Override
        public int compareTo(Version that) {
            if (that == null)
                return 1;

            String[] thisParts = this.get().split("\\.");
            String[] thatParts = that.get().split("\\.");
            int length = Math.max(thisParts.length, thatParts.length);

            for (int i = 0; i < length; i++) {
                int thisPart = i < thisParts.length ? Integer.parseInt(thisParts[i]) : 0;
                int thatPart = i < thatParts.length ? Integer.parseInt(thatParts[i]) : 0;

                if (thisPart < thatPart)
                    return -1;

                if (thisPart > thatPart)
                    return 1;
            }
            return 0;
        }

        @Override
        public boolean equals(Object that) {
            if (this == that)
                return true;
            if (that == null)
                return false;
            if (this.getClass() != that.getClass())
                return false;
            return this.compareTo((Version) that) == 0;
        }
    }
}
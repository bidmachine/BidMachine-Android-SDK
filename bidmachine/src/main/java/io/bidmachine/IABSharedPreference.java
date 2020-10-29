package io.bidmachine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

class IABSharedPreference {

    @VisibleForTesting
    static final String IAB_CONSENT_STRING = "IABConsent_ConsentString";
    @VisibleForTesting
    static final String IAB_SUBJECT_TO_GDPR = "IABConsent_SubjectToGDPR";
    @VisibleForTesting
    static final String IAB_US_PRIVACY_STRING = "IABUSPrivacy_String";

    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceListener;
    private String iabGDPRConsentString;
    private Boolean iabSubjectToGDPR;
    private String iabUSPrivacyString;

    IABSharedPreference() {
        sharedPreferenceListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (TextUtils.isEmpty(key)) {
                    return;
                }
                switch (key) {
                    case IAB_CONSENT_STRING:
                        updateConsentString(sharedPreferences);
                        break;
                    case IAB_SUBJECT_TO_GDPR:
                        updateGDPRSubject(sharedPreferences);
                        break;
                    case IAB_US_PRIVACY_STRING:
                        updateUSPrivacyString(sharedPreferences);
                        break;
                }
            }
        };
    }

    void initialize(@NonNull Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceListener);
        updateConsentString(sharedPreferences);
        updateGDPRSubject(sharedPreferences);
        updateUSPrivacyString(sharedPreferences);
    }

    @Nullable
    String getGDPRConsentString() {
        return iabGDPRConsentString;
    }

    private void updateConsentString(@NonNull SharedPreferences sharedPreferences) {
        iabGDPRConsentString = readString(sharedPreferences, IAB_CONSENT_STRING, null);
    }

    @Nullable
    Boolean getSubjectToGDPR() {
        return iabSubjectToGDPR;
    }

    private void updateGDPRSubject(@NonNull SharedPreferences sharedPreferences) {
        String iabConsentSubjectToGDPR = readString(sharedPreferences, IAB_SUBJECT_TO_GDPR, null);
        iabSubjectToGDPR = iabConsentSubjectToGDPR != null
                ? iabConsentSubjectToGDPR.equals("1")
                : null;
    }

    @Nullable
    String getUSPrivacyString() {
        return iabUSPrivacyString;
    }

    private void updateUSPrivacyString(@NonNull SharedPreferences sharedPreferences) {
        iabUSPrivacyString = readString(sharedPreferences, IAB_US_PRIVACY_STRING, null);
    }

    @Nullable
    @VisibleForTesting
    String readString(@NonNull SharedPreferences sharedPreferences,
                      @NonNull String key,
                      @Nullable String defValue) {
        try {
            String result = sharedPreferences.getString(key, defValue);
            return result != null
                    ? result
                    : defValue;
        } catch (Exception e) {
            return defValue;
        }
    }

}
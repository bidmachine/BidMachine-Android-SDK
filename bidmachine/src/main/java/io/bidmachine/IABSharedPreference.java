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

    @VisibleForTesting
    static final String IAB_TCF_TC_STRING = "IABTCF_TCString";
    @VisibleForTesting
    static final String IAB_TCF_GDPR_APPLIES = "IABTCF_gdprApplies";

    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceListener;

    private String iabGDPRConsentString;
    private Boolean iabSubjectToGDPR;
    private String iabUSPrivacyString;

    private String iabTcfTcString;
    private Boolean iabTcfGdprApplies;

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
                    case IAB_TCF_TC_STRING:
                        updateTcfTcString(sharedPreferences);
                        break;
                    case IAB_TCF_GDPR_APPLIES:
                        updateTcfGdprApplies(sharedPreferences);
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
        updateTcfTcString(sharedPreferences);
        updateTcfGdprApplies(sharedPreferences);
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
    String getTcfTcString() {
        return iabTcfTcString;
    }

    private void updateTcfTcString(@NonNull SharedPreferences sharedPreferences) {
        iabTcfTcString = readString(sharedPreferences, IAB_TCF_TC_STRING, null);
    }

    @Nullable
    Boolean getTcfGdprApplies() {
        return iabTcfGdprApplies;
    }

    private void updateTcfGdprApplies(@NonNull SharedPreferences sharedPreferences) {
        int gdprApplies = readInt(sharedPreferences, IAB_TCF_GDPR_APPLIES, -1);
        if (gdprApplies == 1) {
            iabTcfGdprApplies = true;
        } else if (gdprApplies == 0) {
            iabTcfGdprApplies = false;
        } else {
            iabTcfGdprApplies = null;
        }
    }

    @Nullable
    @VisibleForTesting
    String readString(@NonNull SharedPreferences sharedPreferences,
                      @NonNull String key,
                      @Nullable String defValue) {
        try {
            String result = sharedPreferences.getString(key, defValue);
            if (result != null) {
                return result;
            }
        } catch (Exception ignore) {
        }
        return defValue;
    }

    @VisibleForTesting
    int readInt(@NonNull SharedPreferences sharedPreferences,
                @NonNull String key,
                int defValue) {
        try {
            if (sharedPreferences.contains(key)) {
                return sharedPreferences.getInt(key, defValue);
            }
        } catch (Exception ignore) {
        }
        return defValue;
    }

}
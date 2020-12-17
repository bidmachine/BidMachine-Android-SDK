package io.bidmachine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

public class DefaultSharedPreferencesEditor {

    private final SharedPreferences sharedPreferences;

    public DefaultSharedPreferencesEditor(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        clear();
    }

    public DefaultSharedPreferencesEditor setGdprConsent(@Nullable String gdprConsent) {
        sharedPreferences.edit()
                .putString(IABSharedPreference.IAB_CONSENT_STRING, gdprConsent)
                .apply();
        return this;
    }

    public DefaultSharedPreferencesEditor setSubjectToGdpr(@Nullable String subjectToGdpr) {
        sharedPreferences.edit()
                .putString(IABSharedPreference.IAB_SUBJECT_TO_GDPR, subjectToGdpr)
                .apply();
        return this;
    }

    public DefaultSharedPreferencesEditor setUsPrivacy(@Nullable String usPrivacy) {
        sharedPreferences.edit()
                .putString(IABSharedPreference.IAB_US_PRIVACY_STRING, usPrivacy)
                .apply();
        return this;
    }

    public DefaultSharedPreferencesEditor setTcfTcString(@Nullable String tcfTcString) {
        sharedPreferences.edit()
                .putString(IABSharedPreference.IAB_TCF_TC_STRING, tcfTcString)
                .apply();
        return this;
    }

    public DefaultSharedPreferencesEditor setTcfGdprApplies(int tcfGdprApplies) {
        sharedPreferences.edit()
                .putInt(IABSharedPreference.IAB_TCF_GDPR_APPLIES, tcfGdprApplies)
                .apply();
        return this;
    }

    public DefaultSharedPreferencesEditor setCustomString(String key, String value) {
        sharedPreferences.edit()
                .putString(key, value)
                .apply();
        return this;
    }

    public DefaultSharedPreferencesEditor setCustomInt(String key, int value) {
        sharedPreferences.edit()
                .putInt(key, value)
                .apply();
        return this;
    }

    public DefaultSharedPreferencesEditor clear() {
        sharedPreferences.edit().clear().apply();
        return this;
    }

}
package io.bidmachine;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;

import io.bidmachine.core.Utils;

class Debugger {

    private final static String TAG = "Debugger";
    private final static String DIR_NAME = "features";
    private final static String FILE_NAME = "DebugParameters.json";
    private final static String PARAMETER_LOGGING_ENABLED = "loggingEnabled";
    private final static String PARAMETER_TEST_MODE = "testMode";
    private final static String PARAMETER_ENDPOINT = "endpoint";
    private final static String PARAMETER_COPPA = "coppa";
    private final static String PARAMETER_US_PRIVACY_STRING = "usPrivacyString";
    private final static String PARAMETER_SUBJECT_TO_GDPR = "subjectToGDPR";
    private final static String PARAMETER_CONSENT = "consent";
    private final static String PARAMETER_GDPR_STRING = "GDPRString";

    static void setup(@NonNull Context context) {
        try {
            File file = new File(Utils.getCacheDir(context, DIR_NAME), FILE_NAME);
            if (!file.exists()) {
                return;
            }
            Log.d(TAG, "Debug file founded");

            FileInputStream fileInputStream = new FileInputStream(file);
            String content = Utils.streamToString(fileInputStream);
            JSONObject jsonObject = new JSONObject(content);

            if (jsonObject.has(PARAMETER_LOGGING_ENABLED)) {
                boolean isLoggingEnabled = jsonObject.optBoolean(PARAMETER_LOGGING_ENABLED);
                BidMachine.setLoggingEnabled(isLoggingEnabled);
            }
            if (jsonObject.has(PARAMETER_TEST_MODE)) {
                boolean isTestMode = jsonObject.optBoolean(PARAMETER_TEST_MODE);
                BidMachine.setTestMode(isTestMode);
            }
            if (jsonObject.has(PARAMETER_ENDPOINT)) {
                String endpoint = jsonObject.optString(PARAMETER_ENDPOINT);
                if (!TextUtils.isEmpty(endpoint) && Utils.isHttpUrl(endpoint)) {
                    BidMachine.setEndpoint(endpoint);
                }
            }
            if (jsonObject.has(PARAMETER_COPPA)) {
                boolean coppa = jsonObject.optBoolean(PARAMETER_COPPA);
                BidMachine.setCoppa(coppa);
            }
            if (jsonObject.has(PARAMETER_US_PRIVACY_STRING)) {
                String usPrivacyString = jsonObject.optString(PARAMETER_US_PRIVACY_STRING);
                BidMachine.setUSPrivacyString(usPrivacyString);
            }
            if (jsonObject.has(PARAMETER_SUBJECT_TO_GDPR)) {
                boolean subjectToGDPR = jsonObject.optBoolean(PARAMETER_SUBJECT_TO_GDPR);
                BidMachine.setSubjectToGDPR(subjectToGDPR);
            }
            if (jsonObject.has(PARAMETER_CONSENT)) {
                boolean consent = jsonObject.optBoolean(PARAMETER_CONSENT);
                String gdprString = null;
                if (jsonObject.has(PARAMETER_GDPR_STRING)) {
                    gdprString = jsonObject.optString(PARAMETER_GDPR_STRING);
                }
                BidMachine.setConsentConfig(consent, gdprString);
            }
        } catch (Throwable t) {
            Log.w(TAG, t);
        }
    }

}
package io.bidmachine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class IABSharedPreferenceTest {

    private Context context;
    private IABSharedPreference iabSharedPreference;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        iabSharedPreference = new IABSharedPreference();
    }

    @Test
    public void getValues_withoutInit() {
        presetSharedPreference("test_gdpr", "1", "test_us_privacy");
        assertNull(iabSharedPreference.getGDPRConsentString());
        assertNull(iabSharedPreference.getSubjectToGDPR());
        assertNull(iabSharedPreference.getUSPrivacyString());
    }

    @Test
    public void getValues_withInit() {
        presetSharedPreference("test_gdpr", "1", "test_us_privacy");
        iabSharedPreference.initialize(context);

        String gdprConsentString = iabSharedPreference.getGDPRConsentString();
        assertNotNull(gdprConsentString);
        assertEquals("test_gdpr", gdprConsentString);
        Boolean subjectToGDPR = iabSharedPreference.getSubjectToGDPR();
        assertNotNull(subjectToGDPR);
        assertTrue(subjectToGDPR);
        String usPrivacyString = iabSharedPreference.getUSPrivacyString();
        assertNotNull(usPrivacyString);
        assertEquals("test_us_privacy", usPrivacyString);
    }

    @Test
    public void getValues_changeValuesAfterInit() {
        presetSharedPreference("test_gdpr", "1", "test_us_privacy");
        iabSharedPreference.initialize(context);
        presetSharedPreference("test_gdpr_new", "0", "test_us_privacy_new");

        String gdprConsentString = iabSharedPreference.getGDPRConsentString();
        assertNotNull(gdprConsentString);
        assertEquals("test_gdpr_new", gdprConsentString);
        Boolean subjectToGDPR = iabSharedPreference.getSubjectToGDPR();
        assertNotNull(subjectToGDPR);
        assertFalse(subjectToGDPR);
        String usPrivacyString = iabSharedPreference.getUSPrivacyString();
        assertNotNull(usPrivacyString);
        assertEquals("test_us_privacy_new", usPrivacyString);
    }

    @Test
    public void readString_valueNotExist() {
        SharedPreferences sharedPreferences = mock(SharedPreferences.class);
        String defValue = "def_value";
        String result = iabSharedPreference.readString(sharedPreferences, "test_key", defValue);
        assertNotNull(result);
        assertEquals(defValue, result);
    }

    @Test
    public void readString_exception() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putInt("test_key", 123).apply();
        String defValue = "def_value";
        String result = iabSharedPreference.readString(sharedPreferences, "test_key", defValue);
        assertNotNull(result);
        assertEquals(defValue, result);
    }

    private void presetSharedPreference(@Nullable String gdprConsent,
                                        @Nullable String subjectToGdpr,
                                        @Nullable String usPrivacy) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (gdprConsent != null) {
            editor.putString(IABSharedPreference.IAB_CONSENT_STRING, gdprConsent);
        }
        if (subjectToGdpr != null) {
            editor.putString(IABSharedPreference.IAB_SUBJECT_TO_GDPR, subjectToGdpr);
        }
        if (usPrivacy != null) {
            editor.putString(IABSharedPreference.IAB_US_PRIVACY_STRING, usPrivacy);
        }
        editor.apply();
    }

}
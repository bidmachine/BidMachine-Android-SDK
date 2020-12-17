package io.bidmachine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
    private DefaultSharedPreferencesEditor defaultSharedPreferencesEditor;
    private IABSharedPreference iabSharedPreference;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        defaultSharedPreferencesEditor = new DefaultSharedPreferencesEditor(context);
        iabSharedPreference = new IABSharedPreference();
    }

    @Test
    public void getValues_withoutInit() {
        defaultSharedPreferencesEditor
                .setGdprConsent("test_gdpr")
                .setSubjectToGdpr("1")
                .setUsPrivacy("test_us_privacy")
                .setTcfTcString("test_tc")
                .setTcfGdprApplies(1);
        assertNull(iabSharedPreference.getGDPRConsentString());
        assertNull(iabSharedPreference.getSubjectToGDPR());
        assertNull(iabSharedPreference.getUSPrivacyString());
        assertNull(iabSharedPreference.getTcfTcString());
        assertNull(iabSharedPreference.getTcfGdprApplies());
    }

    @Test
    public void getValues_withInit() {
        defaultSharedPreferencesEditor
                .setGdprConsent("test_gdpr")
                .setSubjectToGdpr("1")
                .setUsPrivacy("test_us_privacy")
                .setTcfTcString("test_tc")
                .setTcfGdprApplies(1);
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
        String tcfTcString = iabSharedPreference.getTcfTcString();
        assertNotNull(tcfTcString);
        assertEquals("test_tc", tcfTcString);
        Boolean tcfGdprApplies = iabSharedPreference.getTcfGdprApplies();
        assertNotNull(tcfGdprApplies);
        assertTrue(tcfGdprApplies);
    }

    @Test
    public void getValues_changeValuesAfterInit() {
        defaultSharedPreferencesEditor
                .setGdprConsent("test_gdpr")
                .setSubjectToGdpr("1")
                .setUsPrivacy("test_us_privacy")
                .setTcfTcString("test_tc")
                .setTcfGdprApplies(1);
        iabSharedPreference.initialize(context);
        defaultSharedPreferencesEditor
                .setGdprConsent("test_gdpr_new")
                .setSubjectToGdpr("0")
                .setUsPrivacy("test_us_privacy_new")
                .setTcfTcString("test_tc_new")
                .setTcfGdprApplies(0);

        String gdprConsentString = iabSharedPreference.getGDPRConsentString();
        assertNotNull(gdprConsentString);
        assertEquals("test_gdpr_new", gdprConsentString);
        Boolean subjectToGDPR = iabSharedPreference.getSubjectToGDPR();
        assertNotNull(subjectToGDPR);
        assertFalse(subjectToGDPR);
        String usPrivacyString = iabSharedPreference.getUSPrivacyString();
        assertNotNull(usPrivacyString);
        assertEquals("test_us_privacy_new", usPrivacyString);
        String tcfTcString = iabSharedPreference.getTcfTcString();
        assertNotNull(tcfTcString);
        assertEquals("test_tc_new", tcfTcString);
        Boolean tcfGdprApplies = iabSharedPreference.getTcfGdprApplies();
        assertNotNull(tcfGdprApplies);
        assertFalse(tcfGdprApplies);

        defaultSharedPreferencesEditor
                .setGdprConsent("test_gdpr_new_2")
                .setSubjectToGdpr("123")
                .setUsPrivacy("test_us_privacy_new_2")
                .setTcfTcString("test_tc_new_2")
                .setTcfGdprApplies(123);

        gdprConsentString = iabSharedPreference.getGDPRConsentString();
        assertNotNull(gdprConsentString);
        assertEquals("test_gdpr_new_2", gdprConsentString);
        subjectToGDPR = iabSharedPreference.getSubjectToGDPR();
        assertNotNull(subjectToGDPR);
        assertFalse(subjectToGDPR);
        usPrivacyString = iabSharedPreference.getUSPrivacyString();
        assertNotNull(usPrivacyString);
        assertEquals("test_us_privacy_new_2", usPrivacyString);
        tcfTcString = iabSharedPreference.getTcfTcString();
        assertNotNull(tcfTcString);
        assertEquals("test_tc_new_2", tcfTcString);
        tcfGdprApplies = iabSharedPreference.getTcfGdprApplies();
        assertNull(tcfGdprApplies);
    }

    @Test
    public void readString_validValue_returnActualValue() {
        String actualValue = "test_value";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString("test_key", actualValue).apply();
        String result = iabSharedPreference.readString(sharedPreferences, "test_key", "def_value");
        assertNotNull(result);
        assertEquals(actualValue, result);
    }

    @Test
    public void readString_valueNotExist_returnDefaultValue() {
        SharedPreferences sharedPreferences = mock(SharedPreferences.class);
        String defValue = "def_value";
        String result = iabSharedPreference.readString(sharedPreferences, "test_key", defValue);
        assertNotNull(result);
        assertEquals(defValue, result);
    }

    @Test
    public void readString_exception_returnDefaultValue() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putInt("test_key", 123).apply();
        String defValue = "def_value";
        String result = iabSharedPreference.readString(sharedPreferences, "test_key", defValue);
        assertNotNull(result);
        assertEquals(defValue, result);
    }

    @Test
    public void readInt_validValue_returnActualValue() {
        int actualValue = 123;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putInt("test_key", actualValue).apply();
        int result = iabSharedPreference.readInt(sharedPreferences, "test_key", 0);
        assertEquals(actualValue, result);
    }

    @Test
    public void readInt_valueNotExist_returnDefaultValue() {
        SharedPreferences sharedPreferences = mock(SharedPreferences.class);
        int defValue = -1;
        int result = iabSharedPreference.readInt(sharedPreferences, "test_key", defValue);
        assertEquals(defValue, result);
    }

    @Test
    public void readInt_exception_returnDefaultValue() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString("test_key", "123").apply();
        int defValue = -1;
        int result = iabSharedPreference.readInt(sharedPreferences, "test_key", defValue);
        assertEquals(defValue, result);
    }

}
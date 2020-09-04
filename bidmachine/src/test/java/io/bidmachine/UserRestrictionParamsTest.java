package io.bidmachine;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.explorestack.protobuf.Any;
import com.explorestack.protobuf.adcom.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import io.bidmachine.protobuf.RegsCcpaExtension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class UserRestrictionParamsTest {

    private android.content.Context context;
    private SharedPreferences defaultSharedPreferences;
    private UserRestrictionParams userRestrictionParams;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        defaultSharedPreferences.edit().clear().apply();
        userRestrictionParams = BidMachineImpl.get().getUserRestrictionParams();
        BidMachine.setConsentConfig(false, null);
        BidMachine.setUSPrivacyString(null);
    }

    @Test
    public void buildUser_bidMachineAndSharedPrefProvidedConsentString_returnBidMachineConsentString() {
        defaultSharedPreferences.edit()
                .putString(IABSharedPreference.IAB_CONSENT_STRING, "iab_consent_string")
                .apply();
        BidMachine.setConsentConfig(true, "bid_machine_consent_string");
        BidMachineImpl.get().getIabSharedPreference().initialize(context);

        Context.User.Builder builder = Context.User.newBuilder();
        assertEquals("", builder.getConsent());
        userRestrictionParams.build(builder);
        assertEquals("bid_machine_consent_string", builder.getConsent());
    }

    @Test
    public void buildUser_bidMachineProvidedAndSharedPrefNotProvidedConsentString_returnBidMachineConsentString() {
        BidMachine.setConsentConfig(true, "bid_machine_consent_string");
        BidMachineImpl.get().getIabSharedPreference().initialize(context);

        Context.User.Builder builder = Context.User.newBuilder();
        assertEquals("", builder.getConsent());
        userRestrictionParams.build(builder);
        assertEquals("bid_machine_consent_string", builder.getConsent());
    }

    @Test
    public void buildUser_bidMachineNotProvidedAndSharedPrefProvidedConsentString_returnIABConsentString() {
        defaultSharedPreferences.edit()
                .putString(IABSharedPreference.IAB_CONSENT_STRING, "iab_consent_string")
                .apply();
        BidMachine.setConsentConfig(true, null);
        BidMachineImpl.get().getIabSharedPreference().initialize(context);

        Context.User.Builder builder = Context.User.newBuilder();
        assertEquals("", builder.getConsent());
        userRestrictionParams.build(builder);
        assertEquals("iab_consent_string", builder.getConsent());
    }

    @Test
    public void buildUser_bidMachineAndSharedPrefNotProvidedConsentString_returnDefaultConsentStringOneString() {
        BidMachine.setConsentConfig(true, null);
        BidMachineImpl.get().getIabSharedPreference().initialize(context);

        Context.User.Builder builder = Context.User.newBuilder();
        assertEquals("", builder.getConsent());
        userRestrictionParams.build(builder);
        assertEquals("1", builder.getConsent());
    }

    @Test
    public void buildUser_bidMachineAndSharedPrefNotProvidedConsentString_returnDefaultConsentStringZeroString() {
        BidMachine.setConsentConfig(false, null);
        BidMachineImpl.get().getIabSharedPreference().initialize(context);

        Context.User.Builder builder = Context.User.newBuilder();
        assertEquals("", builder.getConsent());
        userRestrictionParams.build(builder);
        assertEquals("0", builder.getConsent());
    }

    @Test
    public void buildRegs_bidMachineAndSharedPrefProvidedSubjectToGDPR_returnBidMachineSubjectToGDPR() {
        defaultSharedPreferences.edit()
                .putString(IABSharedPreference.IAB_SUBJECT_TO_GDPR, "0")
                .apply();
        BidMachine.setSubjectToGDPR(true);
        BidMachineImpl.get().getIabSharedPreference().initialize(context);

        Context.Regs.Builder builder = Context.Regs.newBuilder();
        assertFalse(builder.getGdpr());
        userRestrictionParams.build(builder);
        assertTrue(builder.getGdpr());
    }

    @Test
    public void buildRegs_bidMachineProvidedAndSharedPrefNotProvidedSubjectToGDPR_returnBidMachineSubjectToGDPR() {
        BidMachine.setSubjectToGDPR(true);
        BidMachineImpl.get().getIabSharedPreference().initialize(context);

        Context.Regs.Builder builder = Context.Regs.newBuilder();
        assertFalse(builder.getGdpr());
        userRestrictionParams.build(builder);
        assertTrue(builder.getGdpr());
    }

    @Test
    public void buildRegs_bidMachineNotProvidedAndSharedPrefProvidedSubjectToGDPR_returnIABSubjectToGDPR() {
        defaultSharedPreferences.edit()
                .putString(IABSharedPreference.IAB_SUBJECT_TO_GDPR, "1")
                .apply();
        BidMachine.setSubjectToGDPR(null);
        BidMachineImpl.get().getIabSharedPreference().initialize(context);

        Context.Regs.Builder builder = Context.Regs.newBuilder();
        assertFalse(builder.getGdpr());
        userRestrictionParams.build(builder);
        assertTrue(builder.getGdpr());
    }

    @Test
    public void buildRegs_bidMachineAndSharedPrefNotProvidedSubjectToGDPR_returnDefaultSubjectToGDPR() {
        BidMachine.setSubjectToGDPR(null);
        BidMachineImpl.get().getIabSharedPreference().initialize(context);

        Context.Regs.Builder builder = Context.Regs.newBuilder();
        assertFalse(builder.getGdpr());
        userRestrictionParams.build(builder);
        assertFalse(builder.getGdpr());
    }

    @Test
    public void buildRegs_ccpaNotPresentInSharedPreference_extensionIsNotSet() {
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.Regs.Builder builder = Context.Regs.newBuilder();
        userRestrictionParams.build(builder);

        assertEquals(0, builder.getExtProtoCount());
    }

    @Test
    public void buildRegs_ccpaIsEmptyInSharedPreference_extensionIsNotSet() {
        defaultSharedPreferences.edit()
                .putString(IABSharedPreference.IAB_US_PRIVACY_STRING, "")
                .apply();
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.Regs.Builder builder = Context.Regs.newBuilder();
        userRestrictionParams.build(builder);

        assertEquals(0, builder.getExtProtoCount());
    }

    @Test
    public void buildRegs_ccpaIsPresentInSharedPreference_extensionIsSet() throws Exception {
        String ccpaString = "test_string";
        defaultSharedPreferences.edit()
                .putString(IABSharedPreference.IAB_US_PRIVACY_STRING, ccpaString)
                .apply();
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.Regs.Builder builder = Context.Regs.newBuilder();
        userRestrictionParams.build(builder);

        List<Any> extList = builder.getExtProtoList();
        assertEquals(1, extList.size());
        RegsCcpaExtension regsCcpaExtension = extList.get(0).unpack(RegsCcpaExtension.class);
        assertEquals(ccpaString, regsCcpaExtension.getUsPrivacy());
    }

    @Test
    public void buildRegs_ccpaIsPresentInSharedPreferenceButWrongType_extensionNotSet() throws Exception {
        defaultSharedPreferences.edit()
                .putInt(IABSharedPreference.IAB_US_PRIVACY_STRING, 123)
                .apply();
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.Regs.Builder builder = Context.Regs.newBuilder();
        userRestrictionParams.build(builder);

        assertEquals(0, builder.getExtProtoCount());
    }

    @Test
    public void buildRegs_ccpaPassedThroughBidMachine_extensionIsSet() throws Exception {
        String ccpaString = "test_string";
        BidMachine.setUSPrivacyString(ccpaString);
        Context.Regs.Builder builder = Context.Regs.newBuilder();
        userRestrictionParams.build(builder);

        List<Any> extList = builder.getExtProtoList();
        assertEquals(1, extList.size());
        RegsCcpaExtension regsCcpaExtension = extList.get(0).unpack(RegsCcpaExtension.class);
        assertEquals(ccpaString, regsCcpaExtension.getUsPrivacy());
    }

    @Test
    public void buildRegs_ccpaPassedThroughBidMachineAndSharedPreference_extensionIsSet() throws Exception {
        String ccpaString = "test_string";
        BidMachine.setUSPrivacyString(ccpaString);
        defaultSharedPreferences.edit()
                .putString(IABSharedPreference.IAB_US_PRIVACY_STRING, "test_string_2")
                .apply();
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.Regs.Builder builder = Context.Regs.newBuilder();
        userRestrictionParams.build(builder);

        List<Any> extList = builder.getExtProtoList();
        assertEquals(1, extList.size());
        RegsCcpaExtension regsCcpaExtension = extList.get(0).unpack(RegsCcpaExtension.class);
        assertEquals(ccpaString, regsCcpaExtension.getUsPrivacy());
    }

    @Test
    public void isUserInCcpaScope() {
        BidMachine.setUSPrivacyString(null);
        assertFalse(userRestrictionParams.isUserInCcpaScope());
        BidMachine.setUSPrivacyString("");
        assertFalse(userRestrictionParams.isUserInCcpaScope());
        BidMachine.setUSPrivacyString("test_string");
        assertFalse(userRestrictionParams.isUserInCcpaScope());
        BidMachine.setUSPrivacyString("1---");
        assertFalse(userRestrictionParams.isUserInCcpaScope());
        BidMachine.setUSPrivacyString("1Y--");
        assertTrue(userRestrictionParams.isUserInCcpaScope());
    }

    @Test
    public void isUserHasCcpaConsent() {
        BidMachine.setUSPrivacyString(null);
        assertFalse(userRestrictionParams.isUserHasCcpaConsent());
        BidMachine.setUSPrivacyString("");
        assertFalse(userRestrictionParams.isUserHasCcpaConsent());
        BidMachine.setUSPrivacyString("test_string");
        assertFalse(userRestrictionParams.isUserHasCcpaConsent());
        BidMachine.setUSPrivacyString("1---");
        assertFalse(userRestrictionParams.isUserHasCcpaConsent());
        BidMachine.setUSPrivacyString("1Y--");
        assertFalse(userRestrictionParams.isUserHasCcpaConsent());
        BidMachine.setUSPrivacyString("1YY-");
        assertFalse(userRestrictionParams.isUserHasCcpaConsent());
        BidMachine.setUSPrivacyString("1YN-");
        assertTrue(userRestrictionParams.isUserHasCcpaConsent());
    }

}
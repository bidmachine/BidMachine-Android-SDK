package io.bidmachine;

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
    private DefaultSharedPreferencesEditor defaultSharedPreferences;
    private UserRestrictionParams userRestrictionParams;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        defaultSharedPreferences = new DefaultSharedPreferencesEditor(context);
        userRestrictionParams = BidMachineImpl.get().getUserRestrictionParams();
        BidMachine.setConsentConfig(false, null);
        BidMachine.setSubjectToGDPR(null);
        BidMachine.setUSPrivacyString(null);
    }

    @Test
    public void buildUser_bidMachineAndSharedPrefProvidedAndTCFNotProvidedConsentString_returnBidMachineConsentString() {
        BidMachine.setConsentConfig(true, "bid_machine_consent_string");
        defaultSharedPreferences.setGdprConsent("iab_consent_string");
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.User.Builder builder = Context.User.newBuilder();
        userRestrictionParams.build(builder);

        assertEquals("bid_machine_consent_string", builder.getConsent());
    }

    @Test
    public void buildUser_bidMachineProvidedAndSharedPrefAndTCFNotProvidedConsentString_returnBidMachineConsentString() {
        BidMachine.setConsentConfig(true, "bid_machine_consent_string");
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.User.Builder builder = Context.User.newBuilder();
        userRestrictionParams.build(builder);

        assertEquals("bid_machine_consent_string", builder.getConsent());
    }

    @Test
    public void buildUser_bidMachineAndTCFNotProvidedAndSharedPrefProvidedConsentString_returnIABConsentString() {
        defaultSharedPreferences.setGdprConsent("iab_consent_string");
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.User.Builder builder = Context.User.newBuilder();
        userRestrictionParams.build(builder);

        assertEquals("iab_consent_string", builder.getConsent());
    }

    @Test
    public void buildUser_bidMachineAndSharedPrefAndTCFProvidedConsentString_returnBidMachineConsentString() {
        BidMachine.setConsentConfig(true, "bid_machine_consent_string");
        defaultSharedPreferences
                .setTcfTcString("tcf_string")
                .setGdprConsent("iab_consent_string");
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.User.Builder builder = Context.User.newBuilder();
        userRestrictionParams.build(builder);

        assertEquals("bid_machine_consent_string", builder.getConsent());
    }

    @Test
    public void buildUser_bidMachineAndTCFProvidedAndSharedPrefNotProvidedConsentString_returnBidMachineConsentString() {
        BidMachine.setConsentConfig(true, "bid_machine_consent_string");
        defaultSharedPreferences.setTcfTcString("tcf_string");
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.User.Builder builder = Context.User.newBuilder();
        userRestrictionParams.build(builder);

        assertEquals("bid_machine_consent_string", builder.getConsent());
    }

    @Test
    public void buildUser_bidMachineNotProvidedAndSharedPrefAndTCFProvidedConsentString_returnTCFString() {
        defaultSharedPreferences
                .setTcfTcString("tcf_string")
                .setGdprConsent("iab_consent_string");
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.User.Builder builder = Context.User.newBuilder();
        userRestrictionParams.build(builder);

        assertEquals("tcf_string", builder.getConsent());
    }

    @Test
    public void buildUser_bidMachineAndSharedPrefNotProvidedAndTCFProvidedConsentString_returnTCFString() {
        defaultSharedPreferences.setTcfTcString("tcf_string");
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.User.Builder builder = Context.User.newBuilder();
        userRestrictionParams.build(builder);

        assertEquals("tcf_string", builder.getConsent());
    }

    @Test
    public void buildUser_bidMachineAndSharedPrefNotProvidedConsentString_returnDefaultConsentStringOneString() {
        BidMachine.setConsentConfig(true, null);
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.User.Builder builder = Context.User.newBuilder();
        userRestrictionParams.build(builder);

        assertEquals("1", builder.getConsent());
    }

    @Test
    public void buildUser_bidMachineAndSharedPrefNotProvidedConsentString_returnDefaultConsentStringZeroString() {
        BidMachine.setConsentConfig(false, null);
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.User.Builder builder = Context.User.newBuilder();
        userRestrictionParams.build(builder);

        assertEquals("0", builder.getConsent());
    }

    @Test
    public void buildRegs_bidMachineAndSharedPrefProvidedAndTCFNotProvidedSubjectToGDPR_returnBidMachineSubjectToGDPR() {
        BidMachine.setSubjectToGDPR(true);
        defaultSharedPreferences.setSubjectToGdpr("0");
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.Regs.Builder builder = Context.Regs.newBuilder();
        userRestrictionParams.build(builder);

        assertTrue(builder.getGdpr());
    }

    @Test
    public void buildRegs_bidMachineProvidedAndSharedPrefAndTCFNotProvidedSubjectToGDPR_returnBidMachineSubjectToGDPR() {
        BidMachine.setSubjectToGDPR(true);
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.Regs.Builder builder = Context.Regs.newBuilder();
        userRestrictionParams.build(builder);

        assertTrue(builder.getGdpr());
    }

    @Test
    public void buildRegs_bidMachineAndTCFNotProvidedAndSharedPrefProvidedSubjectToGDPR_returnIABSubjectToGDPR() {
        defaultSharedPreferences.setSubjectToGdpr("1");
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.Regs.Builder builder = Context.Regs.newBuilder();
        userRestrictionParams.build(builder);

        assertTrue(builder.getGdpr());
    }

    @Test
    public void buildRegs_bidMachineAndSharedPrefAndTCFNotProvidedSubjectToGDPR_returnDefaultSubjectToGDPR() {
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.Regs.Builder builder = Context.Regs.newBuilder();
        userRestrictionParams.build(builder);

        assertFalse(builder.getGdpr());
    }

    @Test
    public void buildRegs_bidMachineAndSharedPrefAndTCFProvidedSubjectToGDPR_returnBidMachineSubjectToGDPR() {
        BidMachine.setSubjectToGDPR(true);
        defaultSharedPreferences
                .setTcfGdprApplies(0)
                .setSubjectToGdpr("0");
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.Regs.Builder builder = Context.Regs.newBuilder();
        userRestrictionParams.build(builder);

        assertTrue(builder.getGdpr());
    }

    @Test
    public void buildRegs_bidMachineAndTCFProvidedAndSharedPrefNotProvidedSubjectToGDPR_returnBidMachineSubjectToGDPR() {
        BidMachine.setSubjectToGDPR(true);
        defaultSharedPreferences.setTcfGdprApplies(0);
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.Regs.Builder builder = Context.Regs.newBuilder();
        userRestrictionParams.build(builder);

        assertTrue(builder.getGdpr());
    }

    @Test
    public void buildRegs_bidMachineNotProvidedAndSharedPrefAndTCFProvidedSubjectToGDPR_returnTCFApplies() {
        defaultSharedPreferences
                .setTcfGdprApplies(1)
                .setSubjectToGdpr("0");
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.Regs.Builder builder = Context.Regs.newBuilder();
        userRestrictionParams.build(builder);

        assertTrue(builder.getGdpr());
    }

    @Test
    public void buildRegs_bidMachineAndSharedPrefNotProvidedAndTCFProvidedSubjectToGDPR_returnTCFApplies() {
        defaultSharedPreferences.setTcfGdprApplies(1);
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.Regs.Builder builder = Context.Regs.newBuilder();
        userRestrictionParams.build(builder);

        assertTrue(builder.getGdpr());
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
        defaultSharedPreferences.setUsPrivacy("");
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.Regs.Builder builder = Context.Regs.newBuilder();
        userRestrictionParams.build(builder);

        assertEquals(0, builder.getExtProtoCount());
    }

    @Test
    public void buildRegs_ccpaIsPresentInSharedPreference_extensionIsSet() throws Exception {
        String ccpaString = "test_string";
        defaultSharedPreferences.setUsPrivacy(ccpaString);
        BidMachineImpl.get().getIabSharedPreference().initialize(context);
        Context.Regs.Builder builder = Context.Regs.newBuilder();
        userRestrictionParams.build(builder);

        List<Any> extList = builder.getExtProtoList();
        assertEquals(1, extList.size());
        RegsCcpaExtension regsCcpaExtension = extList.get(0).unpack(RegsCcpaExtension.class);
        assertEquals(ccpaString, regsCcpaExtension.getUsPrivacy());
    }

    @Test
    public void buildRegs_ccpaIsPresentInSharedPreferenceButWrongType_extensionNotSet() {
        defaultSharedPreferences.setCustomInt(IABSharedPreference.IAB_US_PRIVACY_STRING, 123);
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
        defaultSharedPreferences.setUsPrivacy("test_string_2");
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
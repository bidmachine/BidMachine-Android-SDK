package io.bidmachine;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.bidmachine.core.Logger;

public class BidMachine {

    public static final String NAME = "BidMachine";
    public static final String VERSION = BuildConfig.VERSION_NAME;

    private static final String TAG = BidMachine.class.getSimpleName();

    /**
     * Initializes BidMachine SDK
     *
     * @param context  - your application context
     * @param sellerId - your Seller Id
     */
    public static void initialize(@NonNull Context context, @NonNull String sellerId) {
        initialize(context, sellerId, null);
    }

    /**
     * Initializes BidMachine SDK
     *
     * @param context  - your application context
     * @param sellerId - your Seller Id
     * @param callback - you {@link InitializationCallback}
     */
    public static void initialize(@NonNull Context context,
                                  @NonNull String sellerId,
                                  @Nullable InitializationCallback callback) {
        Logger.log(TAG, String.format("initialize - %s", sellerId));
        BidMachineImpl.get().initialize(context, sellerId, callback);
    }

    /**
     * Checks if BidMachine SDK was initialized
     *
     * @return - {@code true} if BidMachine SDK was already initialized
     */
    public static boolean isInitialized() {
        return BidMachineImpl.get().isInitialized();
    }

    /**
     * Sets BidMachine SDK endpoint
     *
     * @param url - BidMachine endpoint URL
     */
    public static void setEndpoint(@NonNull String url) {
        Logger.log(TAG, String.format("setEndpoint - %s", url));
        BidMachineImpl.get().setEndpoint(url);
    }

    /**
     * Sets BidMachine SDK logs enabled
     *
     * @param enabled - if {@code true} SDK will print all information about ad requests
     */
    public static void setLoggingEnabled(boolean enabled) {
        if (enabled) {
            Logger.setLoggingEnabled(true);
            Logger.log(TAG, "setLoggingEnabled - true");
        } else {
            Logger.log(TAG, "setLoggingEnabled - false");
            Logger.setLoggingEnabled(false);
        }
        NetworkRegistry.setLoggingEnabled(enabled);
    }

    /**
     * Sets BidMachine SDK test mode
     *
     * @param testMode = if {@code true} SDK will run in test mode
     */
    public static void setTestMode(boolean testMode) {
        Logger.log(TAG, String.format("setTestMode - %s", testMode));
        BidMachineImpl.get().setTestMode(testMode);
    }

    /**
     * Sets default {@link TargetingParams} for all ad requests
     */
    public static void setTargetingParams(@Nullable TargetingParams targetingParams) {
        Logger.log(TAG, "setTargetingParams");
        BidMachineImpl.get().setTargetingParams(targetingParams);
    }

    /**
     * Sets consent config
     *
     * @param hasConsent    - user has given consent to the processing of personal data relating to him or her. https://www.eugdpr.org/
     * @param consentString - GDPR consent string if applicable, complying with the comply with the IAB standard
     *                      <a href="https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/Consent%20string%20and%20vendor%20list%20formats%20v1.1%20Final.md">Consent String Format</a>
     *                      in the <a href="https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework">Transparency and Consent Framework</a> technical specifications
     */
    public static void setConsentConfig(boolean hasConsent, @Nullable String consentString) {
        Logger.log(TAG, String.format("setConsentConfig - %s, %s", hasConsent, consentString));
        BidMachineImpl.get().getUserRestrictionParams().setConsentConfig(hasConsent, consentString);
    }

    /**
     * Sets subject to GDPR
     *
     * @param subject - Flag indicating if GDPR regulations should be applied. <a href="https://wikipedia.org/wiki/General_Data_Protection_Regulation">The  General Data Protection Regulation (GDPR)</a> is a regulation of the European Union
     */
    public static void setSubjectToGDPR(@Nullable Boolean subject) {
        Logger.log(TAG, String.format("setSubjectToGDPR - %s", subject));
        BidMachineImpl.get().getUserRestrictionParams().setSubjectToGDPR(subject);
    }

    /**
     * Sets coppa
     *
     * @param coppa - Flag indicating if COPPA regulations should be applied. <a href="https://wikipedia.org/wiki/Children%27s_Online_Privacy_Protection_Act">The Children's Online Privacy Protection Act (COPPA)</a> was established by the U.S. Federal Trade Commission
     */
    public static void setCoppa(@Nullable Boolean coppa) {
        Logger.log(TAG, String.format("setCoppa - %s", coppa));
        BidMachineImpl.get().getUserRestrictionParams().setCoppa(coppa);
    }

    /**
     * Sets US Privacy string
     *
     * @param usPrivacyString - CCPA string if applicable, complying with the comply with the IAB standard
     *                        <a href="https://github.com/InteractiveAdvertisingBureau/USPrivacy/blob/master/CCPA/US%20Privacy%20String.md">CCPA String Format</a>
     */
    public static void setUSPrivacyString(@Nullable String usPrivacyString) {
        Logger.log(TAG, String.format("setUSPrivacyString - %s", usPrivacyString));
        BidMachineImpl.get().getUserRestrictionParams().setUSPrivacyString(usPrivacyString);
    }

    /**
     * Sets publisher information
     *
     * @param publisher instance of {@link Publisher} which contains all information about publisher
     */
    public static void setPublisher(@Nullable Publisher publisher) {
        Logger.log(TAG, "setPublisher");
        BidMachineImpl.get().setPublisher(publisher);
    }

    /**
     * Adds 3rd party network configuration, which will be used for mediation, loading and displaying ads
     *
     * @param networkConfigs - Custom configuration object per network
     */
    public static void registerNetworks(@NonNull NetworkConfig... networkConfigs) {
        Logger.log(TAG, "registerNetworks with NetworkConfig array");
        NetworkRegistry.registerNetworks(networkConfigs);
    }

    /**
     * Adds 3rd party network configuration, which will be used for mediation, loading and displaying ads
     *
     * @param context  - your application context
     * @param jsonData - Json array which contains info about required networks
     */
    public static void registerNetworks(@NonNull Context context, @NonNull String jsonData) {
        Logger.log(TAG, "registerNetworks with JSON string");
        NetworkRegistry.registerNetworks(context, jsonData);
    }

    public static void registerAdRequestListener(@NonNull AdRequest.AdRequestListener adRequestListener) {
        Logger.log(TAG, "registerAdRequestListener");
        BidMachineImpl.get().registerAdRequestListener(adRequestListener);
    }

    public static void unregisterAdRequestListener(@NonNull AdRequest.AdRequestListener adRequestListener) {
        Logger.log(TAG, "unregisterAdRequestListener");
        BidMachineImpl.get().unregisterAdRequestListener(adRequestListener);
    }

}
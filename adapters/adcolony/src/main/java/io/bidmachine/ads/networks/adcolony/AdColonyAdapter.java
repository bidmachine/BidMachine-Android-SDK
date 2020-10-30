package io.bidmachine.ads.networks.adcolony;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAppOptions;
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.adcolony.sdk.AdColonyZone;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import io.bidmachine.AdsFormat;
import io.bidmachine.AdsType;
import io.bidmachine.ContextProvider;
import io.bidmachine.HeaderBiddingAdRequestParams;
import io.bidmachine.HeaderBiddingAdapter;
import io.bidmachine.HeaderBiddingCollectParamsCallback;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfigParams;
import io.bidmachine.core.Utils;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.TargetingInfo;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.utils.BMError;

class AdColonyAdapter extends NetworkAdapter implements HeaderBiddingAdapter {

    private static final String AD_COLONY_VERSION = "4.2.2";
    private static HashSet<String> zonesCache = new HashSet<>();
    private boolean isAdapterInitialized = false;

    AdColonyAdapter() {
        super("adcolony",
              obtainAdColonyVersion(),
              BuildConfig.VERSION_NAME,
              new AdsType[]{AdsType.Interstitial, AdsType.Rewarded});
    }

    @Override
    public UnifiedFullscreenAd createInterstitial() {
        return new AdColonyFullscreenAd(false);
    }

    @Override
    public UnifiedFullscreenAd createRewarded() {
        return new AdColonyFullscreenAd(true);
    }

    @Override
    protected void onInitialize(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull NetworkConfigParams networkConfigParams) {
        super.onInitialize(contextProvider, adRequestParams, networkConfigParams);
        EnumMap<AdsFormat, List<Map<String, String>>> mediationConfigs =
                networkConfigParams.obtainNetworkMediationConfigs(AdsFormat.values());
        if (mediationConfigs != null) {
            for (List<Map<String, String>> configList : mediationConfigs.values()) {
                if (configList == null) {
                    continue;
                }
                for (Map<String, String> config : configList) {
                    extractZoneId(config);
                }
            }
        }
    }

    @Override
    public void collectHeaderBiddingParams(@NonNull ContextProvider contextProvider,
                                           @NonNull UnifiedAdRequestParams adRequestParams,
                                           @NonNull HeaderBiddingAdRequestParams hbAdRequestParams,
                                           @NonNull final HeaderBiddingCollectParamsCallback collectCallback,
                                           @NonNull Map<String, String> mediationConfig) throws Exception {
        String appId = mediationConfig.get(AdColonyConfig.KEY_APP_ID);
        if (TextUtils.isEmpty(appId)) {
            collectCallback.onCollectFail(BMError.requestError("App id not provided"));
            return;
        }
        assert appId != null;
        String zoneId = extractZoneId(mediationConfig);
        if (TextUtils.isEmpty(zoneId)) {
            collectCallback.onCollectFail(BMError.requestError("Zone id not provided"));
            return;
        }
        assert zoneId != null;
        String storeId = mediationConfig.get(AdColonyConfig.KEY_STORE_ID);
        if (TextUtils.isEmpty(storeId)) {
            collectCallback.onCollectFail(BMError.requestError("Store id not provided"));
            return;
        }
        assert storeId != null;
        synchronized (AdColonyAdapter.class) {
            if (!isAdapterInitialized) {
                AdColony.configure(
                        (Application) contextProvider.getContext().getApplicationContext(),
                        createAppOptions(contextProvider.getContext(), adRequestParams, storeId),
                        appId,
                        zonesCache.toArray(new String[0]));
                if (!isAdColonyConfigured()) {
                    collectCallback.onCollectFail(BMError.TimeoutError);
                    return;
                }
                isAdapterInitialized = true;
            }

            final Map<String, String> params = new HashMap<>();
            params.put(AdColonyConfig.KEY_APP_ID, appId);
            params.put(AdColonyConfig.KEY_ZONE_ID, zoneId);

            AdColonyZone zone = AdColony.getZone(zoneId);
            if (zone != null && zone.isValid()) {
                collectCallback.onCollectFinished(params);
            } else {
                AdColony.requestInterstitial(zoneId, new AdColonyInterstitialListener() {
                    @Override
                    public void onRequestFilled(AdColonyInterstitial adColonyInterstitial) {
                        collectCallback.onCollectFinished(params);
                    }

                    @Override
                    public void onRequestNotFilled(AdColonyZone zone) {
                        collectCallback.onCollectFail(BMError.NoContent);
                    }
                });
            }
        }
    }

    private static AdColonyAppOptions createAppOptions(@NonNull Context context,
                                                       @NonNull UnifiedAdRequestParams adRequestParams,
                                                       @NonNull String storeId) {
        DataRestrictions dataRestrictions = adRequestParams.getDataRestrictions();
        TargetingInfo targetingInfo = adRequestParams.getTargetingParams();
        AdColonyAppOptions options = AdColony.getAppOptions();
        if (options == null) {
            options = new AdColonyAppOptions();
            AdColony.setAppOptions(options);
        }
        String userId = targetingInfo.getUserId();
        if (userId != null) {
            options.setUserID(userId);
        }
        options.setOriginStore(storeId);

        String appVersion = Utils.getAppVersion(context);
        if (!TextUtils.isEmpty(appVersion)) {
            options.setAppVersion(appVersion);
        }

        options.setPrivacyFrameworkRequired(AdColonyAppOptions.COPPA,
                                            dataRestrictions.isUserAgeRestricted());
        options.setPrivacyFrameworkRequired(AdColonyAppOptions.GDPR,
                                            dataRestrictions.isUserInGdprScope());
        options.setPrivacyConsentString(AdColonyAppOptions.GDPR,
                                        dataRestrictions.getIABGDPRString());
        String usPrivacyString = dataRestrictions.getUSPrivacyString();
        if (!TextUtils.isEmpty(usPrivacyString)) {
            assert usPrivacyString != null;
            options.setPrivacyFrameworkRequired(AdColonyAppOptions.CCPA,
                                                dataRestrictions.isUserInCcpaScope());
            options.setPrivacyConsentString(AdColonyAppOptions.CCPA,
                                            usPrivacyString);
        }

        options.setTestModeEnabled(adRequestParams.isTestMode());
        return options;
    }

    private static String obtainAdColonyVersion() {
        String version = AdColony.getSDKVersion();
        if (TextUtils.isEmpty(version)) {
            return AD_COLONY_VERSION;
        }
        return version;
    }

    private String extractZoneId(Map<String, String> mediationConfig) {
        String zoneId = mediationConfig.get(AdColonyConfig.KEY_ZONE_ID);
        if (TextUtils.isEmpty(zoneId)) {
            return null;
        }
        assert zoneId != null;
        if (zonesCache == null) {
            zonesCache = new HashSet<>();
        }
        if (zonesCache.add(zoneId)) {
            isAdapterInitialized = false;
        }
        return zoneId;
    }

    private boolean isAdColonyConfigured() {
        return !TextUtils.isEmpty(AdColony.getSDKVersion());
    }

}

package io.bidmachine.ads.networks.pangle;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdSdk;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.AdsType;
import io.bidmachine.ContextProvider;
import io.bidmachine.HeaderBiddingAdRequestParams;
import io.bidmachine.HeaderBiddingAdapter;
import io.bidmachine.HeaderBiddingCollectParamsCallback;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfigParams;
import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.utils.BMError;

class PangleAdapter extends NetworkAdapter implements HeaderBiddingAdapter {

    PangleAdapter() {
        super("pangle_sdk",
              BuildConfig.ADAPTER_SDK_VERSION_NAME,
              BuildConfig.ADAPTER_VERSION_NAME,
              new AdsType[]{AdsType.Banner, AdsType.Interstitial, AdsType.Rewarded});
    }

    @Override
    public UnifiedBannerAd createBanner() {
        return new PangleBannerAd();
    }

    @Override
    public UnifiedFullscreenAd createInterstitial() {
        return new PangleInterstitialAd();
    }

    @Override
    public UnifiedFullscreenAd createRewarded() {
        return new PangleRewardedAd();
    }

    @Override
    protected void onInitialize(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull NetworkConfigParams networkConfig) throws Throwable {
        Map<String, String> networkParams = networkConfig.obtainNetworkParams();
        if (networkParams == null) {
            Logger.log(String.format("[%s] %s",
                                     getKey(),
                                     "Initialize failed: network parameters not found"));
            return;
        }
        String appId = networkParams.get(PangleConfig.KEY_APP_ID);
        if (TextUtils.isEmpty(appId)) {
            Logger.log(String.format("[%s] %s",
                                     getKey(),
                                     "Initialize failed: app_id not provided"));
            return;
        }
        assert appId != null;

        configure(adRequestParams);

        final Context context = contextProvider.getApplicationContext();
        final TTAdConfig ttAdConfig = new TTAdConfig.Builder()
                .appId(appId)
                .debug(adRequestParams.isTestMode())
                .build();
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Need to call init on the main thread
                    TTAdSdk.init(context, ttAdConfig, null);
                } catch (Throwable t) {
                    Logger.log(t);
                }
            }
        });
    }

    @Override
    public void collectHeaderBiddingParams(@NonNull ContextProvider contextProvider,
                                           @NonNull UnifiedAdRequestParams adRequestParams,
                                           @NonNull HeaderBiddingAdRequestParams hbAdRequestParams,
                                           @NonNull final HeaderBiddingCollectParamsCallback collectCallback,
                                           @NonNull Map<String, String> mediationConfig) throws Exception {
        if (!TTAdSdk.isInitSuccess()) {
            collectCallback.onCollectFail(BMError.NotInitialized);
            return;
        }
        final String appId = mediationConfig.get(PangleConfig.KEY_APP_ID);
        if (TextUtils.isEmpty(appId)) {
            collectCallback.onCollectFail(BMError.requestError("app_id not provided"));
            return;
        }
        assert appId != null;
        final String slotId = mediationConfig.get(PangleConfig.KEY_SLOT_ID);
        if (TextUtils.isEmpty(slotId)) {
            collectCallback.onCollectFail(BMError.requestError("slot_id not provided"));
            return;
        }
        assert slotId != null;

        configure(adRequestParams);

        String biddingToken = TTAdSdk.getAdManager().getBiddingToken();
        if (TextUtils.isEmpty(biddingToken)) {
            collectCallback.onCollectFail(
                    BMError.paramError("BiddingToken from Pangle SDK is null or empty"));
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put(PangleConfig.KEY_APP_ID, appId);
        params.put(PangleConfig.KEY_SLOT_ID, slotId);
        params.put(PangleConfig.KEY_BID_TOKEN, biddingToken);
        collectCallback.onCollectFinished(params);
    }

    private static void configure(@NonNull UnifiedAdRequestParams adRequestParams) {
//        Fields to indicate SDK whether the user is a child or an adult:
//        adult = 0, child = 1
        TTAdSdk.setCoppa(adRequestParams.getDataRestrictions().isUserAgeRestricted() ? 1 : 0);

//        Set gdpr = 0 if user grant consent for advertising,
//        Set gdpr = 1 if user doesn't grant consent for advertising.
        TTAdSdk.setGdpr(adRequestParams.getDataRestrictions().isUserGdprProtected() ? 1 : 0);
    }

}
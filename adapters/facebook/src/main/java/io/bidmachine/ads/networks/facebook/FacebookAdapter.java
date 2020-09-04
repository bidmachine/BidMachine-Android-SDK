package io.bidmachine.ads.networks.facebook;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.BidderTokenProvider;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.AdsType;
import io.bidmachine.BidMachine;
import io.bidmachine.ContextProvider;
import io.bidmachine.HeaderBiddingAdRequestParams;
import io.bidmachine.HeaderBiddingAdapter;
import io.bidmachine.HeaderBiddingCollectParamsCallback;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfigParams;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.utils.BMError;

class FacebookAdapter extends NetworkAdapter implements HeaderBiddingAdapter {

    @Nullable
    private static String bidderToken = null;

    FacebookAdapter() {
        super("facebook",
              com.facebook.ads.BuildConfig.VERSION_NAME,
              BuildConfig.VERSION_NAME,
              new AdsType[]{AdsType.Banner, AdsType.Interstitial, AdsType.Rewarded});
    }

    @Override
    public UnifiedBannerAd createBanner() {
        return new FacebookBanner();
    }

    @Override
    public UnifiedFullscreenAd createInterstitial() {
        return new FacebookInterstitial();
    }

    @Override
    public UnifiedFullscreenAd createRewarded() {
        return new FacebookRewarded();
    }

    @Override
    protected void onInitialize(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull NetworkConfigParams networkConfig) {
        super.onInitialize(contextProvider, adRequestParams, networkConfig);
        configure(adRequestParams);
        AudienceNetworkAds.initialize(contextProvider.getContext());
        initializeFacebook(contextProvider.getContext(), null);
    }

    private static void configure(@NonNull UnifiedAdRequestParams adRequestParams) {
        DataRestrictions dataRestrictions = adRequestParams.getDataRestrictions();
        AdSettings.setMediationService(BidMachine.NAME);
        AdSettings.setMixedAudience(dataRestrictions.isUserAgeRestricted());
        if (dataRestrictions.isUserInCcpaScope()) {
            if (dataRestrictions.isUserHasCcpaConsent()) {
                AdSettings.setDataProcessingOptions(new String[]{});
            } else {
                AdSettings.setDataProcessingOptions(new String[]{"LDU"}, 0, 0);
            }
        }
        if (adRequestParams.isTestMode()) {
            AdSettings.setTestAdType(AdSettings.TestAdType.DEFAULT);
        }
    }

    @Override
    public void collectHeaderBiddingParams(@NonNull ContextProvider contextProvider,
                                           @NonNull UnifiedAdRequestParams adRequestParams,
                                           @NonNull HeaderBiddingAdRequestParams hbAdRequestParams,
                                           @NonNull final HeaderBiddingCollectParamsCallback collectCallback,
                                           @NonNull Map<String, String> mediationConfig) {
        final String appId = mediationConfig.get(FacebookConfig.KEY_APP_ID);
        if (TextUtils.isEmpty(appId)) {
            collectCallback.onCollectFail(BMError.requestError("app_id not provided"));
            return;
        }
        assert appId != null;
        final String placementId = mediationConfig.get(FacebookConfig.KEY_PLACEMENT_ID);
        if (TextUtils.isEmpty(placementId)) {
            collectCallback.onCollectFail(BMError.requestError("placement_id (facebook_key) not provided"));
            return;
        }
        assert placementId != null;
        initializeFacebook(contextProvider.getContext(), new FacebookInitializeListener() {
            @Override
            public void onInitialized(@NonNull String bidderToken) {
                Map<String, String> params = new HashMap<>();
                params.put(FacebookConfig.KEY_APP_ID, appId);
                params.put(FacebookConfig.KEY_PLACEMENT_ID, placementId);
                params.put(FacebookConfig.KEY_TOKEN, bidderToken);
                collectCallback.onCollectFinished(params);
            }

            @Override
            public void onInitializationFailed() {
                collectCallback.onCollectFail(BMError.Internal);
            }
        });
    }

    private static synchronized void initializeFacebook(@NonNull final Context context,
                                                        @Nullable final FacebookInitializeListener listener) {
        if (!TextUtils.isEmpty(bidderToken)) {
            if (listener != null) {
                assert bidderToken != null;
                listener.onInitialized(bidderToken);
            }
        } else {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    synchronized (FacebookAdapter.class) {
                        if (!TextUtils.isEmpty(bidderToken)) {
                            if (listener != null) {
                                assert bidderToken != null;
                                listener.onInitialized(bidderToken);
                            }
                            return;
                        }
                        if (TextUtils.isEmpty(bidderToken)) {
                            try {
                                bidderToken = BidderTokenProvider.getBidderToken(context);
                            } catch (Throwable ignore) {
                            }
                        }
                        if (listener != null) {
                            if (TextUtils.isEmpty(bidderToken)) {
                                listener.onInitializationFailed();
                            } else {
                                assert bidderToken != null;
                                listener.onInitialized(bidderToken);
                            }
                        }
                    }
                }
            }.start();
        }
    }

    private interface FacebookInitializeListener {
        void onInitialized(@NonNull String bidderToken);

        void onInitializationFailed();
    }
}

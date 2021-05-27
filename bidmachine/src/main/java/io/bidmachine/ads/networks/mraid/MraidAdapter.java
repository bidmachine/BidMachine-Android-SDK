package io.bidmachine.ads.networks.mraid;

import androidx.annotation.NonNull;

import com.explorestack.iab.mraid.MraidActivity;
import com.explorestack.iab.mraid.MraidLog;
import com.explorestack.iab.utils.Logger;

import io.bidmachine.AdsType;
import io.bidmachine.BuildConfig;
import io.bidmachine.ContextProvider;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfigParams;
import io.bidmachine.measurer.OMSDKSettings;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedFullscreenAd;

public class MraidAdapter extends NetworkAdapter {

    public static final String KEY = "mraid";

    public MraidAdapter() {
        super(KEY,
              "2.0",
              BuildConfig.VERSION_NAME + ".1",
              new AdsType[]{AdsType.Banner, AdsType.Interstitial, AdsType.Rewarded});
    }

    @Override
    public void setLogging(boolean enabled) throws Throwable {
        MraidLog.setLoggingLevel(enabled ? Logger.LogLevel.debug : Logger.LogLevel.none);
    }

    @Override
    protected void onInitialize(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull NetworkConfigParams networkConfigParams) throws Throwable {
        super.onInitialize(contextProvider, adRequestParams, networkConfigParams);

        OMSDKSettings.initialize(contextProvider.getContext());
    }

    @Override
    public UnifiedBannerAd createBanner() {
        return new MraidBannerAd();
    }

    @Override
    public UnifiedFullscreenAd createInterstitial() {
        return new MraidFullScreenAd(MraidActivity.MraidType.Static);
    }

    @Override
    public UnifiedFullscreenAd createRewarded() {
        return new MraidFullScreenAd(MraidActivity.MraidType.Rewarded);
    }

}
package io.bidmachine.ads.networks.mraid;

import com.explorestack.iab.mraid.MraidActivity;
import com.explorestack.iab.mraid.MraidLog;
import com.explorestack.iab.utils.Logger;

import io.bidmachine.AdsType;
import io.bidmachine.BuildConfig;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedFullscreenAd;

public class MraidAdapter extends NetworkAdapter {

    public MraidAdapter() {
        super("mraid",
              "2.0",
              BuildConfig.VERSION_NAME + ".1",
              new AdsType[]{AdsType.Banner, AdsType.Interstitial, AdsType.Rewarded});
    }

    @Override
    public void setLogging(boolean enabled) {
        MraidLog.setLoggingLevel(enabled ? Logger.LogLevel.debug : Logger.LogLevel.none);
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

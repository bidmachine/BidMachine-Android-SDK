package io.bidmachine.ads.networks.mraid;

import com.explorestack.iab.mraid.internal.MRAIDLog;
import com.explorestack.iab.vast.VideoType;
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
        MRAIDLog.setLoggingLevel(enabled ? MRAIDLog.LOG_LEVEL.verbose : MRAIDLog.LOG_LEVEL.none);
    }

    @Override
    public UnifiedBannerAd createBanner() {
        return new MraidBannerAd();
    }

    @Override
    public UnifiedFullscreenAd createInterstitial() {
        return new MraidFullScreenAd(VideoType.NonRewarded);
    }

    @Override
    public UnifiedFullscreenAd createRewarded() {
        return new MraidFullScreenAd(VideoType.Rewarded);
    }

}

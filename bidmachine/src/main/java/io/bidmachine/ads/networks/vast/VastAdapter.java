package io.bidmachine.ads.networks.vast;

import android.support.annotation.NonNull;

import com.explorestack.iab.utils.Logger;
import com.explorestack.iab.vast.VastLog;
import com.explorestack.iab.vast.VideoType;

import io.bidmachine.AdsType;
import io.bidmachine.BuildConfig;
import io.bidmachine.ContextProvider;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfigParams;
import io.bidmachine.measurer.BMIABMeasurer;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedFullscreenAd;

public class VastAdapter extends NetworkAdapter {

    public VastAdapter() {
        super("vast",
              "2.0",
              BuildConfig.VERSION_NAME + ".1",
              new AdsType[]{AdsType.Interstitial, AdsType.Rewarded});
    }

    @Override
    public void setLogging(boolean enabled) {
        VastLog.setLoggingLevel(enabled ? Logger.LogLevel.debug : Logger.LogLevel.none);
    }

    @Override
    public UnifiedFullscreenAd createInterstitial() {
        return new VastFullScreenAd(VideoType.NonRewarded);
    }

    @Override
    public UnifiedFullscreenAd createRewarded() {
        return new VastFullScreenAd(VideoType.Rewarded);
    }

    @Override
    protected void onInitialize(@NonNull final ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParams adRequestParams,
                                @NonNull NetworkConfigParams networkConfigParams) {
        super.onInitialize(contextProvider, adRequestParams, networkConfigParams);

        BMIABMeasurer.initialize(contextProvider.getContext(), null);
    }
}

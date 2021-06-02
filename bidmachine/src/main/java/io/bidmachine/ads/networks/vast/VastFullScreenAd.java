package io.bidmachine.ads.networks.vast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.explorestack.iab.vast.VastRequest;
import com.explorestack.iab.vast.VideoType;

import io.bidmachine.ContextProvider;
import io.bidmachine.measurer.VastOMSDKAdMeasurer;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

class VastFullScreenAd extends UnifiedFullscreenAd {

    @NonNull
    private final VideoType videoType;

    @Nullable
    private VastRequest vastRequest;
    private VastFullScreenAdLoadListener vastAdLoadListener;
    private VastFullScreenAdShowListener vastAdShowListener;
    @Nullable
    private VastOMSDKAdMeasurer vastOMSDKAdMeasurer;

    VastFullScreenAd(@NonNull VideoType videoType) {
        this.videoType = videoType;
    }

    @Override
    public void load(@NonNull ContextProvider contextProvider,
                     @NonNull UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) throws Throwable {
        VastParams vastParams = new VastParams(mediationParams);
        if (!vastParams.isValid(callback)) {
            return;
        }
        assert vastParams.creativeAdm != null;

        if (vastParams.omsdkEnabled) {
            vastOMSDKAdMeasurer = new VastOMSDKAdMeasurer();
        }
        vastAdLoadListener = new VastFullScreenAdLoadListener(callback, vastOMSDKAdMeasurer);
        vastRequest = VastRequest.newBuilder()
                .setPreCache(true)
                .setVideoCloseTime(vastParams.skipOffset)
                .setCompanionCloseTime(vastParams.companionSkipOffset)
                .forceUseNativeCloseTime(vastParams.useNativeClose)
                .build();
        assert vastRequest != null;
        vastRequest.loadVideoWithData(contextProvider.getContext(),
                                      vastParams.creativeAdm,
                                      vastAdLoadListener);
    }

    @Override
    public void show(@NonNull ContextProvider contextProvider,
                     @NonNull UnifiedFullscreenAdCallback callback) throws Throwable {
        if (vastRequest != null && vastRequest.checkFile()) {
            vastAdShowListener = new VastFullScreenAdShowListener(callback, vastOMSDKAdMeasurer);
            vastRequest.display(contextProvider.getContext(),
                                videoType,
                                vastAdShowListener,
                                vastOMSDKAdMeasurer,
                                vastOMSDKAdMeasurer);
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void onDestroy() {
        vastAdLoadListener = null;
        vastAdShowListener = null;
        if (vastOMSDKAdMeasurer != null) {
            vastOMSDKAdMeasurer.destroy();
            vastOMSDKAdMeasurer = null;
        }
        if (vastRequest != null) {
            vastRequest = null;
        }
    }

}
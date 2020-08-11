package io.bidmachine.ads.networks.vast;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.explorestack.iab.vast.VastRequest;
import com.explorestack.iab.vast.VideoType;

import io.bidmachine.ContextProvider;
import io.bidmachine.measurer.vast.VastIABMeasurer;
import io.bidmachine.measurer.vast.VastWrapperListener;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

class VastFullScreenAd extends UnifiedFullscreenAd {

    @NonNull
    private VideoType videoType;
    @Nullable
    private VastRequest vastRequest;
    private VastIABMeasurer vastIABMeasurer;
    private VastWrapperListener wrappedListener;

    VastFullScreenAd(@NonNull VideoType videoType) {
        this.videoType = videoType;
    }

    @Override
    public void load(@NonNull ContextProvider contextProvider,
                     @NonNull UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) {
        VastParams vastParams = new VastParams(mediationParams);
        if (!vastParams.isValid(callback)) {
            return;
        }
        assert vastParams.creativeAdm != null;

        vastIABMeasurer = new VastIABMeasurer();
        VastFullScreenAdapterListener vastRequestListener =
                new VastFullScreenAdapterListener(callback);
        wrappedListener = new VastWrapperListener(vastIABMeasurer,
                                                  vastRequestListener,
                                                  null);
        vastRequest = VastRequest.newBuilder()
                .setPreCache(true)
                .setVideoCloseTime(vastParams.skipOffset)
                .setCompanionCloseTime(vastParams.companionSkipOffset)
                .forceUseNativeCloseTime(vastParams.useNativeClose)
                .build();
        assert vastRequest != null;
        vastRequest.loadVideoWithData(contextProvider.getContext(),
                                      vastParams.creativeAdm,
                                      vastRequestListener);
    }

    @Override
    public void show(@NonNull Context context, @NonNull UnifiedFullscreenAdCallback callback) {
        if (vastRequest != null && vastRequest.checkFile()) {
            vastRequest.display(context, videoType, wrappedListener, wrappedListener);
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void onDestroy() {
        if (vastRequest != null) {
            vastRequest = null;
        }
    }

}

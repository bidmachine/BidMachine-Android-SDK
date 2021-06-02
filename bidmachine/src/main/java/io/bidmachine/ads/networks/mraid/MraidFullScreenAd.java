package io.bidmachine.ads.networks.mraid;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.explorestack.iab.mraid.MraidActivity;
import com.explorestack.iab.mraid.MraidInterstitial;

import io.bidmachine.ContextProvider;
import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.measurer.MraidOMSDKAdMeasurer;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

class MraidFullScreenAd extends UnifiedFullscreenAd {

    private final MraidActivity.MraidType mraidType;

    @Nullable
    private MraidInterstitial mraidInterstitial;
    @Nullable
    private MraidOMSDKAdMeasurer mraidOMSDKAdMeasurer;

    MraidFullScreenAd(MraidActivity.MraidType mraidType) {
        this.mraidType = mraidType;
    }

    @Override
    public void load(@NonNull final ContextProvider contextProvider,
                     @NonNull final UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) throws Throwable {
        final Activity activity = contextProvider.getActivity();
        if (activity == null) {
            callback.onAdLoadFailed(BMError.requestError("Activity not provided"));
            return;
        }
        final MraidParams mraidParams = new MraidParams(mediationParams);
        if (!mraidParams.isValid(callback)) {
            return;
        }
        assert mraidParams.creativeAdm != null;

        final String creativeAdm;
        if (mraidParams.omsdkEnabled) {
            mraidOMSDKAdMeasurer = new MraidOMSDKAdMeasurer();
            creativeAdm = mraidOMSDKAdMeasurer.injectMeasurerJS(mraidParams.creativeAdm);
        } else {
            creativeAdm = mraidParams.creativeAdm;
        }
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mraidInterstitial = MraidInterstitial.newBuilder()
                            .setPreload(true)
                            .setCloseTime(mraidParams.skipOffset)
                            .forceUseNativeCloseButton(mraidParams.useNativeClose)
                            .setListener(new MraidFullScreenAdListener(contextProvider,
                                                                       callback,
                                                                       mraidOMSDKAdMeasurer))
                            .setR1(mraidParams.r1)
                            .setR2(mraidParams.r2)
                            .setDurationSec(mraidParams.progressDuration)
                            .setProductLink(mraidParams.storeUrl)
                            .setCloseStyle(mraidParams.closeableViewStyle)
                            .setCountDownStyle(mraidParams.countDownStyle)
                            .setProgressStyle(mraidParams.progressStyle)
                            .setAdMeasurer(mraidOMSDKAdMeasurer)
                            .build(contextProvider.getContext());
                    mraidInterstitial.load(creativeAdm);
                } catch (Throwable t) {
                    Logger.log(t);
                    callback.onAdLoadFailed(BMError.Internal);
                }
            }
        });
    }

    @Override
    public void show(@NonNull ContextProvider contextProvider,
                     @NonNull UnifiedFullscreenAdCallback callback) throws Throwable {
        if (mraidInterstitial != null && mraidInterstitial.isReady()) {
            mraidInterstitial.show(contextProvider.getContext(), mraidType);
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void onDestroy() {
        if (mraidOMSDKAdMeasurer != null) {
            mraidOMSDKAdMeasurer.destroy(new Runnable() {
                @Override
                public void run() {
                    if (mraidInterstitial != null) {
                        mraidInterstitial.destroy();
                        mraidInterstitial = null;
                    }
                }
            });
            mraidOMSDKAdMeasurer = null;
        }
    }

}
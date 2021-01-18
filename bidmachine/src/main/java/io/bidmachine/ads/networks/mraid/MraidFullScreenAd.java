package io.bidmachine.ads.networks.mraid;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.explorestack.iab.mraid.MraidActivity;
import com.explorestack.iab.mraid.MraidInterstitial;

import io.bidmachine.ContextProvider;
import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

class MraidFullScreenAd extends UnifiedFullscreenAd {

    private final MraidActivity.MraidType mraidType;

    @Nullable
    private MraidInterstitial mraidInterstitial;

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

        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mraidInterstitial = MraidInterstitial.newBuilder()
                            .setPreload(true)
                            .setCloseTime(mraidParams.skipOffset)
                            .forceUseNativeCloseButton(mraidParams.useNativeClose)
                            .setListener(new MraidFullScreenAdListener(contextProvider, callback))
                            .setR1(mraidParams.r1)
                            .setR2(mraidParams.r2)
                            .setDurationSec(mraidParams.progressDuration)
                            .setProductLink(mraidParams.storeUrl)
                            .setCloseStyle(mraidParams.closeableViewStyle)
                            .setCountDownStyle(mraidParams.countDownStyle)
                            .setProgressStyle(mraidParams.progressStyle)
                            .build(contextProvider.getContext());
                    mraidInterstitial.load(mraidParams.creativeAdm);
                } catch (Throwable t) {
                    Logger.log(t);
                    callback.onAdLoadFailed(BMError.Internal);
                }
            }
        });
    }

    @Override
    public void show(@NonNull Context context,
                     @NonNull UnifiedFullscreenAdCallback callback) {
        if (mraidInterstitial != null && mraidInterstitial.isReady()) {
            MraidActivity.show(context, mraidInterstitial, mraidType);
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void onDestroy() {
        if (mraidInterstitial != null) {
            mraidInterstitial.destroy();
            mraidInterstitial = null;
        }
    }

}
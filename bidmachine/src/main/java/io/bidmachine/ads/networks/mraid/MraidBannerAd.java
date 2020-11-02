package io.bidmachine.ads.networks.mraid;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.explorestack.iab.mraid.MRAIDView;

import io.bidmachine.ContextProvider;
import io.bidmachine.core.Logger;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedBannerAdCallback;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

import static io.bidmachine.core.Utils.onUiThread;

class MraidBannerAd extends UnifiedBannerAd {

    @Nullable
    MRAIDView mraidView;

    @Override
    public void load(@NonNull final ContextProvider contextProvider,
                     @NonNull final UnifiedBannerAdCallback callback,
                     @NonNull UnifiedBannerAdRequestParams requestParams,
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
        final MraidBannerAdListener mraidBannerAdListener =
                new MraidBannerAdListener(this, callback);
        onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mraidView = new MRAIDView.builder(activity,
                                                      mraidParams.creativeAdm,
                                                      mraidParams.width,
                                                      mraidParams.height)
                            .setPreload(true)
                            .setListener(mraidBannerAdListener)
                            .setNativeFeatureListener(mraidBannerAdListener)
                            .build();
                    mraidView.load();
                } catch (Throwable t) {
                    Logger.log(t);
                    callback.onAdLoadFailed(BMError.Internal);
                }
            }
        });
    }

    void processMraidViewLoaded(@NonNull UnifiedBannerAdCallback callback) {
        if (mraidView != null && mraidView.getParent() == null) {
            mraidView.show();
            callback.onAdLoaded(mraidView);
        } else {
            callback.onAdLoadFailed(BMError.Internal);
        }
    }

    @Override
    public void onDestroy() {
        if (mraidView != null) {
            mraidView.destroy();
            mraidView = null;
        }
    }

}
package io.bidmachine.ads.networks.mraid;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.explorestack.iab.mraid.MRAIDView;

import io.bidmachine.ContextProvider;
import io.bidmachine.core.Utils;
import io.bidmachine.measurer.mraid.MraidIABMeasurer;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedBannerAdCallback;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

import static io.bidmachine.core.Utils.onUiThread;

class MraidBannerAd extends UnifiedBannerAd {

    private MraidIABMeasurer mraidIABMeasurer;
    private MraidBannerAdListener adListener;
    @Nullable
    MRAIDView mraidView;

    @Override
    public void load(@NonNull final ContextProvider contextProvider,
                     @NonNull UnifiedBannerAdCallback callback,
                     @NonNull UnifiedBannerAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) {
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

        mraidIABMeasurer = new MraidIABMeasurer();
        adListener = new MraidBannerAdListener(this, callback);
        onUiThread(new Runnable() {
            @Override
            public void run() {
                mraidView = mraidIABMeasurer
                        .createMraidViewBuilder(activity,
                                                mraidParams.creativeAdm,
                                                mraidParams.width,
                                                mraidParams.height)
                        .setPreload(true)
                        .setListener(adListener)
                        .setNativeFeatureListener(adListener)
                        .build();
                mraidView.load();
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
    public void onShown() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                if (mraidIABMeasurer != null) {
                    mraidIABMeasurer.shown();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        if (mraidIABMeasurer != null) {
            mraidIABMeasurer.destroy();
        }
        if (mraidView != null) {
            mraidView.destroy();
            mraidView = null;
        }
    }

}
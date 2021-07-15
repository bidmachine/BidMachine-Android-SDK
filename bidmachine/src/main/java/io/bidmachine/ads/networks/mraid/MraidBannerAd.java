package io.bidmachine.ads.networks.mraid;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.explorestack.iab.mraid.MraidView;

import io.bidmachine.ContextProvider;
import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.measurer.MraidOMSDKAdMeasurer;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedBannerAdCallback;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

class MraidBannerAd extends UnifiedBannerAd {

    @Nullable
    private MraidView mraidView;
    @Nullable
    private MraidOMSDKAdMeasurer mraidOMSDKAdMeasurer;

    @Override
    public void load(@NonNull final ContextProvider contextProvider,
                     @NonNull final UnifiedBannerAdCallback callback,
                     @NonNull UnifiedBannerAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) throws Throwable {
        final Activity activity = contextProvider.getActivity();
        if (activity == null) {
            callback.onAdLoadFailed(BMError.internal("Activity is null"));
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
                    mraidView = new MraidView.Builder()
                            .setPreload(true)
                            .setListener(new MraidBannerAdListener(contextProvider, callback))
                            .setAdMeasurer(mraidOMSDKAdMeasurer)
                            .build(contextProvider.getContext());
                    mraidView.load(creativeAdm);
                } catch (Throwable t) {
                    Logger.log(t);
                    callback.onAdLoadFailed(BMError.internal("Exception when loading banner object"));
                }
            }
        });
    }

    @Override
    public void onShown() {
        super.onShown();

        if (mraidOMSDKAdMeasurer != null) {
            mraidOMSDKAdMeasurer.onAdShown();
        }
    }

    @Override
    public void onDestroy() {
        if (mraidOMSDKAdMeasurer != null) {
            mraidOMSDKAdMeasurer.destroy(new Runnable() {
                @Override
                public void run() {
                    if (mraidView != null) {
                        mraidView.destroy();
                        mraidView = null;
                    }
                }
            });
            mraidOMSDKAdMeasurer = null;
        }
    }

}
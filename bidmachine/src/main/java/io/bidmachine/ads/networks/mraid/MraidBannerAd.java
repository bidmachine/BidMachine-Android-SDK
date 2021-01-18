package io.bidmachine.ads.networks.mraid;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.explorestack.iab.mraid.MraidView;

import io.bidmachine.ContextProvider;
import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedBannerAdCallback;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

class MraidBannerAd extends UnifiedBannerAd {

    @Nullable
    private MraidView mraidView;

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

        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    mraidView = new MraidView.Builder()
                            .setPreload(true)
                            .setListener(new MraidBannerAdListener(contextProvider, callback))
                            .build(contextProvider.getContext());
                    mraidView.load(mraidParams.creativeAdm);
                } catch (Throwable t) {
                    Logger.log(t);
                    callback.onAdLoadFailed(BMError.Internal);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        if (mraidView != null) {
            mraidView.destroy();
            mraidView = null;
        }
    }

}
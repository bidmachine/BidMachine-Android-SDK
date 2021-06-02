package io.bidmachine.ads.networks.adcolony;

import androidx.annotation.NonNull;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyAdOptions;
import com.adcolony.sdk.AdColonyInterstitial;

import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

class AdColonyFullscreenAd extends UnifiedFullscreenAd {

    private final boolean isRewarded;

    private AdColonyInterstitial adColonyInterstitial;
    private AdColonyFullscreenAdListener listener;

    AdColonyFullscreenAd(boolean rewarded) {
        isRewarded = rewarded;
    }

    @Override
    public void load(@NonNull ContextProvider context,
                     @NonNull UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) throws Throwable {
        AdColonyParams params = new AdColonyParams(mediationParams);
        if (!params.isValid(callback)) {
            return;
        }
        assert params.zoneId != null;
        assert params.adm != null;

        listener = new AdColonyFullscreenAdListener(params.zoneId, this, callback);
        if (isRewarded) {
            AdColonyRewardListenerWrapper.get().addListener(listener);
        }
        AdColony.requestInterstitial(params.zoneId,
                                     listener,
                                     new AdColonyAdOptions().setOption("adm", params.adm));
    }

    @Override
    public void show(@NonNull ContextProvider contextProvider,
                     @NonNull UnifiedFullscreenAdCallback callback) throws Throwable {
        if (adColonyInterstitial != null && !adColonyInterstitial.isExpired()) {
            adColonyInterstitial.show();
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void onDestroy() {
        if (adColonyInterstitial != null) {
            adColonyInterstitial.destroy();
            adColonyInterstitial = null;
        }
        if (listener != null) {
            if (isRewarded) {
                AdColonyRewardListenerWrapper.get().removeListener(listener);
            }
            listener = null;
        }
    }

    void setAdColonyInterstitial(AdColonyInterstitial adColonyInterstitial) {
        this.adColonyInterstitial = adColonyInterstitial;
    }

}
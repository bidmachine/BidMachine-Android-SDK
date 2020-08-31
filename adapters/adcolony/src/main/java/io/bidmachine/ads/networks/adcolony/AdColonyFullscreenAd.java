package io.bidmachine.ads.networks.adcolony;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyInterstitial;
import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

class AdColonyFullscreenAd extends UnifiedFullscreenAd {

    private AdColonyInterstitial adColonyInterstitial;
    private AdColonyFullscreenAdListener listener;
    private boolean isRewarded;

    AdColonyFullscreenAd(boolean rewarded) {
        isRewarded = rewarded;
    }

    @Override
    public void load(@NonNull ContextProvider context,
                     @NonNull UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) throws Throwable {
        String zoneId = mediationParams.getString("zone_id");
        if (TextUtils.isEmpty(zoneId)) {
            callback.onAdLoadFailed(BMError.requestError("zone id not provided"));
            return;
        }
        assert zoneId != null;
        listener = new AdColonyFullscreenAdListener(zoneId, this, callback);
        if (isRewarded) {
            AdColonyRewardListenerWrapper.get().addListener(listener);
        }
        AdColony.requestInterstitial(zoneId, listener);
    }

    @Override
    public void show(@NonNull Context context,
                     @NonNull UnifiedFullscreenAdCallback callback) {
        if (adColonyInterstitial != null && !adColonyInterstitial.isExpired()) {
            adColonyInterstitial.show();
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void onDestroy() {
        if (adColonyInterstitial != null) {
            if (isRewarded) {
                AdColonyRewardListenerWrapper.get().removeListener(listener);
            }
            adColonyInterstitial.destroy();
            adColonyInterstitial = null;
        }
    }

    void setAdColonyInterstitial(AdColonyInterstitial adColonyInterstitial) {
        this.adColonyInterstitial = adColonyInterstitial;
    }
}

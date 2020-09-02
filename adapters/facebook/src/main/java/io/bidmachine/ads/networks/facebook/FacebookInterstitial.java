package io.bidmachine.ads.networks.facebook;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.ads.Ad;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;

import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

class FacebookInterstitial extends UnifiedFullscreenAd {

    @Nullable
    private InterstitialAd interstitialAd;
    private FacebookListener facebookListener;

    @Override
    public void load(@NonNull ContextProvider context,
                     @NonNull UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) throws Throwable {
        FacebookParams params = new FacebookParams(mediationParams);
        if (!params.isValid(callback)) {
            return;
        }
        facebookListener = new FacebookListener(callback);
        interstitialAd = new InterstitialAd(context.getContext(), params.placementId);
        interstitialAd.loadAd(interstitialAd.buildLoadAdConfig()
                                      .withAdListener(facebookListener)
                                      .withBid(params.bidPayload)
                                      .build());
    }

    @Override
    public void show(@NonNull Context context, @NonNull UnifiedFullscreenAdCallback callback) {
        if (interstitialAd != null
                && interstitialAd.isAdLoaded()
                && !interstitialAd.isAdInvalidated()) {
            interstitialAd.show();
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void onDestroy() {
        facebookListener = null;
        if (interstitialAd != null) {
            interstitialAd.destroy();
            interstitialAd = null;
        }
    }

    private static final class FacebookListener
            extends BaseFacebookListener<UnifiedFullscreenAdCallback>
            implements InterstitialAdListener {

        FacebookListener(@NonNull UnifiedFullscreenAdCallback callback) {
            super(callback);
        }

        @Override
        public void onAdLoaded(Ad ad) {
            getCallback().onAdLoaded();
        }

        @Override
        public void onInterstitialDisplayed(Ad ad) {
            //ignore
        }

        @Override
        public void onLoggingImpression(Ad ad) {
            getCallback().onAdShown();
        }

        @Override
        public void onInterstitialDismissed(Ad ad) {
            getCallback().onAdClosed();
            ad.destroy();
        }
    }
}

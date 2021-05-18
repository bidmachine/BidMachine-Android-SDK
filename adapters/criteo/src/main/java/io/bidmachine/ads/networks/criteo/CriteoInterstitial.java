package io.bidmachine.ads.networks.criteo;

import android.content.Context;

import androidx.annotation.NonNull;

import com.criteo.publisher.Bid;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitialAdListener;
import com.criteo.publisher.model.InterstitialAdUnit;

import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

public class CriteoInterstitial extends UnifiedFullscreenAd {

    private com.criteo.publisher.CriteoInterstitial criteoInterstitial;

    @Override
    public void load(@NonNull ContextProvider context,
                     @NonNull UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) throws Throwable {
        String adUnitId = mediationParams.getString(CriteoConfig.AD_UNIT_ID);
        InterstitialAdUnit interstitialAdUnit =
                (InterstitialAdUnit) CriteoAdUnitController.getAdUnit(adUnitId);
        if (interstitialAdUnit == null) {
            callback.onAdLoadFailed(BMError.requestError("AdUnit not found"));
            return;
        }
        Bid bid = CriteoBidTokenController.takeBid(requestParams.getAdRequest());
        if (bid == null) {
            callback.onAdLoadFailed(BMError.requestError("Bid not found"));
            return;
        }
        criteoInterstitial = new com.criteo.publisher.CriteoInterstitial(interstitialAdUnit);
        criteoInterstitial.setCriteoInterstitialAdListener(new Listener(callback));
        criteoInterstitial.loadAd(bid);
    }

    @Override
    public void show(@NonNull Context context,
                     @NonNull UnifiedFullscreenAdCallback callback) throws Throwable {
        if (criteoInterstitial.isAdLoaded()) {
            criteoInterstitial.show();
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void onDestroy() {
        if (criteoInterstitial != null) {
            criteoInterstitial.setCriteoInterstitialAdListener(null);
            criteoInterstitial = null;
        }
    }

    private static final class Listener implements CriteoInterstitialAdListener {

        private final UnifiedFullscreenAdCallback callback;

        Listener(@NonNull UnifiedFullscreenAdCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onAdReceived(@NonNull com.criteo.publisher.CriteoInterstitial criteoInterstitial) {
            callback.onAdLoaded();
        }

        @Override
        public void onAdFailedToReceive(@NonNull CriteoErrorCode criteoErrorCode) {
            callback.onAdLoadFailed(CriteoAdapter.mapError(criteoErrorCode));
        }

        @Override
        public void onAdClicked() {
            callback.onAdClicked();
        }

        @Override
        public void onAdOpened() {
            callback.onAdShown();
        }

        @Override
        public void onAdClosed() {
            callback.onAdClosed();
        }

        @Override
        public void onAdLeftApplication() {

        }

    }

}
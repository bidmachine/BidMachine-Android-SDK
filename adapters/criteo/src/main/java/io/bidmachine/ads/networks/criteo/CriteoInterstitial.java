package io.bidmachine.ads.networks.criteo;

import android.content.Context;
import android.support.annotation.NonNull;

import com.criteo.publisher.BidToken;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.CriteoInterstitialAdDisplayListener;
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
        BidToken bidToken = CriteoBidTokenController.takeBidToken(requestParams.getAdRequest());
        if (bidToken == null) {
            callback.onAdLoadFailed(BMError.requestError("BidToken not found"));
            return;
        }
        Listener listener = new Listener(callback);
        criteoInterstitial = new com.criteo.publisher.CriteoInterstitial(
                context.getContext(),
                interstitialAdUnit);
        criteoInterstitial.setCriteoInterstitialAdListener(listener);
        criteoInterstitial.setCriteoInterstitialAdDisplayListener(listener);
        criteoInterstitial.loadAd(bidToken);
    }

    @Override
    public void show(@NonNull Context context, @NonNull UnifiedFullscreenAdCallback callback) {
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
            criteoInterstitial.setCriteoInterstitialAdDisplayListener(null);
            criteoInterstitial = null;
        }
    }

    private static final class Listener implements CriteoInterstitialAdListener, CriteoInterstitialAdDisplayListener {

        private final UnifiedFullscreenAdCallback callback;

        Listener(@NonNull UnifiedFullscreenAdCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onAdReceived() {

        }

        @Override
        public void onAdFailedToReceive(CriteoErrorCode criteoErrorCode) {
            callback.onAdLoadFailed(CriteoAdapter.mapError(criteoErrorCode));
        }

        @Override
        public void onAdReadyToDisplay() {
            callback.onAdLoaded();
        }

        @Override
        public void onAdLeftApplication() {

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
        public void onAdFailedToDisplay(CriteoErrorCode criteoErrorCode) {
            callback.onAdShowFailed(CriteoAdapter.mapError(criteoErrorCode));
        }
    }

}
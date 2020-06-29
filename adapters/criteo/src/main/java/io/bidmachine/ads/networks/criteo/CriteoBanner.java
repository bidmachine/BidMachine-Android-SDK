package io.bidmachine.ads.networks.criteo;

import android.support.annotation.NonNull;
import android.view.View;

import com.criteo.publisher.BidToken;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.model.BannerAdUnit;

import io.bidmachine.ContextProvider;
import io.bidmachine.core.Utils;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedBannerAdCallback;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

public class CriteoBanner extends UnifiedBannerAd {

    private CriteoBannerView criteoBannerView;

    @Override
    public void load(@NonNull final ContextProvider context,
                     @NonNull final UnifiedBannerAdCallback callback,
                     @NonNull UnifiedBannerAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) throws Throwable {
        String adUnitId = mediationParams.getString(CriteoConfig.AD_UNIT_ID);
        final BannerAdUnit bannerAdUnit = (BannerAdUnit) CriteoAdUnitController.getAdUnit(adUnitId);
        if (bannerAdUnit == null) {
            callback.onAdLoadFailed(BMError.requestError("AdUnit not found"));
            return;
        }
        final BidToken bidToken = CriteoBidTokenController.takeBidToken(requestParams.getAdRequest());
        if (bidToken == null) {
            callback.onAdLoadFailed(BMError.requestError("BidToken not found"));
            return;
        }
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                criteoBannerView = new CriteoBannerView(context.getContext(), bannerAdUnit);
                criteoBannerView.setCriteoBannerAdListener(new Listener(callback));
                criteoBannerView.loadAd(bidToken);
            }
        });
    }

    @Override
    public void onDestroy() {
        if (criteoBannerView != null) {
            criteoBannerView.setCriteoBannerAdListener(null);
            criteoBannerView = null;
        }
    }

    private static final class Listener implements CriteoBannerAdListener {

        private final UnifiedBannerAdCallback callback;

        Listener(@NonNull UnifiedBannerAdCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onAdReceived(View view) {
            callback.onAdLoaded(view);
        }

        @Override
        public void onAdFailedToReceive(CriteoErrorCode criteoErrorCode) {
            callback.onAdLoadFailed(CriteoAdapter.mapError(criteoErrorCode));
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

        }

        @Override
        public void onAdClosed() {

        }

    }

}
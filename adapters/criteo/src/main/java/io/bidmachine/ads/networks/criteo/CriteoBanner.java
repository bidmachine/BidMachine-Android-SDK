package io.bidmachine.ads.networks.criteo;

import androidx.annotation.NonNull;

import com.criteo.publisher.Bid;
import com.criteo.publisher.CriteoBannerAdListener;
import com.criteo.publisher.CriteoBannerView;
import com.criteo.publisher.CriteoErrorCode;
import com.criteo.publisher.model.BannerAdUnit;

import io.bidmachine.ContextProvider;
import io.bidmachine.core.AdapterLogger;
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
        final BannerAdUnit bannerAdUnit = (BannerAdUnit) CriteoAdUnitStorage.getAdUnit(adUnitId);
        if (bannerAdUnit == null) {
            callback.onAdLoadFailed(BMError.requestError("AdUnit not found"));
            return;
        }
        final Bid bid = CriteoBidTokenStorage.takeBid(requestParams.getAdRequest());
        if (bid == null) {
            callback.onAdLoadFailed(BMError.requestError("Bid not found"));
            return;
        }
        Utils.onUiThread(() -> {
            try {
                criteoBannerView = new CriteoBannerView(context.getContext(), bannerAdUnit);
                criteoBannerView.setCriteoBannerAdListener(new Listener(callback));
                criteoBannerView.loadAd(bid);
            } catch (Throwable t) {
                AdapterLogger.log(t);
                callback.onAdLoadFailed(BMError.Internal);
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
        public void onAdReceived(@NonNull CriteoBannerView criteoBannerView) {
            callback.onAdLoaded(criteoBannerView);
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
        public void onAdLeftApplication() {

        }

    }

}
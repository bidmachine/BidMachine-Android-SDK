package io.bidmachine.ads.networks.my_target;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.my.target.ads.MyTargetView;

import io.bidmachine.ContextProvider;
import io.bidmachine.banner.BannerSize;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedBannerAdCallback;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

class MyTargetBanner extends UnifiedBannerAd {

    @Nullable
    private MyTargetView adView;

    @Override
    public void load(@NonNull ContextProvider contextProvider,
                     @NonNull UnifiedBannerAdCallback callback,
                     @NonNull UnifiedBannerAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) {
        MyTargetParams params = new MyTargetParams(mediationParams);
        if (!params.isValid(callback)) {
            return;
        }
        BannerSize size = requestParams.getBannerSize();
        int adSize;
        switch (size) {
            case Size_728x90: {
                adSize = MyTargetView.AdSize.BANNER_728x90;
                break;
            }
            case Size_300x250: {
                adSize = MyTargetView.AdSize.BANNER_300x250;
                break;
            }
            default: {
                adSize = MyTargetView.AdSize.BANNER_320x50;
                break;
            }
        }
        adView = new MyTargetView(contextProvider.getContext());
        assert params.slotId != null; // it's shouldn't be null since we already check it in {@link MyTargetParams}
        adView.init(params.slotId, adSize, false);
        adView.setListener(new MyTargetListener(callback));
        assert adView.getCustomParams() != null; // it's shouldn't be null at this point
        MyTargetAdapter.updateTargeting(requestParams, adView.getCustomParams());
        assert params.bidId != null; // it's shouldn't be null since we already check it in {@link MyTargetParams}
        adView.loadFromBid(params.bidId);
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
    }

    private final class MyTargetListener implements MyTargetView.MyTargetViewListener {

        private UnifiedBannerAdCallback callback;

        MyTargetListener(UnifiedBannerAdCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onLoad(@NonNull MyTargetView myTargetView) {
            callback.onAdLoaded(myTargetView);
        }

        @Override
        public void onNoAd(@NonNull String s, @NonNull MyTargetView myTargetView) {
            callback.onAdLoadFailed(BMError.noFillError(null));
        }

        @Override
        public void onShow(@NonNull MyTargetView myTargetView) {
            //ignore
        }

        @Override
        public void onClick(@NonNull MyTargetView myTargetView) {
            callback.onAdClicked();
        }
    }

}

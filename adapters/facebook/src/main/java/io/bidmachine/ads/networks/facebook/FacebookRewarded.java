package io.bidmachine.ads.networks.facebook;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.ads.Ad;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;

import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

class FacebookRewarded extends UnifiedFullscreenAd {

    @Nullable
    private RewardedVideoAd rewardedVideoAd;
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
        rewardedVideoAd = new RewardedVideoAd(context.getContext(), params.placementId);
        rewardedVideoAd.loadAd(rewardedVideoAd.buildLoadAdConfig()
                                       .withAdListener(facebookListener)
                                       .withBid(params.bidPayload)
                                       .withFailOnCacheFailureEnabled(false)
                                       .build());
    }

    @Override
    public void show(@NonNull Context context, @NonNull UnifiedFullscreenAdCallback callback) {
        if (rewardedVideoAd != null
                && rewardedVideoAd.isAdLoaded()
                && !rewardedVideoAd.isAdInvalidated()) {
            rewardedVideoAd.show();
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void onDestroy() {
        facebookListener = null;
        if (rewardedVideoAd != null) {
            rewardedVideoAd.destroy();
            rewardedVideoAd = null;
        }
    }

    private static final class FacebookListener
            extends BaseFacebookListener<UnifiedFullscreenAdCallback>
            implements RewardedVideoAdListener {

        FacebookListener(@NonNull UnifiedFullscreenAdCallback callback) {
            super(callback);
        }

        @Override
        public void onAdLoaded(Ad ad) {
            getCallback().onAdLoaded();
        }

        @Override
        public void onLoggingImpression(Ad ad) {
            getCallback().onAdShown();
        }

        @Override
        public void onRewardedVideoCompleted() {
            getCallback().onAdFinished();
        }

        @Override
        public void onRewardedVideoClosed() {
            getCallback().onAdClosed();
        }
    }
}

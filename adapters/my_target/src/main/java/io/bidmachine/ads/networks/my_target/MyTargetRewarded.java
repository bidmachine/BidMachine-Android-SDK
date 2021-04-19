package io.bidmachine.ads.networks.my_target;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.my.target.ads.Reward;
import com.my.target.ads.RewardedAd;

import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

public class MyTargetRewarded extends UnifiedFullscreenAd {

    @Nullable
    private RewardedAd rewardedAd;

    @Override
    public void load(@NonNull ContextProvider contextProvider,
                     @NonNull UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) throws Throwable {
        MyTargetParams params = new MyTargetParams(mediationParams);
        if (!params.isValid(callback)) {
            return;
        }
        assert params.slotId != null;
        assert params.bidId != null;

        rewardedAd = new RewardedAd(params.slotId, contextProvider.getContext());
        rewardedAd.setListener(new Listener(callback));
        MyTargetAdapter.updateTargeting(requestParams, rewardedAd.getCustomParams());
        rewardedAd.loadFromBid(params.bidId);
    }

    @Override
    public void show(@NonNull Context context, @NonNull UnifiedFullscreenAdCallback callback) {
        if (rewardedAd != null) {
            rewardedAd.show(context);
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void onDestroy() {
        if (rewardedAd != null) {
            rewardedAd.destroy();
            rewardedAd = null;
        }
    }


    private static final class Listener implements RewardedAd.RewardedAdListener {

        @NonNull
        private final UnifiedFullscreenAdCallback callback;

        Listener(@NonNull UnifiedFullscreenAdCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onLoad(@NonNull RewardedAd rewardedAd) {
            callback.onAdLoaded();
        }

        @Override
        public void onNoAd(@NonNull String s, @NonNull RewardedAd rewardedAd) {
            callback.onAdLoadFailed(BMError.noFillError(null));
        }

        @Override
        public void onClick(@NonNull RewardedAd rewardedAd) {
            callback.onAdClicked();
        }

        @Override
        public void onDisplay(@NonNull RewardedAd rewardedAd) {
            callback.onAdShown();
        }

        @Override
        public void onDismiss(@NonNull RewardedAd rewardedAd) {
            callback.onAdClosed();
        }

        @Override
        public void onReward(@NonNull Reward reward, @NonNull RewardedAd rewardedAd) {
            callback.onAdFinished();
        }

    }

}
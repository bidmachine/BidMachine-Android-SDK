package io.bidmachine.ads.networks.pangle;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;

import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

class PangleRewardedAd extends UnifiedFullscreenAd {

    @Nullable
    private LoadListener loadListener;
    @Nullable
    private TTAdNative ttAdNative;
    @Nullable
    private TTRewardVideoAd ttRewardVideoAd;

    @Override
    public void load(@NonNull ContextProvider context,
                     @NonNull UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) throws Throwable {
        PangleParams params = new PangleParams(mediationParams);
        if (!params.isValid(callback)) {
            return;
        }
        assert params.slotId != null;
        assert params.bidPayload != null;

        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(params.slotId)
                .withBid(params.bidPayload)
                .build();

        loadListener = new LoadListener(this, callback);
        ttAdNative = TTAdSdk.getAdManager().createAdNative(context.getApplicationContext());
        ttAdNative.loadRewardVideoAd(adSlot, loadListener);
    }

    void setTtRewardVideoAd(@Nullable TTRewardVideoAd ttRewardVideoAd) {
        this.ttRewardVideoAd = ttRewardVideoAd;
    }

    @Override
    public void show(@NonNull ContextProvider contextProvider,
                     @NonNull UnifiedFullscreenAdCallback callback) throws Throwable {
        Activity activity = contextProvider.getActivity();
        if (activity == null) {
            callback.onAdShowFailed(BMError.internal("Activity is null"));
            return;
        }
        if (ttRewardVideoAd == null) {
            callback.onAdShowFailed(BMError.internal("Rewarded object is null"));
            return;
        }

        ttRewardVideoAd.setRewardAdInteractionListener(new ShowListener(callback));
        ttRewardVideoAd.showRewardVideoAd(activity);
    }

    @Override
    public void onDestroy() {
        ttAdNative = null;
        if (loadListener != null) {
            loadListener.destroy();
            loadListener = null;
        }
        if (ttRewardVideoAd != null) {
            ttRewardVideoAd.setRewardAdInteractionListener(null);
            ttRewardVideoAd = null;
        }
    }


    private static final class LoadListener implements TTAdNative.RewardVideoAdListener {

        @NonNull
        private final PangleRewardedAd pangleRewardedAd;
        @NonNull
        private final UnifiedFullscreenAdCallback callback;

        private boolean isDestroyed;

        public LoadListener(@NonNull PangleRewardedAd pangleRewardedAd,
                            @NonNull UnifiedFullscreenAdCallback callback) {
            this.pangleRewardedAd = pangleRewardedAd;
            this.callback = callback;
        }

        @Override
        public void onRewardVideoAdLoad(TTRewardVideoAd ttRewardVideoAd) {
            if (isDestroyed) {
                return;
            }

            pangleRewardedAd.setTtRewardVideoAd(ttRewardVideoAd);

            callback.onAdLoaded();
        }

        @Override
        public void onRewardVideoCached() {

        }

        @Override
        public void onError(int errorCode, String message) {
            if (isDestroyed) {
                return;
            }

            callback.onAdLoadFailed(BMError.noFill());
        }

        public void destroy() {
            isDestroyed = true;
        }

    }

    private static final class ShowListener implements TTRewardVideoAd.RewardAdInteractionListener {

        @NonNull
        private final UnifiedFullscreenAdCallback callback;

        public ShowListener(@NonNull UnifiedFullscreenAdCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onAdShow() {
            callback.onAdShown();
        }

        @Override
        public void onAdVideoBarClick() {
            callback.onAdClicked();
        }

        @Override
        public void onAdClose() {
            callback.onAdClosed();
        }

        @Override
        public void onSkippedVideo() {

        }

        @Override
        public void onVideoComplete() {
            callback.onAdFinished();
        }

        @Override
        public void onVideoError() {

        }

        @Override
        public void onRewardVerify(boolean rewardVerify,
                                   int rewardAmount,
                                   String rewardName,
                                   int errorCode,
                                   String errorMsg) {

        }

    }

}
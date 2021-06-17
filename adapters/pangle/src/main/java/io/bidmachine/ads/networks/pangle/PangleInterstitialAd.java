package io.bidmachine.ads.networks.pangle;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;

import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

public class PangleInterstitialAd extends UnifiedFullscreenAd {

    @Nullable
    private ContextProvider contextProvider;
    @Nullable
    private LoadListener loadListener;
    @Nullable
    private TTAdNative ttAdNative;
    @Nullable
    private TTFullScreenVideoAd ttFullScreenVideoAd;

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

        contextProvider = context;

        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(params.slotId)
                .withBid(params.bidPayload)
                .build();

        loadListener = new LoadListener(this, callback);
        ttAdNative = TTAdSdk.getAdManager().createAdNative(context.getApplicationContext());
        ttAdNative.loadFullScreenVideoAd(adSlot, loadListener);
    }

    void setTtFullScreenVideoAd(@Nullable TTFullScreenVideoAd ttFullScreenVideoAd) {
        this.ttFullScreenVideoAd = ttFullScreenVideoAd;
    }

    @Override
    public void show(@NonNull Context context,
                     @NonNull UnifiedFullscreenAdCallback callback) throws Throwable {
        Activity activity = contextProvider != null ? contextProvider.getActivity() : null;
        if (activity == null) {
            callback.onAdShowFailed(BMError.Internal);
            return;
        }
        if (ttFullScreenVideoAd == null) {
            callback.onAdShowFailed(BMError.NotLoaded);
            return;
        }

        ttFullScreenVideoAd.setFullScreenVideoAdInteractionListener(new ShowListener(callback));
        ttFullScreenVideoAd.showFullScreenVideoAd(activity);
    }

    @Override
    public void onDestroy() {
        contextProvider = null;
        ttAdNative = null;
        if (loadListener != null) {
            loadListener.destroy();
            loadListener = null;
        }
        if (ttFullScreenVideoAd != null) {
            ttFullScreenVideoAd.setFullScreenVideoAdInteractionListener(null);
            ttFullScreenVideoAd = null;
        }
    }


    private static final class LoadListener implements TTAdNative.FullScreenVideoAdListener {

        @NonNull
        private final PangleInterstitialAd pangleInterstitialAd;
        @NonNull
        private final UnifiedFullscreenAdCallback callback;

        private boolean isDestroyed;

        public LoadListener(@NonNull PangleInterstitialAd pangleInterstitialAd,
                            @NonNull UnifiedFullscreenAdCallback callback) {
            this.pangleInterstitialAd = pangleInterstitialAd;
            this.callback = callback;
        }

        @Override
        public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ttFullScreenVideoAd) {
            if (isDestroyed) {
                return;
            }

            pangleInterstitialAd.setTtFullScreenVideoAd(ttFullScreenVideoAd);

            callback.onAdLoaded();
        }

        @Override
        public void onFullScreenVideoCached() {

        }

        @Override
        public void onError(int errorCode, String message) {
            if (isDestroyed) {
                return;
            }

            callback.onAdLoadFailed(BMError.noFillError(null));
        }

        public void destroy() {
            isDestroyed = true;
        }

    }

    private static final class ShowListener implements TTFullScreenVideoAd.FullScreenVideoAdInteractionListener {

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

    }

}
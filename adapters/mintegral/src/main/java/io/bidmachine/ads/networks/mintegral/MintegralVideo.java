package io.bidmachine.ads.networks.mintegral;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mintegral.msdk.interstitialvideo.out.InterstitialVideoListener;
import com.mintegral.msdk.interstitialvideo.out.MTGBidInterstitialVideoHandler;

import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

class MintegralVideo extends UnifiedFullscreenAd {

    @Nullable
    private MTGBidInterstitialVideoHandler handler;

    @Override
    public void show(@NonNull Context context, @NonNull UnifiedFullscreenAdCallback callback) {
        if (handler != null && handler.isBidReady()) {
            handler.showFromBid();
        } else {
            callback.onAdShowFailed(BMError.NotLoaded);
        }
    }

    @Override
    public void load(@NonNull ContextProvider context,
                     @NonNull final UnifiedFullscreenAdCallback callback,
                     @NonNull UnifiedFullscreenAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) throws Throwable {
        final String unitId = mediationParams.getString(MintegralConfig.KEY_UNIT_ID);
        final String bidToken = mediationParams.getString(MintegralConfig.KEY_BID_TOKEN);
        MintegralAdapter.handler.post(new Runnable() {
            @Override
            public void run() {
                handler = new MTGBidInterstitialVideoHandler(unitId);
                handler.setInterstitialVideoListener(new Listener(callback));
                handler.loadFromBid(bidToken);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler = null;
        }
    }

    private class Listener implements InterstitialVideoListener {

        @NonNull
        private UnifiedFullscreenAdCallback callback;

        Listener(@NonNull UnifiedFullscreenAdCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onLoadSuccess(String s) {
            //ignore
        }

        @Override
        public void onVideoLoadSuccess(String s) {
            callback.onAdLoaded();
        }

        @Override
        public void onVideoLoadFail(String s) {
            callback.onAdLoadFailed(MintegralAdapter.mapError(s));
        }

        @Override
        public void onAdShow() {
            callback.onAdShown();
        }

        @Override
        public void onAdClose(boolean isCompleteView) {
            if (isCompleteView) {
                callback.onAdFinished();
            }
            callback.onAdClosed();
        }

        @Override
        public void onShowFail(String s) {
            callback.onAdShowFailed(BMError.Internal);
        }

        @Override
        public void onVideoAdClicked(String s) {
            callback.onAdClicked();
        }

        @Override
        public void onVideoComplete(String s) {
            // Ignore since we should notify finish via check "isCompleteView" in
            // {@link InterstitialVideoListener#onAdClose}
        }

        @Override
        public void onEndcardShow(String s) {
            //ignore
        }
    }

}
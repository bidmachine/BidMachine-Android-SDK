package io.bidmachine.ads.networks.mintegral;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mintegral.msdk.out.MTGBidRewardVideoHandler;
import com.mintegral.msdk.out.RewardVideoListener;

import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedFullscreenAd;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

public class MintegralRewarded extends UnifiedFullscreenAd {

    @Nullable
    private MTGBidRewardVideoHandler handler;
    @Nullable
    private String rewardId;

    @Override
    public void show(@NonNull Context context, @NonNull UnifiedFullscreenAdCallback callback) {
        if (handler != null && handler.isBidReady()) {
            handler.showFromBid(rewardId);
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
        rewardId = mediationParams.getString(MintegralConfig.KEY_REWARD_ID);
        MintegralAdapter.handler.post(new Runnable() {
            @Override
            public void run() {
                handler = new MTGBidRewardVideoHandler(unitId);
                handler.setRewardVideoListener(new Listener(callback));
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

    private class Listener implements RewardVideoListener {

        @NonNull
        private UnifiedFullscreenAdCallback callback;

        Listener(@NonNull UnifiedFullscreenAdCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onVideoLoadSuccess(String s) {
            callback.onAdLoaded();
        }

        @Override
        public void onLoadSuccess(String s) {
            //ignore
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
        public void onAdClose(boolean isCompleteView, String RewardName, float RewardAmount) {
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
            // {@link RewardVideoListener#onAdClose}
        }

        @Override
        public void onEndcardShow(String s) {
            //ignore
        }
    }

}

package io.bidmachine.ads.networks.tapjoy;

import androidx.annotation.NonNull;

import com.tapjoy.TJActionRequest;
import com.tapjoy.TJError;
import com.tapjoy.TJPlacement;
import com.tapjoy.TJPlacementListener;
import com.tapjoy.TJPlacementVideoListener;

import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.utils.BMError;

class TapjoyFullscreenAdListener implements TJPlacementListener, TJPlacementVideoListener {

    @NonNull
    private final UnifiedFullscreenAdCallback callback;

    TapjoyFullscreenAdListener(@NonNull UnifiedFullscreenAdCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onRequestSuccess(TJPlacement tjPlacement) {
        if (!tjPlacement.isContentAvailable()) {
            callback.onAdLoadFailed(BMError.NoContent);
        }
    }

    @Override
    public void onRequestFailure(TJPlacement tjPlacement, TJError tjError) {
        callback.onAdLoadFailed(BMError.NoContent);
    }

    @Override
    public void onContentReady(TJPlacement tjPlacement) {
        callback.onAdLoaded();
    }

    @Override
    public void onContentShow(TJPlacement tjPlacement) {
        callback.onAdShown();
    }

    @Override
    public void onContentDismiss(TJPlacement tjPlacement) {
        callback.onAdClosed();
    }

    @Override
    public void onPurchaseRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s) {
        //ignore
    }

    @Override
    public void onRewardRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s, int i) {
        //ignore
    }

    @Override
    public void onClick(TJPlacement tjPlacement) {
        callback.onAdClicked();
    }

    @Override
    public void onVideoStart(TJPlacement tjPlacement) {
        //ignore
    }

    @Override
    public void onVideoError(TJPlacement tjPlacement, String s) {
        //ignore
    }

    @Override
    public void onVideoComplete(TJPlacement tjPlacement) {
        callback.onAdFinished();
    }
}

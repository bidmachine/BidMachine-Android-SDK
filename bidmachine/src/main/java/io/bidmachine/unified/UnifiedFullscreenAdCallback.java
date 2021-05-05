package io.bidmachine.unified;

public interface UnifiedFullscreenAdCallback extends UnifiedAdCallback {

    void onAdLoaded();

    void onAdShown();

    void onAdFinished();

    void onAdClosed();

}
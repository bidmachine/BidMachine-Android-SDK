package io.bidmachine.app_event;

public interface BMAdManagerAppEventListener {

    void onAdLoaded();

    void onAdFailToLoad();

    void onAdShown();

    void onAdClicked();

    void onAdClosed();

    void onAdExpired();

}
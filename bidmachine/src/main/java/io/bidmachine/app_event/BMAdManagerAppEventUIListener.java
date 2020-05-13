package io.bidmachine.app_event;

import android.support.annotation.NonNull;

import io.bidmachine.core.Utils;

public class BMAdManagerAppEventUIListener implements BMAdManagerAppEventListener {

    private final BMAdManagerAppEventListener bmAdManagerAppEventListener;

    public BMAdManagerAppEventUIListener(@NonNull BMAdManagerAppEventListener bmAdManagerAppEventListener) {
        this.bmAdManagerAppEventListener = bmAdManagerAppEventListener;
    }

    @Override
    public void onAdLoaded() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                bmAdManagerAppEventListener.onAdLoaded();
            }
        });
    }

    @Override
    public void onAdFailToLoad() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                bmAdManagerAppEventListener.onAdFailToLoad();
            }
        });
    }

    @Override
    public void onAdShown() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                bmAdManagerAppEventListener.onAdShown();
            }
        });
    }

    @Override
    public void onAdClicked() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                bmAdManagerAppEventListener.onAdClicked();
            }
        });
    }

    @Override
    public void onAdClosed() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                bmAdManagerAppEventListener.onAdClosed();
            }
        });
    }

    @Override
    public void onAdExpired() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                bmAdManagerAppEventListener.onAdExpired();
            }
        });
    }

}
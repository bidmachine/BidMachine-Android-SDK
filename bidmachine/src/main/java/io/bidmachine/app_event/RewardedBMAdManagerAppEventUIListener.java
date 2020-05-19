package io.bidmachine.app_event;

import android.support.annotation.NonNull;

import io.bidmachine.core.Utils;

class RewardedBMAdManagerAppEventUIListener extends BMAdManagerAppEventUIListener implements RewardedBMAdManagerAppEventListener {

    RewardedBMAdManagerAppEventUIListener(@NonNull BMAdManagerAppEventListener bmAdManagerAppEventListener) {
        super(bmAdManagerAppEventListener);
    }

    @Override
    public void onAdRewarded() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                if (bmAdManagerAppEventListener instanceof RewardedBMAdManagerAppEventListener) {
                    ((RewardedBMAdManagerAppEventListener) bmAdManagerAppEventListener).onAdRewarded();
                }
            }
        });
    }

}
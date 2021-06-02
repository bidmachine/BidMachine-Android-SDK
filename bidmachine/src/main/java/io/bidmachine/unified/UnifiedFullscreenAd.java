package io.bidmachine.unified;

import androidx.annotation.NonNull;

import io.bidmachine.ContextProvider;

public abstract class UnifiedFullscreenAd extends UnifiedAd<UnifiedFullscreenAdCallback, UnifiedFullscreenAdRequestParams> {

    public abstract void show(@NonNull ContextProvider contextProvider,
                              @NonNull UnifiedFullscreenAdCallback callback) throws Throwable;

    public void onFinished() {

    }

    public void onClosed(boolean finished) {

    }

}
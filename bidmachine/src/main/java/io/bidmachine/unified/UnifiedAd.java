package io.bidmachine.unified;

import androidx.annotation.NonNull;

import io.bidmachine.ContextProvider;

public abstract class UnifiedAd<
        UnifiedAdCallbackType extends UnifiedAdCallback,
        UnifiedAdRequestParamsType extends UnifiedAdRequestParams> {

    public abstract void load(@NonNull ContextProvider context,
                              @NonNull UnifiedAdCallbackType callback,
                              @NonNull UnifiedAdRequestParamsType requestParams,
                              @NonNull UnifiedMediationParams mediationParams) throws Throwable;

    public void onShown() {

    }

    public void onShowFailed() {

    }

    public void onImpression() {

    }

    public void onClicked() {

    }

    public void onExpired() {

    }

    public void onDestroy() {

    }

}
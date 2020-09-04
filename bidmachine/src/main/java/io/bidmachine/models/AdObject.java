package io.bidmachine.models;

import androidx.annotation.NonNull;

import io.bidmachine.AdProcessCallback;
import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedAdCallback;
import io.bidmachine.unified.UnifiedAdRequestParams;

public interface AdObject<
        AdObjectParamsType extends AdObjectParams,
        UnifiedAdRequestParamsType extends UnifiedAdRequestParams,
        UnifiedAdCallbackType extends UnifiedAdCallback> {

    AdObjectParamsType getParams();

    void load(@NonNull ContextProvider contextProvider,
              @NonNull UnifiedAdRequestParamsType adRequestParams) throws Throwable;

    @NonNull
    UnifiedAdCallbackType createUnifiedCallback(@NonNull AdProcessCallback processCallback);

    void onShown();

    void onShowFailed();

    void onImpression();

    void onClicked();

    void onFinished();

    void onClosed(boolean finished);

    void onExpired();

    void onDestroy();
}
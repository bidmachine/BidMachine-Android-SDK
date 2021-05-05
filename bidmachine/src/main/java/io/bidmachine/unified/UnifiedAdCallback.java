package io.bidmachine.unified;

import io.bidmachine.utils.BMError;

public interface UnifiedAdCallback {

    void onAdLoadFailed(BMError error);

    void onAdClicked();

    void onAdShowFailed(BMError error);

    void onAdExpired();

    void log(String message);

}
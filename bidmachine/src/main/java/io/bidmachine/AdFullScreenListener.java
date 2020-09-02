package io.bidmachine;

import androidx.annotation.NonNull;

import io.bidmachine.utils.BMError;

public interface AdFullScreenListener<AdType extends IAd> {

    /**
     * Called when ad show failed
     *
     * @param ad    - Ad type {@link IAd}
     * @param error - Ad load error {@link BMError}
     */
    void onAdShowFailed(@NonNull AdType ad, @NonNull BMError error);

    /**
     * Called when ad was closed (e.g - user click close button)
     *
     * @param ad       - Ad type {@link IAd}
     * @param finished - Value for indicated, if ads was finished (e.g - video playing finished)
     */
    void onAdClosed(@NonNull AdType ad, boolean finished);

}

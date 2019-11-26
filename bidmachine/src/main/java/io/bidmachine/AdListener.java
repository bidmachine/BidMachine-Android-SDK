package io.bidmachine;

import android.support.annotation.NonNull;

import io.bidmachine.utils.BMError;

public interface AdListener<AdType extends IAd> {

    /**
     * Called when Ad was loaded and ready to be displayed
     *
     * @param ad - Ad type {@link IAd}
     */
    void onAdLoaded(@NonNull AdType ad);

    /**
     * Called when Ad failed to load
     *
     * @param ad    - Ad type {@link IAd}
     * @param error - Ad load error {@link BMError}
     */
    void onAdLoadFailed(@NonNull AdType ad, @NonNull BMError error);

    /**
     * Called when Ad has been shown
     *
     * @param ad - Ad type {@link IAd}
     */
    void onAdShown(@NonNull AdType ad);

    /**
     * Called when Ad Impression has been tracked
     *
     * @param ad - Ad type {@link IAd}
     */
    void onAdImpression(@NonNull AdType ad);

    /**
     * Called when Ad has been clicked
     *
     * @param ad - Ad type {@link IAd}
     */
    void onAdClicked(@NonNull AdType ad);

    /**
     * Called when Ad expired
     *
     * @param ad - Ad type {@link IAd}
     */
    void onAdExpired(@NonNull AdType ad);

}

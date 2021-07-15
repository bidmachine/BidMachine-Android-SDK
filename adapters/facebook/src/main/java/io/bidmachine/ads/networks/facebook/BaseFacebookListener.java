package io.bidmachine.ads.networks.facebook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;

import io.bidmachine.unified.UnifiedAdCallback;
import io.bidmachine.utils.BMError;

abstract class BaseFacebookListener<UnifiedAdCallbackType extends UnifiedAdCallback> implements AdListener {

    @NonNull
    private final UnifiedAdCallbackType callback;

    BaseFacebookListener(@NonNull UnifiedAdCallbackType callback) {
        this.callback = callback;
    }

    @NonNull
    UnifiedAdCallbackType getCallback() {
        return callback;
    }

    @Override
    public void onError(Ad ad, AdError adError) {
        callback.onAdLoadFailed(mapError(adError));
        ad.destroy();
    }

    @Override
    public void onAdClicked(Ad ad) {
        callback.onAdClicked();
    }

    /**
     * @param error Facebook loading error code {@link com.facebook.ads.AdError}.
     * @return Appodeal loading error {@link BMError} or null.
     */
    @Nullable
    private static BMError mapError(@Nullable AdError error) {
        if (error == null) {
            return null;
        }
        switch (error.getErrorCode()) {
            case AdError.NETWORK_ERROR_CODE:
                return BMError.NoConnection;
            case AdError.NO_FILL_ERROR_CODE:
            case AdError.SERVER_ERROR_CODE:
            case AdError.INTERNAL_ERROR_CODE:
            case AdError.CACHE_ERROR_CODE:
            case AdError.MEDIATION_ERROR_CODE:
            case AdError.LOAD_TOO_FREQUENTLY_ERROR_CODE:
                return BMError.noFill();
            case AdError.INTERSTITIAL_AD_TIMEOUT:
                return BMError.TimeoutError;
            default:
                return BMError.internal("Unknown error");
        }
    }

}
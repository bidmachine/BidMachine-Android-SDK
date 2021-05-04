package io.bidmachine.unified;

import android.view.View;

import androidx.annotation.Nullable;

public interface UnifiedBannerAdCallback extends UnifiedAdCallback {

    void onAdLoaded(@Nullable View adView);

}
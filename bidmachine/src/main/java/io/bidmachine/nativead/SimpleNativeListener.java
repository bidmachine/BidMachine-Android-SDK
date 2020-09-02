package io.bidmachine.nativead;

import androidx.annotation.NonNull;

import io.bidmachine.utils.BMError;

public class SimpleNativeListener implements NativeListener {

    @Override
    public void onAdLoaded(@NonNull NativeAd ad) {

    }

    @Override
    public void onAdLoadFailed(@NonNull NativeAd ad, @NonNull BMError error) {

    }

    @Override
    public void onAdShown(@NonNull NativeAd ad) {

    }

    @Override
    public void onAdImpression(@NonNull NativeAd ad) {

    }

    @Override
    public void onAdClicked(@NonNull NativeAd ad) {

    }

    @Override
    public void onAdExpired(@NonNull NativeAd ad) {

    }
}
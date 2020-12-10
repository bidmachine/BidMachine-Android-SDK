package io.bidmachine;

import androidx.annotation.NonNull;

import io.bidmachine.interstitial.InterstitialAd;
import io.bidmachine.interstitial.InterstitialListener;
import io.bidmachine.utils.BMError;

public class SimpleInterstitialListener implements InterstitialListener {

    @Override
    public void onAdLoaded(@NonNull InterstitialAd ad) {

    }

    @Override
    public void onAdLoadFailed(@NonNull InterstitialAd ad, @NonNull BMError error) {

    }

    @Override
    public void onAdShown(@NonNull InterstitialAd ad) {

    }

    @Override
    public void onAdShowFailed(@NonNull InterstitialAd ad, @NonNull BMError error) {

    }

    @Override
    public void onAdImpression(@NonNull InterstitialAd ad) {

    }

    @Override
    public void onAdClicked(@NonNull InterstitialAd ad) {

    }

    @Override
    public void onAdClosed(@NonNull InterstitialAd ad, boolean finished) {

    }

    @Override
    public void onAdExpired(@NonNull InterstitialAd ad) {

    }

}

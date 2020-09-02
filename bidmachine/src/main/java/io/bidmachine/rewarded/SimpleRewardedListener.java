package io.bidmachine.rewarded;

import androidx.annotation.NonNull;

import io.bidmachine.utils.BMError;

public class SimpleRewardedListener implements RewardedListener {

    @Override
    public void onAdLoaded(@NonNull RewardedAd ad) {

    }

    @Override
    public void onAdLoadFailed(@NonNull RewardedAd ad, @NonNull BMError error) {

    }

    @Override
    public void onAdShown(@NonNull RewardedAd ad) {

    }

    @Override
    public void onAdShowFailed(@NonNull RewardedAd ad, @NonNull BMError error) {

    }

    @Override
    public void onAdImpression(@NonNull RewardedAd ad) {

    }

    @Override
    public void onAdClicked(@NonNull RewardedAd ad) {

    }

    @Override
    public void onAdExpired(@NonNull RewardedAd ad) {

    }

    @Override
    public void onAdRewarded(@NonNull RewardedAd ad) {

    }

    @Override
    public void onAdClosed(@NonNull RewardedAd ad, boolean finished) {

    }

}
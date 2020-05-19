package io.bidmachine.app_event;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.rewarded.OnAdMetadataChangedListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import io.bidmachine.AdRequest;
import io.bidmachine.BidMachineFetcher;
import io.bidmachine.BidMachineHelper;
import io.bidmachine.core.Utils;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.rewarded.RewardedListener;
import io.bidmachine.rewarded.RewardedRequest;
import io.bidmachine.utils.BMError;

public class RewardedBMAdManagerAppEvent extends BMAdManagerAppEvent {

    private static final String APP_EVENT_KEY = "bidmachine-rewarded";

    private RewardedRequest rewardedRequest;
    private RewardedAd rewardedAd;
    private io.bidmachine.rewarded.RewardedAd bmRewardedAd;

    public RewardedBMAdManagerAppEvent(String adUnitId) {
        super(adUnitId);
    }

    @Override
    public void load(@NonNull final Context context) {
        destroy();

        rewardedRequest = new RewardedRequest.Builder()
                .setListener(new RewardedRequest.AdRequestListener() {
                    @Override
                    public void onRequestSuccess(@NonNull final RewardedRequest rewardedRequest,
                                                 @NonNull AuctionResult auctionResult) {
                        Utils.onUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadPublisherAd(context, rewardedRequest);
                            }
                        });
                    }

                    @Override
                    public void onRequestFailed(@NonNull RewardedRequest request,
                                                @NonNull BMError error) {
                        if (listener != null) {
                            listener.onAdFailToLoad();
                        }
                    }

                    @Override
                    public void onRequestExpired(@NonNull RewardedRequest request) {
                        if (listener != null) {
                            listener.onAdExpired();
                        }
                    }
                })
                .build();
        rewardedRequest.request(context);
    }

    private void loadPublisherAd(@NonNull final Context context,
                                 @NonNull AdRequest adRequest) {
        AuctionResult auctionResult = adRequest.getAuctionResult();
        if (auctionResult == null) {
            if (listener != null) {
                listener.onAdFailToLoad();
            }
            return;
        }
        double price = auctionResult.getPrice();
        if (price <= 25) {
            BidMachineFetcher.setPriceRounding(5);
        } else {
            BidMachineFetcher.setPriceRounding(1000);
        }

        PublisherAdRequest publisherAdRequest = BidMachineHelper.AdManager
                .createPublisherAdRequestBuilder(adRequest)
                .addCustomTargeting("bm_platform", "android")
                .build();

        rewardedAd = new RewardedAd(context, adUnitId);
        rewardedAd.setOnAdMetadataChangedListener(new OnAdMetadataChangedListener() {
            @Override
            public void onAdMetadataChanged() {
                if (rewardedAd == null) {
                    return;
                }
                if (isBidMachine(rewardedAd.getAdMetadata())) {
                    loadAd(context);
                } else {
                    if (listener != null) {
                        listener.onAdFailToLoad();
                    }
                }
            }
        });
        rewardedAd.loadAd(publisherAdRequest, new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {

            }

            @Override
            public void onRewardedAdFailedToLoad(int i) {
                if (listener != null) {
                    listener.onAdFailToLoad();
                }
            }
        });
    }

    private boolean isBidMachine(@Nullable Bundle metadata) {
        if (metadata != null) {
            String adTitle = metadata.getString("AdTitle");
            return TextUtils.equals(adTitle, APP_EVENT_KEY);
        }
        return false;
    }

    private void loadAd(@NonNull Context context) {
        bmRewardedAd = new io.bidmachine.rewarded.RewardedAd(context);
        bmRewardedAd.setListener(new Listener());
        bmRewardedAd.load(rewardedRequest);
    }

    @Override
    public boolean isLoaded() {
        return bmRewardedAd != null && bmRewardedAd.isLoaded() && bmRewardedAd.canShow();
    }

    @Override
    public void show(@NonNull Context context) {
        if (isLoaded()) {
            bmRewardedAd.show();
        }
    }

    @Override
    public void destroy() {
        if (bmRewardedAd != null) {
            bmRewardedAd.destroy();
            bmRewardedAd = null;
        }
        if (rewardedAd != null) {
            rewardedAd.setOnAdMetadataChangedListener(null);
            rewardedAd = null;
        }
        rewardedRequest = null;
    }

    private final class Listener implements RewardedListener {

        @Override
        public void onAdLoaded(@NonNull io.bidmachine.rewarded.RewardedAd ad) {
            if (listener != null) {
                listener.onAdLoaded();
            }
        }

        @Override
        public void onAdLoadFailed(@NonNull io.bidmachine.rewarded.RewardedAd ad,
                                   @NonNull BMError error) {
            if (listener != null) {
                listener.onAdFailToLoad();
            }
        }

        @Override
        public void onAdShown(@NonNull io.bidmachine.rewarded.RewardedAd ad) {
            if (listener != null) {
                listener.onAdShown();
            }
        }

        @Override
        public void onAdShowFailed(@NonNull io.bidmachine.rewarded.RewardedAd ad,
                                   @NonNull BMError error) {

        }

        @Override
        public void onAdImpression(@NonNull io.bidmachine.rewarded.RewardedAd ad) {

        }

        @Override
        public void onAdClicked(@NonNull io.bidmachine.rewarded.RewardedAd ad) {
            if (listener != null) {
                listener.onAdClicked();
            }
        }

        @Override
        public void onAdClosed(@NonNull io.bidmachine.rewarded.RewardedAd ad, boolean finished) {
            if (listener != null) {
                listener.onAdClosed();
            }
        }

        @Override
        public void onAdRewarded(@NonNull io.bidmachine.rewarded.RewardedAd ad) {

        }

        @Override
        public void onAdExpired(@NonNull io.bidmachine.rewarded.RewardedAd ad) {
            if (listener != null) {
                listener.onAdExpired();
            }
        }

    }

}
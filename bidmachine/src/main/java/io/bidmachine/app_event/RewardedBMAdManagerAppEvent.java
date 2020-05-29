package io.bidmachine.app_event;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.rewarded.OnAdMetadataChangedListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.HashMap;
import java.util.Map;

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

    private RewardedRequest.AdRequestListener rewardedRequestListener;
    private RewardedRequest rewardedRequest;
    private RewardedAd rewardedAd;
    private io.bidmachine.rewarded.RewardedAd bmRewardedAd;

    public RewardedBMAdManagerAppEvent(String adUnitId) {
        super(adUnitId);
    }

    @Override
    public void setListener(@Nullable BMAdManagerAppEventListener adManagerAppEventListener) {
        if (adManagerAppEventListener instanceof RewardedBMAdManagerAppEventListener) {
            this.listener = new RewardedBMAdManagerAppEventUIListener(adManagerAppEventListener);
        } else {
            super.setListener(adManagerAppEventListener);
        }
    }

    @Override
    public void load(@NonNull final Context context) {
        destroy();
        super.load(context);
        if (eventTracker != null) {
            eventTracker.addParam("ad_type", "rewarded");
        }

        rewardedRequestListener = new RewardedRequest.AdRequestListener() {
            @Override
            public void onRequestSuccess(@NonNull final RewardedRequest rewardedRequest,
                                         @NonNull AuctionResult auctionResult) {
                if (eventTracker != null) {
                    eventTracker.addParam("bm_pf_clear",
                                          String.valueOf(auctionResult.getPrice()));
                    eventTracker.addParams(BidMachineHelper.toMap(rewardedRequest));
                    eventTracker.send(Event.BMRequestSuccess);
                }

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
        };
        rewardedRequest = new RewardedRequest.Builder()
                .setListener(rewardedRequestListener)
                .build();
        rewardedRequest.request(context);

        if (eventTracker != null) {
            eventTracker.send(Event.BMRequestStart);
        }
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

                Bundle metadata = rewardedAd.getAdMetadata();

                if (eventTracker != null) {
                    Map<String, String> params = new HashMap<>();
                    if (metadata != null) {
                        for (String key : metadata.keySet()) {
                            String value = metadata.getString(key);
                            if (!TextUtils.isEmpty(value)) {
                                params.put("metadata_" + key, String.valueOf(metadata.get(key)));
                            }
                        }
                    } else {
                        params.put("metadata", "null");
                    }
                    eventTracker.send(Event.GAMMetadata, params);
                }

                if (isBidMachine(metadata)) {
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
                if (eventTracker != null) {
                    eventTracker.send(Event.GAMLoaded);
                }
            }

            @Override
            public void onRewardedAdFailedToLoad(int errorCode) {
                if (eventTracker != null) {
                    Map<String, String> params = new HashMap<>();
                    params.put("error_code", String.valueOf(errorCode));
                    params.put("error_code_message", BMAdManager.decryptGAMErrorCode(errorCode));
                    eventTracker.send(Event.GAMFailToLoad, params);
                }

                if (listener != null) {
                    listener.onAdFailToLoad();
                }
            }
        });

        if (eventTracker != null) {
            eventTracker.addParams(BidMachineHelper.toMap(adRequest));
            eventTracker.send(Event.GAMLoadStart);
        }
    }

    private boolean isBidMachine(@Nullable Bundle metadata) {
        if (metadata != null) {
            String adTitle = metadata.getString("AdTitle");
            return TextUtils.equals(adTitle, APP_EVENT_KEY);
        }
        return false;
    }

    private void loadAd(@NonNull Context context) {
        if (isDestroyed) {
            return;
        }

        bmRewardedAd = new io.bidmachine.rewarded.RewardedAd(context);
        bmRewardedAd.setListener(new Listener());
        bmRewardedAd.load(rewardedRequest);

        if (eventTracker != null) {
            eventTracker.send(Event.BMLoadStart);
        }
    }

    @Override
    public boolean isLoaded() {
        if (eventTracker != null) {
            eventTracker.send(Event.BMIsLoaded);
        }

        return bmRewardedAd != null && bmRewardedAd.isLoaded() && bmRewardedAd.canShow();
    }

    @Override
    public void show(@NonNull Context context) {
        if (isLoaded()) {
            if (eventTracker != null) {
                eventTracker.send(Event.BMShow);
            }

            bmRewardedAd.show();
        } else {
            Log.e(TAG, "Rewarded not loaded");
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (bmRewardedAd != null) {
            bmRewardedAd.destroy();
            bmRewardedAd = null;
        }
        if (rewardedAd != null) {
            rewardedAd.setOnAdMetadataChangedListener(null);
            rewardedAd = null;
        }
        if (rewardedRequest != null) {
            rewardedRequest.removeListener(rewardedRequestListener);
            rewardedRequest = null;
        }
        rewardedRequestListener = null;
    }

    private final class Listener implements RewardedListener {

        @Override
        public void onAdLoaded(@NonNull io.bidmachine.rewarded.RewardedAd ad) {
            if (eventTracker != null) {
                eventTracker.send(Event.BMLoaded);
            }

            if (listener != null) {
                listener.onAdLoaded();
            }
        }

        @Override
        public void onAdLoadFailed(@NonNull io.bidmachine.rewarded.RewardedAd ad,
                                   @NonNull BMError error) {
            if (eventTracker != null) {
                Map<String, String> params = new HashMap<>();
                params.put("bm_error", String.valueOf(error.getCode()));
                eventTracker.send(Event.BMFailToLoad, params);
            }

            if (listener != null) {
                listener.onAdFailToLoad();
            }
        }

        @Override
        public void onAdShown(@NonNull io.bidmachine.rewarded.RewardedAd ad) {
            if (eventTracker != null) {
                eventTracker.send(Event.BMShown);
            }

            if (listener != null) {
                listener.onAdShown();
            }
        }

        @Override
        public void onAdShowFailed(@NonNull io.bidmachine.rewarded.RewardedAd ad,
                                   @NonNull BMError error) {
            if (eventTracker != null) {
                Map<String, String> params = new HashMap<>();
                params.put("bm_error", String.valueOf(error.getCode()));
                eventTracker.send(Event.BMFailToShow, params);
            }
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

            destroy();
        }

        @Override
        public void onAdRewarded(@NonNull io.bidmachine.rewarded.RewardedAd ad) {
            if (listener instanceof RewardedBMAdManagerAppEventListener) {
                ((RewardedBMAdManagerAppEventListener) listener).onAdRewarded();
            }
        }

        @Override
        public void onAdExpired(@NonNull io.bidmachine.rewarded.RewardedAd ad) {
            if (eventTracker != null) {
                eventTracker.send(Event.BMExpired);
            }

            if (listener != null) {
                listener.onAdExpired();
            }
        }

    }

}
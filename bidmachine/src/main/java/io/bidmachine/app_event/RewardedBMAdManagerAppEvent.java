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
        super.load(context);
        eventParams.put("ad_type", "rewarded");
        destroy();

        rewardedRequest = new RewardedRequest.Builder()
                .setListener(new RewardedRequest.AdRequestListener() {
                    @Override
                    public void onRequestSuccess(@NonNull final RewardedRequest rewardedRequest,
                                                 @NonNull AuctionResult auctionResult) {
                        eventParams.put("bm_pf_clear", String.valueOf(auctionResult.getPrice()));
                        eventParams.putAll(BidMachineHelper.toMap(rewardedRequest));
                        Event.BMRequestSuccess.send(eventParams);

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

        Event.BMRequestStart.send(eventParams);
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

                Map<String, String> params = new HashMap<>(eventParams);
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
                Event.GAMMetadata.send(params);

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
                Event.GAMLoaded.send(eventParams);
            }

            @Override
            public void onRewardedAdFailedToLoad(int errorCode) {
                Map<String, String> params = new HashMap<>(eventParams);
                params.put("error_code", String.valueOf(errorCode));
                params.put("error_code_message", BMAdManager.decryptGAMErrorCode(errorCode));
                Event.GAMFailToLoad.send(params);

                if (listener != null) {
                    listener.onAdFailToLoad();
                }
            }
        });

        eventParams.putAll(BidMachineHelper.toMap(adRequest));
        Event.GAMLoadStart.send(eventParams);
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

        Event.BMLoadStart.send(eventParams);
    }

    @Override
    public boolean isLoaded() {
        Event.BMIsLoaded.send(eventParams);

        return bmRewardedAd != null && bmRewardedAd.isLoaded() && bmRewardedAd.canShow();
    }

    @Override
    public void show(@NonNull Context context) {
        if (isLoaded()) {
            Event.BMShow.send(eventParams);

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
            Event.BMLoaded.send(eventParams);

            if (listener != null) {
                listener.onAdLoaded();
            }
        }

        @Override
        public void onAdLoadFailed(@NonNull io.bidmachine.rewarded.RewardedAd ad,
                                   @NonNull BMError error) {
            Map<String, String> params = new HashMap<>(eventParams);
            params.put("bm_error", String.valueOf(error.getCode()));
            Event.BMFailToLoad.send(params);

            if (listener != null) {
                listener.onAdFailToLoad();
            }
        }

        @Override
        public void onAdShown(@NonNull io.bidmachine.rewarded.RewardedAd ad) {
            Event.BMShown.send(eventParams);

            if (listener != null) {
                listener.onAdShown();
            }
        }

        @Override
        public void onAdShowFailed(@NonNull io.bidmachine.rewarded.RewardedAd ad,
                                   @NonNull BMError error) {
            Map<String, String> params = new HashMap<>(eventParams);
            params.put("bm_error", String.valueOf(error.getCode()));
            Event.BMFailToShow.send(params);
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
            if (listener instanceof RewardedBMAdManagerAppEventListener) {
                ((RewardedBMAdManagerAppEventListener) listener).onAdRewarded();
            }
        }

        @Override
        public void onAdExpired(@NonNull io.bidmachine.rewarded.RewardedAd ad) {
            Event.BMExpired.send(eventParams);

            if (listener != null) {
                listener.onAdExpired();
            }
        }

    }

}
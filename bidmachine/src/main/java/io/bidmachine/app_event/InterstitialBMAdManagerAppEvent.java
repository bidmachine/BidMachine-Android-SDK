package io.bidmachine.app_event;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;

import io.bidmachine.AdRequest;
import io.bidmachine.BidMachineFetcher;
import io.bidmachine.BidMachineHelper;
import io.bidmachine.core.Utils;
import io.bidmachine.interstitial.InterstitialAd;
import io.bidmachine.interstitial.InterstitialListener;
import io.bidmachine.interstitial.InterstitialRequest;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.utils.BMError;

public class InterstitialBMAdManagerAppEvent extends BMAdManagerAppEvent {

    private static final String APP_EVENT_KEY = "bidmachine-interstitial";

    private InterstitialRequest interstitialRequest;
    private PublisherInterstitialAd publisherInterstitialAd;
    private InterstitialAd interstitialAd;

    public InterstitialBMAdManagerAppEvent(String adUnitId) {
        super(adUnitId);
    }

    @Override
    public void load(@NonNull final Context context) {
        destroy();

        interstitialRequest = new InterstitialRequest.Builder()
                .setListener(new InterstitialRequest.AdRequestListener() {
                    @Override
                    public void onRequestSuccess(final @NonNull InterstitialRequest interstitialRequest,
                                                 @NonNull AuctionResult auctionResult) {
                        Utils.onUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadPublisherAd(context, interstitialRequest);
                            }
                        });
                    }

                    @Override
                    public void onRequestFailed(@NonNull InterstitialRequest interstitialRequest,
                                                @NonNull BMError bmError) {
                        if (listener != null) {
                            listener.onAdFailToLoad();
                        }
                    }

                    @Override
                    public void onRequestExpired(@NonNull InterstitialRequest interstitialRequest) {
                        if (listener != null) {
                            listener.onAdExpired();
                        }
                    }
                })
                .build();
        interstitialRequest.request(context);
    }

    private void loadPublisherAd(@NonNull final Context context, @NonNull AdRequest adRequest) {
        AuctionResult auctionResult = adRequest.getAuctionResult();
        if (auctionResult == null) {
            if (listener != null) {
                listener.onAdFailToLoad();
            }
            return;
        }
        double price = auctionResult.getPrice();
        if (price <= 10) {
            BidMachineFetcher.setPriceRounding(1);
        } else {
            BidMachineFetcher.setPriceRounding(1000);
        }

        PublisherAdRequest publisherAdRequest = BidMachineHelper.AdManager
                .createPublisherAdRequestBuilder(adRequest)
                .addCustomTargeting("bm_platform", "android")
                .build();

        publisherInterstitialAd = new PublisherInterstitialAd(context);
        publisherInterstitialAd.setAdUnitId(adUnitId);
        publisherInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {

            }

            @Override
            public void onAdFailedToLoad(int i) {
                if (listener != null) {
                    listener.onAdFailToLoad();
                }
            }
        });
        publisherInterstitialAd.setAppEventListener(new AppEventListener() {
            @Override
            public void onAppEvent(String key, String value) {
                if (isBidMachine(key)) {
                    loadAd(context);
                } else {
                    if (listener != null) {
                        listener.onAdFailToLoad();
                    }
                }
            }
        });
        publisherInterstitialAd.loadAd(publisherAdRequest);
    }

    private boolean isBidMachine(@Nullable String key) {
        return TextUtils.equals(key, APP_EVENT_KEY);
    }

    private void loadAd(@NonNull Context context) {
        interstitialAd = new InterstitialAd(context);
        interstitialAd.setListener(new Listener());
        interstitialAd.load(interstitialRequest);
    }

    @Override
    public boolean isLoaded() {
        return interstitialAd != null && interstitialAd.isLoaded() && interstitialAd.canShow();
    }

    @Override
    public void show(@NonNull final Context context) {
        if (isLoaded()) {
            interstitialAd.show();
        }
    }

    @Override
    public void destroy() {
        if (interstitialAd != null) {
            interstitialAd.destroy();
            interstitialAd = null;
        }
        publisherInterstitialAd = null;
        interstitialRequest = null;
    }

    private final class Listener implements InterstitialListener {

        @Override
        public void onAdLoaded(@NonNull InterstitialAd ad) {
            if (listener != null) {
                listener.onAdLoaded();
            }
        }

        @Override
        public void onAdLoadFailed(@NonNull InterstitialAd ad, @NonNull BMError error) {
            if (listener != null) {
                listener.onAdFailToLoad();
            }
        }

        @Override
        public void onAdShown(@NonNull InterstitialAd ad) {
            if (listener != null) {
                listener.onAdShown();
            }
        }

        @Override
        public void onAdShowFailed(@NonNull InterstitialAd ad, @NonNull BMError error) {

        }

        @Override
        public void onAdImpression(@NonNull InterstitialAd ad) {

        }

        @Override
        public void onAdClicked(@NonNull InterstitialAd ad) {
            if (listener != null) {
                listener.onAdClicked();
            }
        }

        @Override
        public void onAdClosed(@NonNull InterstitialAd ad, boolean finished) {
            if (listener != null) {
                listener.onAdClosed();
            }
        }

        @Override
        public void onAdExpired(@NonNull InterstitialAd ad) {
            if (listener != null) {
                listener.onAdExpired();
            }
        }

    }

}
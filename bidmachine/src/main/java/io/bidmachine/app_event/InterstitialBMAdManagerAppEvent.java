package io.bidmachine.app_event;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.ads.mediation.bidmachine.BidMachineBundleBuilder;
import com.google.ads.mediation.bidmachine.BidMachineCustomEventInterstitial;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;

import java.util.Map;

import io.bidmachine.AdRequest;
import io.bidmachine.BidMachineFetcher;
import io.bidmachine.BidMachineHelper;
import io.bidmachine.core.Utils;
import io.bidmachine.interstitial.InterstitialRequest;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.utils.BMError;

public class InterstitialBMAdManagerAppEvent extends BMAdManagerAppEvent {

    private InterstitialRequest interstitialRequest;
    private Bundle localExtras;
    private PublisherInterstitialAd interstitialAd;
    private BidMachineCustomEventInterstitial bidMachineCustomEventInterstitial;

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
        Map<String, String> fetchParams = prepareFetchParams(adRequest);
        if (fetchParams == null) {
            if (listener != null) {
                listener.onAdFailToLoad();
            }
            return;
        }
        localExtras = new BidMachineBundleBuilder()
                .setFetchParams(fetchParams)
                .build();
        PublisherAdRequest publisherAdRequest = BidMachineHelper.AdManager
                .createPublisherAdRequestBuilder(adRequest)
                .build();

        interstitialAd = new PublisherInterstitialAd(context);
        interstitialAd.setAdUnitId(adUnitId);
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                if (listener != null) {
                    listener.onAdFailToLoad();
                }
            }
        });
        interstitialAd.setAppEventListener(new AppEventListener() {
            @Override
            public void onAppEvent(String key, String value) {
                loadCustomAdapter(context);
            }
        });
        interstitialAd.loadAd(publisherAdRequest);
    }

    @Nullable
    private Map<String, String> prepareFetchParams(@NonNull AdRequest adRequest) {
        AuctionResult auctionResult = adRequest.getAuctionResult();
        if (auctionResult == null) {
            return null;
        }
        double price = auctionResult.getPrice();
        if (price <= 10) {
            BidMachineFetcher.setPriceRounding(1);
        } else {
            BidMachineFetcher.setPriceRounding(1000);
        }
        return BidMachineFetcher.fetch(adRequest);
    }

    private void loadCustomAdapter(@NonNull Context context) {
        if (localExtras == null) {
            if (listener != null) {
                listener.onAdFailToLoad();
            }
            return;
        }
        bidMachineCustomEventInterstitial = new BidMachineCustomEventInterstitial();
        bidMachineCustomEventInterstitial.requestInterstitialAd(
                context,
                new CustomAdapterListener(),
                null,
                BMAdManagerMediationAdRequest.instance,
                localExtras);
    }

    @Override
    public boolean isLoaded() {
        return bidMachineCustomEventInterstitial != null && isLoaded;
    }

    @Override
    public void show() {
        if (isLoaded()) {
            bidMachineCustomEventInterstitial.showInterstitial();
        }
    }

    @Override
    public void destroy() {
        if (bidMachineCustomEventInterstitial != null) {
            bidMachineCustomEventInterstitial.onDestroy();
            bidMachineCustomEventInterstitial = null;
        }
        interstitialAd = null;
        localExtras = null;
        if (interstitialRequest != null) {
            BidMachineFetcher.release(interstitialRequest);
            interstitialRequest = null;
        }
        isLoaded = false;
    }

    private final class CustomAdapterListener implements CustomEventInterstitialListener {

        @Override
        public void onAdLoaded() {
            isLoaded = true;
            if (listener != null) {
                listener.onAdLoaded();
            }
        }

        @Override
        public void onAdFailedToLoad(int i) {
            if (listener != null) {
                listener.onAdFailToLoad();
            }
        }

        @Override
        public void onAdOpened() {
            if (listener != null) {
                listener.onAdShown();
            }
        }

        @Override
        public void onAdClicked() {
            if (listener != null) {
                listener.onAdClicked();
            }
        }

        @Override
        public void onAdClosed() {
            if (listener != null) {
                listener.onAdClosed();
            }
        }

        @Override
        public void onAdLeftApplication() {

        }

    }

}
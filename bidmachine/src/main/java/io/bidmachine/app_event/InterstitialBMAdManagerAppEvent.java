package io.bidmachine.app_event;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;

import java.util.HashMap;
import java.util.Map;

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

    private InterstitialRequest.AdRequestListener interstitialRequestListener;
    private InterstitialRequest interstitialRequest;
    private PublisherInterstitialAd publisherInterstitialAd;
    private InterstitialAd interstitialAd;

    public InterstitialBMAdManagerAppEvent(String adUnitId) {
        super(adUnitId);
    }

    @Override
    public void load(@NonNull final Context context) {
        destroy();
        super.load(context);
        if (eventTracker != null) {
            eventTracker.addParam("ad_type", "interstitial");
        }

        interstitialRequestListener = new InterstitialRequest.AdRequestListener() {
            @Override
            public void onRequestSuccess(@NonNull final InterstitialRequest interstitialRequest,
                                         @NonNull AuctionResult auctionResult) {
                if (eventTracker != null) {
                    eventTracker.addParam("bm_pf_clear",
                                          String.valueOf(auctionResult.getPrice()));
                    eventTracker.addParams(BidMachineHelper.toMap(interstitialRequest));
                    eventTracker.send(Event.BMRequestSuccess);
                }

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
        };
        interstitialRequest = new InterstitialRequest.Builder()
                .setListener(interstitialRequestListener)
                .build();
        interstitialRequest.request(context);

        if (eventTracker != null) {
            eventTracker.send(Event.BMRequestStart);
        }
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
                if (eventTracker != null) {
                    eventTracker.send(Event.GAMLoaded);
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
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
        publisherInterstitialAd.setAppEventListener(new AppEventListener() {
            @Override
            public void onAppEvent(String key, String value) {
                if (eventTracker != null) {
                    Map<String, String> params = new HashMap<>();
                    params.put("app_event_key", String.valueOf(key));
                    params.put("app_event_value", String.valueOf(value));
                    eventTracker.send(Event.GAMAppEvent, params);
                }

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

        if (eventTracker != null) {
            eventTracker.addParams(BidMachineHelper.toMap(adRequest));
            eventTracker.send(Event.GAMLoadStart);
        }
    }

    private boolean isBidMachine(@Nullable String key) {
        return TextUtils.equals(key, APP_EVENT_KEY);
    }

    private void loadAd(@NonNull Context context) {
        if (isDestroyed) {
            return;
        }

        interstitialAd = new InterstitialAd(context);
        interstitialAd.setListener(new Listener());
        interstitialAd.load(interstitialRequest);

        if (eventTracker != null) {
            eventTracker.send(Event.BMLoadStart);
        }
    }

    @Override
    public boolean isLoaded() {
        if (eventTracker != null) {
            eventTracker.send(Event.BMIsLoaded);
        }

        return interstitialAd != null && interstitialAd.isLoaded() && interstitialAd.canShow();
    }

    @Override
    public void show(@NonNull final Context context) {
        if (isLoaded()) {
            if (eventTracker != null) {
                eventTracker.send(Event.BMShow);
            }

            interstitialAd.show();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (interstitialAd != null) {
            interstitialAd.destroy();
            interstitialAd = null;
        }
        if (publisherInterstitialAd != null) {
            publisherInterstitialAd = null;
        }
        if (interstitialRequest != null) {
            interstitialRequest.removeListener(interstitialRequestListener);
            interstitialRequest = null;
        }
        interstitialRequestListener = null;
    }

    private final class Listener implements InterstitialListener {

        @Override
        public void onAdLoaded(@NonNull InterstitialAd ad) {
            if (eventTracker != null) {
                eventTracker.send(Event.BMLoaded);
            }

            if (listener != null) {
                listener.onAdLoaded();
            }
        }

        @Override
        public void onAdLoadFailed(@NonNull InterstitialAd ad, @NonNull BMError error) {
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
        public void onAdShown(@NonNull InterstitialAd ad) {
            if (eventTracker != null) {
                eventTracker.send(Event.BMShown);
            }

            if (listener != null) {
                listener.onAdShown();
            }
        }

        @Override
        public void onAdShowFailed(@NonNull InterstitialAd ad, @NonNull BMError error) {
            if (eventTracker != null) {
                Map<String, String> params = new HashMap<>();
                params.put("bm_error", String.valueOf(error.getCode()));
                eventTracker.send(Event.BMFailToShow, params);
            }
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
            if (eventTracker != null) {
                eventTracker.send(Event.BMExpired);
            }

            if (listener != null) {
                listener.onAdExpired();
            }
        }

    }

}
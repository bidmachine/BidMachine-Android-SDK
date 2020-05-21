package io.bidmachine.app_event;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.AdRequest;
import io.bidmachine.BidMachineFetcher;
import io.bidmachine.BidMachineHelper;
import io.bidmachine.banner.BannerListener;
import io.bidmachine.banner.BannerRequest;
import io.bidmachine.banner.BannerSize;
import io.bidmachine.banner.BannerView;
import io.bidmachine.core.Utils;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.utils.BMError;

class BannerViewBMAdManagerAppEvent extends BMAdManagerAppEvent {

    private static final String APP_EVENT_KEY = "bidmachine-banner";
    private static final AdSize adSize = AdSize.BANNER;

    private BannerRequest bannerRequest;
    private PublisherAdView publisherAdView;
    private BannerView bannerView;
    private BMPopupWindow bmPopupWindow;

    BannerViewBMAdManagerAppEvent(String adUnitId) {
        super(adUnitId);
    }

    @Override
    public void load(@NonNull final Context context) {
        super.load(context);
        eventParams.put("ad_type", "banner");

        bannerRequest = new BannerRequest.Builder()
                .setSize(BannerSize.Size_320x50)
                .setListener(new BannerRequest.AdRequestListener() {
                    @Override
                    public void onRequestSuccess(@NonNull final BannerRequest bannerRequest,
                                                 @NonNull AuctionResult auctionResult) {
                        eventParams.put("bm_pf_clear", String.valueOf(auctionResult.getPrice()));
                        eventParams.putAll(BidMachineHelper.toMap(bannerRequest));
                        Event.BMRequestSuccess.send(eventParams);

                        Utils.onUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadPublisherAd(context, bannerRequest);
                            }
                        });
                    }

                    @Override
                    public void onRequestFailed(@NonNull BannerRequest request,
                                                @NonNull BMError error) {
                        if (listener != null) {
                            listener.onAdFailToLoad();
                        }
                    }

                    @Override
                    public void onRequestExpired(@NonNull BannerRequest request) {
                        if (listener != null) {
                            listener.onAdExpired();
                        }
                    }
                })
                .build();
        bannerRequest.request(context);

        Event.BMRequestStart.send(eventParams);
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
        if (price <= 1) {
            BidMachineFetcher.setPriceRounding(0.2);
        } else {
            BidMachineFetcher.setPriceRounding(1000);
        }

        PublisherAdRequest publisherAdRequest = BidMachineHelper.AdManager
                .createPublisherAdRequestBuilder(adRequest)
                .addCustomTargeting("bm_platform", "android")
                .build();

        publisherAdView = new PublisherAdView(context);
        publisherAdView.setAdUnitId(adUnitId);
        publisherAdView.setAdSizes(adSize);
        publisherAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Event.GAMLoaded.send(eventParams);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Map<String, String> params = new HashMap<>(eventParams);
                params.put("error_code", String.valueOf(errorCode));
                params.put("error_code_message", BMAdManager.decryptGAMErrorCode(errorCode));
                Event.GAMFailToLoad.send(params);

                if (listener != null) {
                    listener.onAdFailToLoad();
                }
            }
        });
        publisherAdView.setAppEventListener(new AppEventListener() {
            @Override
            public void onAppEvent(String key, String value) {
                Map<String, String> params = new HashMap<>(eventParams);
                params.put("app_event_key", String.valueOf(key));
                params.put("app_event_value", String.valueOf(value));
                Event.GAMAppEvent.send(params);

                if (isBidMachine(key)) {
                    loadAd(context);
                } else {
                    if (listener != null) {
                        listener.onAdFailToLoad();
                    }
                }
            }
        });
        publisherAdView.loadAd(publisherAdRequest);

        eventParams.putAll(BidMachineHelper.toMap(adRequest));
        Event.GAMLoadStart.send(eventParams);
    }

    private boolean isBidMachine(@Nullable String key) {
        return TextUtils.equals(key, APP_EVENT_KEY);
    }

    private void loadAd(@NonNull Context context) {
        bannerView = new BannerView(context);
        bannerView.setListener(new Listener());
        bannerView.load(bannerRequest);

        Event.BMLoadStart.send(eventParams);
    }

    @Override
    public boolean isLoaded() {
        Event.BMIsLoaded.send(eventParams);

        return bannerView != null && bannerView.isLoaded() && bannerView.canShow();
    }

    @Override
    public void show(@NonNull final Context context) {
        if (context instanceof Activity) {
            if (isLoaded()) {
                Event.BMShow.send(eventParams);

                bmPopupWindow.showView((Activity) context,
                                       bannerView,
                                       adSize.getWidth(),
                                       adSize.getHeight());
            }
        } else {
            Map<String, String> params = new HashMap<>(eventParams);
            params.put("show_error_message", "WRONG_CONTEXT");
            Event.BMFailToShow.send(params);

            Log.e(TAG, "Activity needed to display banner");
        }
    }

    @Override
    public void hide() {
        if (bmPopupWindow != null) {
            bmPopupWindow.hide();
        }
    }

    @Override
    public void destroy() {
        hide();
        if (bmPopupWindow != null) {
            bmPopupWindow = null;
        }
        if (bannerView != null) {
            bannerView.destroy();
            bannerView = null;
        }
        if (publisherAdView != null) {
            publisherAdView.destroy();
            publisherAdView = null;
        }
        bannerRequest = null;
    }

    private final class Listener implements BannerListener {

        @Override
        public void onAdLoaded(@NonNull BannerView ad) {
            Event.BMLoaded.send(eventParams);

            bmPopupWindow = new BMPopupWindow();
            if (listener != null) {
                listener.onAdLoaded();
            }
        }

        @Override
        public void onAdLoadFailed(@NonNull BannerView ad, @NonNull BMError error) {
            Map<String, String> params = new HashMap<>(eventParams);
            params.put("bm_error", String.valueOf(error.getCode()));
            Event.BMFailToLoad.send(params);

            if (listener != null) {
                listener.onAdFailToLoad();
            }
        }

        @Override
        public void onAdShown(@NonNull BannerView ad) {
            Event.BMShown.send(eventParams);

            if (listener != null) {
                listener.onAdShown();
            }
        }

        @Override
        public void onAdImpression(@NonNull BannerView ad) {

        }

        @Override
        public void onAdClicked(@NonNull BannerView ad) {
            if (listener != null) {
                listener.onAdClicked();
            }
        }

        @Override
        public void onAdExpired(@NonNull BannerView ad) {
            Event.BMExpired.send(eventParams);

            if (listener != null) {
                listener.onAdExpired();
            }
        }

    }

}
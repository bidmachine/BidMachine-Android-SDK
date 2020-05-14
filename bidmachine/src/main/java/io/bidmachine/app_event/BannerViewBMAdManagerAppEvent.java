package io.bidmachine.app_event;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.google.ads.mediation.bidmachine.BidMachineBundleBuilder;
import com.google.ads.mediation.bidmachine.BidMachineCustomEventBanner;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;

import java.util.Map;

import io.bidmachine.AdRequest;
import io.bidmachine.BidMachineFetcher;
import io.bidmachine.BidMachineHelper;
import io.bidmachine.banner.BannerRequest;
import io.bidmachine.banner.BannerSize;
import io.bidmachine.core.Utils;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.utils.BMError;

class BannerViewBMAdManagerAppEvent extends BMAdManagerAppEvent {

    private static final AdSize adSize = AdSize.BANNER;

    private BannerRequest bannerRequest;
    private Bundle localExtras;
    private PublisherAdView publisherAdView;
    private BidMachineCustomEventBanner bidMachineCustomEventBanner;
    private View bannerView;
    private BMPopupWindow bmPopupWindow;

    BannerViewBMAdManagerAppEvent(String adUnitId) {
        super(adUnitId);
    }

    @Override
    public void load(@NonNull final Context context) {
        bannerRequest = new BannerRequest.Builder()
                .setSize(BannerSize.Size_320x50)
                .setListener(new BannerRequest.AdRequestListener() {
                    @Override
                    public void onRequestSuccess(@NonNull final BannerRequest bannerRequest,
                                                 @NonNull AuctionResult auctionResult) {
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

        publisherAdView = new PublisherAdView(context);
        publisherAdView.setAdUnitId(adUnitId);
        publisherAdView.setAdSizes(adSize);
        publisherAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                if (listener != null) {
                    listener.onAdFailToLoad();
                }
            }
        });
        publisherAdView.setAppEventListener(new AppEventListener() {
            @Override
            public void onAppEvent(String key, String value) {
                loadCustomAdapter(context);
            }
        });
        publisherAdView.loadAd(publisherAdRequest);
    }

    @Nullable
    private Map<String, String> prepareFetchParams(@NonNull AdRequest adRequest) {
        AuctionResult auctionResult = adRequest.getAuctionResult();
        if (auctionResult == null) {
            return null;
        }
        double price = auctionResult.getPrice();
        if (price <= 1) {
            BidMachineFetcher.setPriceRounding(0.2);
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
        bidMachineCustomEventBanner = new BidMachineCustomEventBanner();
        bidMachineCustomEventBanner.requestBannerAd(
                context,
                new BannerViewBMAdManagerAppEvent.CustomAdapterListener(),
                null,
                adSize,
                BMAdManagerMediationAdRequest.instance,
                localExtras);
    }

    @Override
    public boolean isLoaded() {
        return bannerView != null && isLoaded;
    }

    @Override
    public void show(@NonNull final Context context) {
        if (context instanceof Activity && isLoaded()) {
            bmPopupWindow.showView((Activity) context,
                                   bannerView,
                                   adSize.getWidth(),
                                   adSize.getHeight());
        } else {
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
        bannerView = null;
        if (bidMachineCustomEventBanner != null) {
            bidMachineCustomEventBanner.onDestroy();
            bidMachineCustomEventBanner = null;
        }
        if (publisherAdView != null) {
            publisherAdView.destroy();
            publisherAdView = null;
        }
        localExtras = null;
        if (bannerRequest != null) {
            BidMachineFetcher.release(bannerRequest);
            bannerRequest = null;
        }
        isLoaded = false;
    }

    private final class CustomAdapterListener implements CustomEventBannerListener {

        @Override
        public void onAdLoaded(View view) {
            bannerView = view;
            bmPopupWindow = new BMPopupWindow();
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

        }

        @Override
        public void onAdClicked() {
            if (listener != null) {
                listener.onAdClicked();
            }
        }

        @Override
        public void onAdClosed() {

        }

        @Override
        public void onAdLeftApplication() {

        }

    }

}
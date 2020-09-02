package io.bidmachine;

import android.graphics.Point;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.explorestack.protobuf.Any;
import com.explorestack.protobuf.InvalidProtocolBufferException;
import com.explorestack.protobuf.Message;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.bidmachine.banner.BannerSize;
import io.bidmachine.core.Logger;
import io.bidmachine.displays.DisplayPlacementBuilder;
import io.bidmachine.displays.NativePlacementBuilder;
import io.bidmachine.displays.PlacementBuilder;
import io.bidmachine.displays.VideoPlacementBuilder;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.protobuf.headerbidding.HeaderBiddingAd;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;

public enum AdsType {

    Banner("banner",
           new ApiRequest.ApiAuctionDataBinder(),
           new PlacementBuilder[]{
                   new DisplayPlacementBuilder<UnifiedBannerAdRequestParams>(false, true) {
                       @Override
                       public Point getSize(ContextProvider contextProvider,
                                            UnifiedBannerAdRequestParams bannerRequest) {
                           BannerSize bannerSize = bannerRequest.getBannerSize();
                           return new Point(bannerSize.width, bannerSize.height);
                       }
                   }}),
    Interstitial("interstitial",
                 new ApiRequest.ApiAuctionDataBinder(),
                 new PlacementBuilder[]{
                         new DisplayPlacementBuilder(true, true),
                         new VideoPlacementBuilder(true, true)}),
    Rewarded("rewarded",
             new ApiRequest.ApiAuctionDataBinder(),
             new PlacementBuilder[]{
                     new DisplayPlacementBuilder(true, true),
                     new VideoPlacementBuilder(false, true)}),
    Native("native",
           new ApiRequest.ApiAuctionDataBinder(),
           new PlacementBuilder[]{
                   new NativePlacementBuilder(false)});

    private final String name;
    private final ApiRequest.ApiAuctionDataBinder binder;
    private final PlacementBuilder[] placementBuilders;
    private final Map<String, NetworkConfig> networkConfigs = new HashMap<>();
    private final Executor placementCreateExecutor = Executors.newFixedThreadPool(
            Math.max(8, Runtime.getRuntime().availableProcessors() * 4));

    AdsType(@NonNull String name,
            @NonNull ApiRequest.ApiAuctionDataBinder binder,
            @NonNull PlacementBuilder[] placementBuilders) {
        this.name = name;
        this.binder = binder;
        this.placementBuilders = placementBuilders;
    }

    public String getName() {
        return name;
    }

    NetworkConfig obtainNetworkConfig(@NonNull Ad ad) {
        NetworkConfig networkConfig = obtainHeaderBiddingAdNetworkConfig(ad);
        if (networkConfig == null) {
            if (this == AdsType.Native) {
                networkConfig = NetworkRegistry.getConfig(NetworkRegistry.Nast);
            } else if (ad.hasDisplay()) {
                networkConfig = NetworkRegistry.getConfig(NetworkRegistry.Mraid);
            } else if (ad.hasVideo()) {
                networkConfig = NetworkRegistry.getConfig(NetworkRegistry.Vast);
            }
        }
        return networkConfig;
    }

    @Nullable
    private NetworkConfig obtainHeaderBiddingAdNetworkConfig(@NonNull Ad ad) {
        List<Any> extensions = null;
        if (ad.hasDisplay()) {
            Ad.Display display = ad.getDisplay();
            if (display.hasBanner()) {
                extensions = display.getBanner().getExtProtoList();
            } else if (display.hasNative()) {
                extensions = display.getNative().getExtProtoList();
            }
        }
        if ((extensions == null || extensions.isEmpty()) && ad.hasVideo()) {
            extensions = ad.getVideo().getExtProtoList();
        }
        if (extensions != null) {
            return obtainHeaderBiddingAdNetworkConfig(extensions);
        }
        return null;
    }

    @Nullable
    private NetworkConfig obtainHeaderBiddingAdNetworkConfig(@NonNull List<Any> extensions) {
        for (Any extension : extensions) {
            if (extension.is(HeaderBiddingAd.class)) {
                try {
                    HeaderBiddingAd headerBiddingAd = extension.unpack(HeaderBiddingAd.class);
                    return NetworkRegistry.getConfig(headerBiddingAd.getBidder());
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    ApiRequest.ApiAuctionDataBinder getBinder() {
        return binder;
    }

    @SuppressWarnings("unchecked")
    AdObjectParams createAdObjectParams(@NonNull ContextProvider contextProvider,
                                        @NonNull UnifiedAdRequestParams adRequestParams,
                                        @NonNull Response.Seatbid seatbid,
                                        @NonNull Response.Seatbid.Bid bid,
                                        @NonNull Ad ad) {
        for (PlacementBuilder builder : placementBuilders) {
            AdObjectParams params = builder.createAdObjectParams(
                    contextProvider, adRequestParams, seatbid, bid, ad);
            if (params != null) {
                return params;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    void collectDisplayPlacements(@NonNull final ContextProvider contextProvider,
                                  @NonNull final AdRequest adRequest,
                                  @NonNull final UnifiedAdRequestParams adRequestParams,
                                  @NonNull final ArrayList<Message.Builder> outList) {
        final CountDownLatch syncLock = new CountDownLatch(placementBuilders.length);
        for (final PlacementBuilder placementBuilder : placementBuilders) {
            if (adRequest.isPlacementBuilderMatch(placementBuilder)) {
                placementCreateExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        placementBuilder.createPlacement(
                                contextProvider,
                                adRequestParams,
                                AdsType.this,
                                networkConfigs.values(),
                                new PlacementBuilder.PlacementCreateCallback() {
                                    @Override
                                    public void onCreated(@Nullable Message.Builder placement) {
                                        if (placement != null) {
                                            synchronized (outList) {
                                                outList.add(placement);
                                            }
                                        }
                                        syncLock.countDown();
                                    }
                                });
                    }
                });
            } else {
                syncLock.countDown();
            }
        }
        try {
            syncLock.await();
        } catch (InterruptedException e) {
            Logger.log(e);
        }
    }

    void addNetworkConfig(@NonNull String key, @NonNull NetworkConfig networkConfig) {
        networkConfigs.put(key, networkConfig);
    }

}



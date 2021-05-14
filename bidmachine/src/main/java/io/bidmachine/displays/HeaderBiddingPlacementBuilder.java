package io.bidmachine.displays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.explorestack.protobuf.Any;
import com.explorestack.protobuf.InvalidProtocolBufferException;
import com.explorestack.protobuf.Message;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.bidmachine.AdContentType;
import io.bidmachine.AdsType;
import io.bidmachine.BidMachineEvents;
import io.bidmachine.ContextProvider;
import io.bidmachine.HeaderBiddingAdRequestParams;
import io.bidmachine.HeaderBiddingAdapter;
import io.bidmachine.HeaderBiddingCollectParamsCallback;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfig;
import io.bidmachine.SimpleTrackingObject;
import io.bidmachine.TrackEventInfo;
import io.bidmachine.TrackEventType;
import io.bidmachine.TrackingObject;
import io.bidmachine.core.Logger;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.protobuf.headerbidding.HeaderBiddingAd;
import io.bidmachine.protobuf.headerbidding.HeaderBiddingPlacement;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.utils.BMError;

class HeaderBiddingPlacementBuilder<UnifiedAdRequestParamsType extends UnifiedAdRequestParams> {

    private static final long HEADER_BIDDING_PREPARE_TIMEOUT_SEC = 10;

    Message.Builder createPlacement(@NonNull ContextProvider contextProvider,
                                    @NonNull UnifiedAdRequestParamsType adRequestParams,
                                    @NonNull AdsType adsType,
                                    @NonNull AdContentType adContentType,
                                    @NonNull Collection<NetworkConfig> networkConfigs) {
        List<AdUnitPreloadTask> preloadTasks = new ArrayList<>();
        for (NetworkConfig networkConfig : networkConfigs) {
            NetworkAdapter adapter = networkConfig.obtainNetworkAdapter();
            if (adapter instanceof HeaderBiddingAdapter) {
                List<Map<String, String>> mediationConfigs =
                        networkConfig.peekMediationConfig(adsType, adRequestParams, adContentType);
                if (mediationConfigs != null) {
                    for (Map<String, String> config : mediationConfigs) {
                        preloadTasks.add(
                                new AdUnitPreloadTask<>(contextProvider,
                                                        (HeaderBiddingAdapter) adapter,
                                                        adsType,
                                                        adContentType,
                                                        adRequestParams,
                                                        config));
                    }
                }
            }
        }
        if (!preloadTasks.isEmpty()) {
            TrackingObject trackingObject = new SimpleTrackingObject();
            BidMachineEvents.eventStart(trackingObject,
                                        TrackEventType.HeaderBiddingNetworksPrepare);
            try {
                CountDownLatch syncLock = new CountDownLatch(preloadTasks.size());
                for (AdUnitPreloadTask task : preloadTasks) {
                    task.execute(syncLock);
                }
                try {
                    syncLock.await(HEADER_BIDDING_PREPARE_TIMEOUT_SEC, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Logger.log(e);
                }
                List<HeaderBiddingPlacement.AdUnit> adUnitList = null;
                for (AdUnitPreloadTask task : preloadTasks) {
                    HeaderBiddingPlacement.AdUnit adUnit = task.getAdUnit();
                    if (adUnit != null) {
                        if (adUnitList == null) {
                            adUnitList = new ArrayList<>();
                        }
                        adUnitList.add(adUnit);
                    } else if (!task.isFinished()) {
                        // In case when we reach this block after lock timeout - cancel tasks which are not finished yet
                        task.cancel();
                    }
                }
                if (adUnitList != null && !adUnitList.isEmpty()) {
                    HeaderBiddingPlacement.Builder placementBuilder = HeaderBiddingPlacement.newBuilder();
                    placementBuilder.addAllAdUnits(adUnitList);
                    return placementBuilder;
                }
            } finally {
                BidMachineEvents.eventFinish(trackingObject,
                                             TrackEventType.HeaderBiddingNetworksPrepare,
                                             adsType,
                                             null);
            }
        }
        return null;
    }

    AdObjectParams createAdObjectParams(@NonNull Response.Seatbid seatbid,
                                        @NonNull Response.Seatbid.Bid bid,
                                        @NonNull Ad ad) {
        HeaderBiddingAd headerBiddingAd = obtainHeaderBiddingAd(ad);
        return headerBiddingAd != null
                ? new HeaderBiddingAdObjectParams(seatbid, bid, ad, headerBiddingAd)
                : null;
    }

    @Nullable
    HeaderBiddingAd obtainHeaderBiddingAd(@NonNull Ad ad) {
        HeaderBiddingAd headerBiddingAd = null;
        if (ad.hasDisplay()) {
            Ad.Display display = ad.getDisplay();
            if (display.hasBanner()) {
                headerBiddingAd = obtainHeaderBiddingAd(display.getBanner().getExtProtoList());
            }
            if (headerBiddingAd == null && display.hasNative()) {
                headerBiddingAd = obtainHeaderBiddingAd(display.getNative().getExtProtoList());
            }
        }
        if (headerBiddingAd == null && ad.hasVideo()) {
            headerBiddingAd = obtainHeaderBiddingAd(ad.getVideo().getExtProtoList());
        }
        return headerBiddingAd;
    }

    @Nullable
    private HeaderBiddingAd obtainHeaderBiddingAd(@NonNull List<Any> extensions) {
        for (Any extension : extensions) {
            if (extension.is(HeaderBiddingAd.class)) {
                try {
                    return extension.unpack(HeaderBiddingAd.class);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    private static final class AdUnitPreloadTask<UnifiedAdRequestParamsType extends UnifiedAdRequestParams>
            implements Runnable, HeaderBiddingAdRequestParams, HeaderBiddingCollectParamsCallback {

        private static final Executor executor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() * 2);

        @NonNull
        private final ContextProvider contextProvider;
        @NonNull
        private final HeaderBiddingAdapter adapter;
        @NonNull
        private final AdsType adsType;
        @NonNull
        private final AdContentType adContentType;
        @NonNull
        private final UnifiedAdRequestParamsType adRequestParams;
        @NonNull
        private final Map<String, String> mediationConfig;

        private CountDownLatch syncLock;
        private HeaderBiddingPlacement.AdUnit adUnit;

        private boolean isFinished = false;

        private final TrackingObject trackingObject = new SimpleTrackingObject();

        AdUnitPreloadTask(@NonNull ContextProvider contextProvider,
                          @NonNull HeaderBiddingAdapter adapter,
                          @NonNull AdsType adsType,
                          @NonNull AdContentType adContentType,
                          @NonNull UnifiedAdRequestParamsType adRequestParams,
                          @NonNull Map<String, String> mediationConfig) {
            this.contextProvider = contextProvider;
            this.adapter = adapter;
            this.adsType = adsType;
            this.adContentType = adContentType;
            this.adRequestParams = adRequestParams;
            this.mediationConfig = mediationConfig;
        }

        @Override
        @NonNull
        public AdsType getAdsType() {
            return adsType;
        }

        @Override
        @NonNull
        public AdContentType getAdContentType() {
            return adContentType;
        }

        @Override
        public void run() {
            try {
                adapter.collectHeaderBiddingParams(contextProvider,
                                                   adRequestParams,
                                                   this,
                                                   this,
                                                   mediationConfig);
            } catch (Exception e) {
                Logger.log(e);
                onCollectFail(BMError.Internal);
            }
        }

        @Override
        public void onCollectFinished(@Nullable Map<String, String> params) {
            if (isFinished) {
                return;
            }
            HeaderBiddingPlacement.AdUnit.Builder builder = HeaderBiddingPlacement.AdUnit.newBuilder();
            builder.setBidder(adapter.getKey());
            builder.setBidderSdkver(adapter.getVersion());
            builder.putAllClientParams(mediationConfig);
            builder.putAllClientParams(params);
            adUnit = builder.build();
            Logger.log(String.format("%s: %s: Header bidding collect finished",
                                     adapter.getKey(),
                                     adsType));
            finish();
            BidMachineEvents.eventFinish(trackingObject,
                                         TrackEventType.HeaderBiddingNetworkPrepare,
                                         adsType,
                                         null);
        }

        @Override
        public void onCollectFail(@Nullable BMError error) {
            if (isFinished) {
                return;
            }
            if (error != null) {
                Logger.log(String.format("%s: Header bidding collect fail: %s",
                                         adapter.getKey(),
                                         error.getMessage()));
            }
            finish();
            BidMachineEvents.eventFinish(trackingObject,
                                         TrackEventType.HeaderBiddingNetworkPrepare,
                                         adsType,
                                         error);
        }

        void execute(@NonNull CountDownLatch syncLock) {
            BidMachineEvents.eventStart(trackingObject,
                                        TrackEventType.HeaderBiddingNetworkPrepare,
                                        new TrackEventInfo()
                                                .withParameter("HB_NETWORK", adapter.getKey())
                                                .withParameter("BM_AD_TYPE", adsType.getName()));
            this.syncLock = syncLock;
            executor.execute(this);
        }

        void cancel() {
            if (isFinished) {
                return;
            }
            Logger.log(String.format("%s: Header bidding collect fail: timeout", adapter.getKey()));
            finish();
        }

        HeaderBiddingPlacement.AdUnit getAdUnit() {
            return adUnit;
        }

        boolean isFinished() {
            return isFinished;
        }

        private void finish() {
            isFinished = true;
            syncLock.countDown();
        }

    }

}
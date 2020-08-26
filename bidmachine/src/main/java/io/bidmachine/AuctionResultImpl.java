package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.explorestack.protobuf.Any;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.models.AuctionResult;
import io.bidmachine.protobuf.AdExtension;
import io.bidmachine.protobuf.headerbidding.HeaderBiddingAd;

final class AuctionResultImpl implements AuctionResult {

    @NonNull
    private final String id;
    @Nullable
    private final String demandSource;
    private final double price;
    @Nullable
    private final String deal;
    @Nullable
    private final String seat;
    @NonNull
    private final String creativeId;
    @Nullable
    private final String cid;
    @Nullable
    private final String[] adDomains;
    @NonNull
    private final String networkKey;
    @NonNull
    private final Map<String, String> networkParams;
    @Nullable
    private final CreativeFormat creativeFormat;
    @NonNull
    private final Map<String, String> customParams;

    AuctionResultImpl(@NonNull AdsType adsType,
                      @NonNull Response.Seatbid seatbid,
                      @NonNull Response.Seatbid.Bid bid,
                      @NonNull Ad ad,
                      @NonNull NetworkConfig networkConfig) {
        id = bid.getId();
        demandSource = seatbid.getSeat();
        seat = seatbid.getSeat();
        price = bid.getPrice();
        deal = bid.getDeal();
        creativeId = ad.getId();
        cid = bid.getCid();
        if (ad.getAdomainCount() > 0) {
            adDomains = ad.getAdomainList().toArray(new String[0]);
        } else {
            adDomains = null;
        }
        customParams = createCustomParams(ad);
        networkKey = networkConfig.getKey();
        networkParams = createClientParams(adsType.obtainHeaderBiddingAd(ad));
        creativeFormat = identifyCreativeFormat(ad);
    }

    @NonNull
    @Override
    public String getId() {
        return id;
    }

    @Nullable
    @Override
    public String getDemandSource() {
        return demandSource;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Nullable
    @Override
    public String getDeal() {
        return deal;
    }

    @Override
    @Nullable
    public String getSeat() {
        return seat;
    }

    @Override
    @NonNull
    public String getCreativeId() {
        return creativeId;
    }

    @Nullable
    @Override
    public String getCid() {
        return cid;
    }

    @Override
    @Nullable
    public String[] getAdDomains() {
        return adDomains;
    }

    @Override
    @NonNull
    public String getNetworkKey() {
        return networkKey;
    }

    @NonNull
    @Override
    public Map<String, String> getNetworkParams() {
        return networkParams;
    }

    @Nullable
    @Override
    public CreativeFormat getCreativeFormat() {
        return creativeFormat;
    }

    @NonNull
    @Override
    public Map<String, String> getCustomParams() {
        return customParams;
    }

    @NonNull
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[@" + Integer.toHexString(hashCode()) + "]: "
                + "id=" + id + ", demandSource=" + demandSource + ", price: " + price
                + ", creativeId: " + creativeId + ", cid: " + cid;
    }

    @NonNull
    private Map<String, String> createCustomParams(@NonNull Ad ad) {
        Map<String, String> customParams = new HashMap<>();
        for (Any any : ad.getExtList()) {
            if (any.is(AdExtension.class)) {
                try {
                    AdExtension adExtension = any.unpack(AdExtension.class);
                    customParams.putAll(adExtension.getCustomParamsMap());
                } catch (Exception ignore) {
                }
            }
        }
        return customParams;
    }

    @NonNull
    @VisibleForTesting
    Map<String, String> createClientParams(@Nullable HeaderBiddingAd headerBiddingAd) {
        Map<String, String> clientParamsMap = new HashMap<>();
        if (headerBiddingAd != null) {
            Map<String, String> headerBiddingAdClientParamsMap = headerBiddingAd.getClientParamsMap();
            if (headerBiddingAdClientParamsMap != null) {
                clientParamsMap.putAll(headerBiddingAdClientParamsMap);
            }
        }
        return clientParamsMap;
    }

    @Nullable
    @VisibleForTesting
    static CreativeFormat identifyCreativeFormat(@NonNull Ad ad) {
        if (ad.hasDisplay()) {
            Ad.Display display = ad.getDisplay();
            if (display.hasBanner() || !TextUtils.isEmpty(display.getAdm())) {
                return CreativeFormat.Banner;
            } else if (display.hasNative()) {
                return CreativeFormat.Native;
            }
        } else if (ad.hasVideo()) {
            return CreativeFormat.Video;
        }
        return null;
    }

}
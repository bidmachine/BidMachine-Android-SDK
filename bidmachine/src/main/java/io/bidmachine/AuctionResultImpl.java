package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;

import io.bidmachine.models.AuctionResult;

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
    @Nullable
    private final CreativeFormat creativeFormat;

    AuctionResultImpl(@NonNull Response.Seatbid seatbid,
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
        networkKey = networkConfig.getKey();
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

    @Nullable
    @Override
    public CreativeFormat getCreativeFormat() {
        return creativeFormat;
    }

    @NonNull
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[@" + Integer.toHexString(hashCode()) + "]: "
                + "id=" + id + ", demandSource=" + demandSource + ", price: " + price
                + ", creativeId: " + creativeId + ", cid: " + cid;
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

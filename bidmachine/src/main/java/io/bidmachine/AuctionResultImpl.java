package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;
import io.bidmachine.models.AuctionResult;

final class AuctionResultImpl implements AuctionResult {

    @NonNull
    private final String id;
    @NonNull
    private final String networkName;
    @Nullable
    private final String demandSource;
    private final double price;
    @Nullable
    private final String seat;
    @NonNull
    private final String creativeId;
    @Nullable
    private final String cid;
    @Nullable
    private final String[] adDomains;

    AuctionResultImpl(@NonNull NetworkConfig networkConfig,
                      @NonNull Response.Seatbid seatbid,
                      @NonNull Response.Seatbid.Bid bid,
                      @NonNull Ad ad) {
        id = bid.getId();
        networkName = networkConfig.getKey();
        demandSource = seatbid.getSeat();
        seat = seatbid.getSeat();
        price = bid.getPrice();
        creativeId = ad.getId();
        cid = bid.getCid();
        if (ad.getAdomainCount() > 0) {
            adDomains = ad.getAdomainList().toArray(new String[0]);
        } else {
            adDomains = null;
        }
    }

    @NonNull
    @Override
    public String getId() {
        return id;
    }

    @NonNull
    @Override
    public String getNetworkName() {
        return networkName;
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

    @NonNull
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[@" + Integer.toHexString(hashCode()) + "]: "
                + "id=" + id + ", demandSource=" + demandSource + ", price: " + price
                + ", creativeId: " + creativeId + ", cid: " + cid;
    }

}

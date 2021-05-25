package io.bidmachine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.adcom.Placement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.bidmachine.models.AuctionResult;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.unified.UnifiedAdRequestParams;

public class TestAdRequest extends AdRequest<TestAdRequest, UnifiedAdRequestParams> {

    private final boolean isPlacementObjectValid;

    private TestAdRequest(final Builder builder) {
        super(builder.adsType);
        auctionResult = new AuctionResult() {
            @NonNull
            @Override
            public String getId() {
                return builder.auctionId;
            }

            @Nullable
            @Override
            public String getDemandSource() {
                return builder.auctionDemandSource;
            }

            @Override
            public double getPrice() {
                return builder.auctionPrice;
            }

            @Nullable
            @Override
            public String getDeal() {
                return builder.auctionDeal;
            }

            @Override
            public String getSeat() {
                return builder.auctionSeat;
            }

            @NonNull
            @Override
            public String getCreativeId() {
                return builder.auctionCreativeId;
            }

            @Nullable
            @Override
            public String getCid() {
                return builder.auctionCid;
            }

            @Nullable
            @Override
            public String[] getAdDomains() {
                return builder.auctionAdDomains;
            }

            @NonNull
            @Override
            public String getNetworkKey() {
                return builder.auctionNetworkName;
            }

            @NonNull
            @Override
            public Map<String, String> getNetworkParams() {
                return builder.auctionNetworkParams;
            }

            @Nullable
            @Override
            public CreativeFormat getCreativeFormat() {
                return builder.auctionCreativeFormat;
            }

            @NonNull
            @Override
            public Map<String, String> getCustomParams() {
                return builder.auctionCustomParams;
            }

        };
        adResult = Ad.newBuilder()
                .addAllBundle(builder.bundleList)
                .addAllAdomain(builder.adDomainList)
                .build();

        isPlacementObjectValid = builder.isPlacementObjectValid;
    }

    @NonNull
    public AuctionResult getAuctionResult() {
        assert auctionResult != null;
        return auctionResult;
    }

    @Override
    protected boolean isPlacementObjectValid(@NonNull Placement placement) throws Throwable {
        return isPlacementObjectValid;
    }

    @NonNull
    @Override
    protected UnifiedAdRequestParams createUnifiedAdRequestParams(@NonNull TargetingParams targetingParams,
                                                                  @NonNull DataRestrictions dataRestrictions) {
        return new BaseUnifiedAdRequestParams(targetingParams, dataRestrictions);
    }

    public static class Builder extends AdRequestBuilderImpl<Builder, TestAdRequest> {

        AdsType adsType;
        String auctionId;
        String auctionDemandSource;
        double auctionPrice;
        String auctionDeal;
        String auctionSeat;
        String auctionCreativeId;
        String auctionCid;
        String[] auctionAdDomains;
        String auctionNetworkName = "test_network";
        Map<String, String> auctionNetworkParams = new HashMap<>();
        CreativeFormat auctionCreativeFormat;
        Map<String, String> auctionCustomParams = new HashMap<>();

        List<String> bundleList = new ArrayList<>();
        List<String> adDomainList = new ArrayList<>();

        boolean isPlacementObjectValid;

        public Builder(@NonNull AdsType adsType) {
            this.adsType = adsType;
        }

        public Builder setAuctionId(String auctionId) {
            this.auctionId = auctionId;
            return this;
        }

        public Builder setAuctionDemandSource(String auctionDemandSource) {
            this.auctionDemandSource = auctionDemandSource;
            return this;
        }

        public Builder setAuctionPrice(double auctionPrice) {
            this.auctionPrice = auctionPrice;
            return this;
        }

        public Builder setAuctionDeal(String auctionDeal) {
            this.auctionDeal = auctionDeal;
            return this;
        }

        public Builder setAuctionSeat(String auctionSeat) {
            this.auctionSeat = auctionSeat;
            return this;
        }

        public Builder setAuctionCreativeId(String auctionCreativeId) {
            this.auctionCreativeId = auctionCreativeId;
            return this;
        }

        public Builder setAuctionCid(String auctionCid) {
            this.auctionCid = auctionCid;
            return this;
        }

        public Builder setAuctionAdDomains(String[] auctionAdDomains) {
            this.auctionAdDomains = auctionAdDomains;
            return this;
        }

        public Builder setAuctionNetworkName(String auctionNetworkName) {
            this.auctionNetworkName = auctionNetworkName;
            return this;
        }

        public Builder setAuctionNetworkParams(Map<String, String> auctionNetworkParams) {
            this.auctionNetworkParams = auctionNetworkParams;
            return this;
        }

        public Builder setAuctionCreativeFormat(CreativeFormat auctionCreativeFormat) {
            this.auctionCreativeFormat = auctionCreativeFormat;
            return this;
        }

        public Builder setAuctionCustomParams(Map<String, String> auctionCustomParams) {
            this.auctionCustomParams = auctionCustomParams;
            return this;
        }

        public Builder setBundleList(List<String> bundleList) {
            this.bundleList = bundleList;
            return this;
        }

        public Builder setAdDomainList(List<String> adDomainList) {
            this.adDomainList = adDomainList;
            return this;
        }

        public Builder setPlacementObjectValid(boolean isPlacementObjectValid) {
            this.isPlacementObjectValid = isPlacementObjectValid;
            return this;
        }

        @Override
        protected TestAdRequest createRequest() {
            return new TestAdRequest(this);
        }

    }

}
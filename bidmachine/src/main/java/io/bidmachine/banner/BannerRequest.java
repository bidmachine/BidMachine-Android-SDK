package io.bidmachine.banner;

import androidx.annotation.NonNull;

import com.explorestack.protobuf.adcom.Placement;

import io.bidmachine.AdRequest;
import io.bidmachine.AdsType;
import io.bidmachine.TargetingParams;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.IBannerRequestBuilder;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.utils.BMError;
import io.bidmachine.utils.ProtoUtils;

public final class BannerRequest extends AdRequest<BannerRequest, UnifiedBannerAdRequestParams> {

    private BannerSize bannerSize;

    private BannerRequest() {
        super(AdsType.Banner);
    }

    public BannerSize getSize() {
        return bannerSize;
    }

    @Override
    protected BMError verifyRequest() {
        if (bannerSize == null) {
            return BMError.paramError("BannerSize not provided");
        }
        return super.verifyRequest();
    }

    @Override
    protected boolean isPlacementObjectValid(@NonNull Placement placement) throws Throwable {
        return ProtoUtils.isBannerPlacement(placement, bannerSize);
    }

    @NonNull
    @Override
    protected UnifiedBannerAdRequestParams createUnifiedAdRequestParams(@NonNull TargetingParams targetingParams,
                                                                        @NonNull DataRestrictions dataRestrictions) {
        return new BannerUnifiedAdRequestParams(targetingParams, dataRestrictions);
    }

    public static final class Builder extends AdRequestBuilderImpl<Builder, BannerRequest>
            implements IBannerRequestBuilder<Builder> {

        @Override
        protected BannerRequest createRequest() {
            return new BannerRequest();
        }

        @Override
        public Builder setSize(BannerSize bannerSize) {
            prepareRequest();
            params.bannerSize = bannerSize;
            return this;
        }

    }

    public interface AdRequestListener extends AdRequest.AdRequestListener<BannerRequest> {
    }

    private class BannerUnifiedAdRequestParams extends BaseUnifiedAdRequestParams
            implements UnifiedBannerAdRequestParams {

        BannerUnifiedAdRequestParams(@NonNull TargetingParams targetingParams,
                                     @NonNull DataRestrictions dataRestrictions) {
            super(targetingParams, dataRestrictions);
        }

        @Override
        public BannerSize getBannerSize() {
            return BannerRequest.this.getSize();
        }
    }

}

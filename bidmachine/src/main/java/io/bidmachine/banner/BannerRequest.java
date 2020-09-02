package io.bidmachine.banner;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.bidmachine.AdRequest;
import io.bidmachine.AdsType;
import io.bidmachine.R;
import io.bidmachine.TargetingParams;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.IBannerRequestBuilder;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.utils.BMError;

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

    @NonNull
    @Override
    protected UnifiedBannerAdRequestParams createUnifiedAdRequestParams(@NonNull TargetingParams targetingParams,
                                                                        @NonNull DataRestrictions dataRestrictions) {
        return new BannerUnifiedAdRequestParams(targetingParams, dataRestrictions);
    }

    public static final class Builder extends AdRequestBuilderImpl<Builder, BannerRequest>
            implements IBannerRequestBuilder<Builder> {

        public Builder() {
        }

        Builder(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            TypedArray params = context.obtainStyledAttributes(attrs, R.styleable.BannerRequest, defStyleAttr, 0);
            setSize(BannerSize.values()[params.getInt(R.styleable.BannerRequest_bannerSize, 0)]);
            params.recycle();
        }

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

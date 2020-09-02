package io.bidmachine.banner;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.bidmachine.AdView;
import io.bidmachine.ViewAdObject;
import io.bidmachine.models.RequestBuilder;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;

public class BannerView extends AdView<
        BannerView,
        BannerAd,
        BannerRequest,
        ViewAdObject<BannerRequest, UnifiedBannerAd, UnifiedBannerAdRequestParams>,
        BannerListener> {

    public BannerView(@NonNull Context context) {
        super(context);
    }

    public BannerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BannerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected RequestBuilder<? extends RequestBuilder, BannerRequest> createAdRequest(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        return new BannerRequest.Builder(context, attrs, defStyleAttr);
    }

    @Override
    protected BannerAd createAd(Context context) {
        return new BannerAd(context);
    }

}
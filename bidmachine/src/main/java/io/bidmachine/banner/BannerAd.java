package io.bidmachine.banner;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import io.bidmachine.AdListener;
import io.bidmachine.AdProcessCallback;
import io.bidmachine.AdsType;
import io.bidmachine.ContextProvider;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.ViewAd;
import io.bidmachine.ViewAdObject;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
final class BannerAd extends ViewAd<
        BannerAd,
        BannerRequest,
        ViewAdObject<BannerRequest, UnifiedBannerAd, UnifiedBannerAdRequestParams>,
        UnifiedBannerAdRequestParams,
        AdListener<BannerAd>> {

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    BannerAd(@NonNull Context context) {
        super(context, AdsType.Banner);
    }

    @Override
    protected ViewAdObject<BannerRequest, UnifiedBannerAd, UnifiedBannerAdRequestParams> createAdObject(
            @NonNull ContextProvider contextProvider,
            @NonNull BannerRequest adRequest,
            @NonNull NetworkAdapter adapter,
            @NonNull AdObjectParams adObjectParams,
            @NonNull AdProcessCallback processCallback
    ) {
        UnifiedBannerAd unifiedAd = adapter.createBanner();
        if (unifiedAd == null) {
            return null;
        }
        ViewAdObject<BannerRequest, UnifiedBannerAd, UnifiedBannerAdRequestParams> adObject =
                new ViewAdObject<>(contextProvider, processCallback, adRequest, adObjectParams, unifiedAd);
        BannerSize bannerSize = adRequest.getSize();
        adObject.setWidth(bannerSize.width);
        adObject.setHeight(bannerSize.height);
        return adObject;
    }
}

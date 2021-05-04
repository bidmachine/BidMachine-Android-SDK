package io.bidmachine;

import androidx.annotation.NonNull;

public interface HeaderBiddingAdRequestParams {

    @NonNull
    AdsType getAdsType();

    @NonNull
    AdContentType getAdContentType();

}
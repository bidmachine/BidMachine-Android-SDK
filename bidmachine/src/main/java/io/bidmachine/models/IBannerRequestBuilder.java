package io.bidmachine.models;

import io.bidmachine.banner.BannerSize;

public interface IBannerRequestBuilder<SelfType> {

    @SuppressWarnings("UnusedReturnValue")
    SelfType setSize(BannerSize bannerSize);

}
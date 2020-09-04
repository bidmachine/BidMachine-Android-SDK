package io.bidmachine.models;

import io.bidmachine.AdRequest;
import io.bidmachine.PriceFloorParams;
import io.bidmachine.SessionAdParams;
import io.bidmachine.TargetingParams;

public interface RequestBuilder<SelfType extends RequestBuilder,
        ReturnType extends AdRequest> {

    @SuppressWarnings("UnusedReturnValue")
    SelfType setPriceFloorParams(PriceFloorParams priceFloorParams);

    @SuppressWarnings("UnusedReturnValue")
    SelfType setTargetingParams(TargetingParams targetingParams);

    @SuppressWarnings("UnusedReturnValue")
    SelfType setSessionAdParams(SessionAdParams sessionAdParams);

    @SuppressWarnings("UnusedReturnValue")
    SelfType setLoadingTimeOut(int timeOutMs);

    @SuppressWarnings("UnusedReturnValue")
    SelfType disableHeaderBidding();

    @SuppressWarnings("UnusedReturnValue")
    SelfType enableHeaderBidding();

//    SelfType setExtraParams(ExtraParams extraParams);

    @SuppressWarnings("UnusedReturnValue")
    SelfType setListener(AdRequest.AdRequestListener<ReturnType> listener);

    @SuppressWarnings("UnusedReturnValue")
    ReturnType build();

}
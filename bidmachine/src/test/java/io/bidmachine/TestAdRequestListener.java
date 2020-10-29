package io.bidmachine;

import androidx.annotation.NonNull;

import io.bidmachine.models.AuctionResult;
import io.bidmachine.utils.BMError;

class TestAdRequestListener implements AdRequest.AdRequestListener<TestAdRequest> {

    @Override
    public void onRequestSuccess(@NonNull TestAdRequest request,
                                 @NonNull AuctionResult auctionResult) {

    }

    @Override
    public void onRequestFailed(@NonNull TestAdRequest request, @NonNull BMError error) {

    }

    @Override
    public void onRequestExpired(@NonNull TestAdRequest request) {

    }

}
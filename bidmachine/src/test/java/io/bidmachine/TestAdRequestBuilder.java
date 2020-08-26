package io.bidmachine;

public class TestAdRequestBuilder extends AdRequest.AdRequestBuilderImpl<TestAdRequestBuilder, TestAdRequest> {

    @Override
    protected TestAdRequest createRequest() {
        return new TestAdRequest.Builder(AdsType.Interstitial).build();
    }

}
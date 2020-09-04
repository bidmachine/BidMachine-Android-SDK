package io.bidmachine.interstitial;

import androidx.annotation.NonNull;

import io.bidmachine.AdContentType;
import io.bidmachine.AdRequest;
import io.bidmachine.AdsType;
import io.bidmachine.FullScreenAdRequest;

public final class InterstitialRequest extends FullScreenAdRequest<InterstitialRequest> {

    private InterstitialRequest() {
        super(AdsType.Interstitial);
    }

    public static final class Builder extends FullScreenRequestBuilder<Builder, InterstitialRequest> {
        @Override
        protected InterstitialRequest createRequest() {
            return new InterstitialRequest();
        }

        @Override
        public Builder setAdContentType(@NonNull AdContentType adContentType) {
            return super.setAdContentType(adContentType);
        }
    }

    public interface AdRequestListener extends AdRequest.AdRequestListener<InterstitialRequest> {
    }

}

package io.bidmachine.interstitial;

import androidx.annotation.NonNull;

import com.explorestack.protobuf.adcom.Placement;

import io.bidmachine.AdContentType;
import io.bidmachine.AdRequest;
import io.bidmachine.AdsType;
import io.bidmachine.FullScreenAdRequest;
import io.bidmachine.utils.ProtoUtils;

public final class InterstitialRequest extends FullScreenAdRequest<InterstitialRequest> {

    private InterstitialRequest() {
        super(AdsType.Interstitial);
    }

    @Override
    protected boolean isPlacementObjectValid(@NonNull Placement placement) throws Throwable {
        return ProtoUtils.isInterstitialPlacement(placement);
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
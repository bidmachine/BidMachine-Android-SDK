package io.bidmachine;

import androidx.annotation.NonNull;

import io.bidmachine.displays.PlacementBuilder;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.RequestBuilder;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;

public abstract class FullScreenAdRequest<SelfType extends FullScreenAdRequest>
        extends AdRequest<SelfType, UnifiedFullscreenAdRequestParams> {

    AdContentType adContentType = AdContentType.All;

    protected FullScreenAdRequest(@NonNull AdsType adsType) {
        super(adsType);
    }

    @Override
    boolean isPlacementBuilderMatch(PlacementBuilder placementBuilder) {
        return (adContentType == AdContentType.All || adContentType == placementBuilder.getAdContentType())
                && super.isPlacementBuilderMatch(placementBuilder);
    }

    @NonNull
    @Override
    protected UnifiedFullscreenAdRequestParams createUnifiedAdRequestParams(@NonNull TargetingParams targetingParams,
                                                                            @NonNull DataRestrictions dataRestrictions) {
        return new FullscreenUnifiedAdRequestParams(targetingParams, dataRestrictions);
    }

    protected abstract static class FullScreenRequestBuilder<
            SelfType extends RequestBuilder,
            ReturnType extends FullScreenAdRequest>
            extends AdRequestBuilderImpl<SelfType, ReturnType> {

        @SuppressWarnings("unchecked")
        protected SelfType setAdContentType(@NonNull AdContentType adContentType) {
            prepareRequest();
            params.adContentType = adContentType;
            return (SelfType) this;
        }
    }

    private class FullscreenUnifiedAdRequestParams extends BaseUnifiedAdRequestParams
            implements UnifiedFullscreenAdRequestParams {

        FullscreenUnifiedAdRequestParams(@NonNull TargetingParams targetingParams,
                                         @NonNull DataRestrictions dataRestrictions) {
            super(targetingParams, dataRestrictions);
        }

        @Override
        public boolean isContentTypeMatch(@NonNull AdContentType adContentType) {
            return FullScreenAdRequest.this.adContentType == AdContentType.All
                    || FullScreenAdRequest.this.adContentType == adContentType;
        }
    }

}

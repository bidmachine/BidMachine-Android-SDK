package io.bidmachine.nativead;

import androidx.annotation.NonNull;

import com.explorestack.protobuf.adcom.Placement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.bidmachine.AdRequest;
import io.bidmachine.AdsType;
import io.bidmachine.MediaAssetType;
import io.bidmachine.TargetingParams;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.INativeRequestBuilder;
import io.bidmachine.unified.UnifiedNativeAdRequestParams;
import io.bidmachine.utils.ProtoUtils;

public final class NativeRequest extends AdRequest<NativeRequest, UnifiedNativeAdRequestParams> {

    private final List<MediaAssetType> mediaAssetTypes =
            new ArrayList<MediaAssetType>(MediaAssetType.values().length) {{
                add(MediaAssetType.Icon);
                add(MediaAssetType.Image);
            }};

    private NativeRequest() {
        super(AdsType.Native);
    }

    @SuppressWarnings("WeakerAccess")
    public boolean containsAssetType(MediaAssetType assetType) {
        return mediaAssetTypes.isEmpty()
                || mediaAssetTypes.contains(assetType)
                || mediaAssetTypes.contains(MediaAssetType.All);
    }

    @Override
    protected boolean isPlacementObjectValid(@NonNull Placement placement) throws Throwable {
        return ProtoUtils.isNativePlacement(placement);
    }

    @NonNull
    @Override
    protected UnifiedNativeAdRequestParams createUnifiedAdRequestParams(@NonNull TargetingParams targetingParams,
                                                                        @NonNull DataRestrictions dataRestrictions) {
        return new NativeUnifiedAdRequestParams(targetingParams, dataRestrictions);
    }

    public static final class Builder extends AdRequestBuilderImpl<Builder, NativeRequest>
            implements INativeRequestBuilder<Builder> {

        @Override
        protected NativeRequest createRequest() {
            return new NativeRequest();
        }

        @Override
        public Builder setMediaAssetTypes(@NonNull MediaAssetType... types) {
            prepareRequest();
            if (types.length > 0) {
                params.mediaAssetTypes.clear();
                params.mediaAssetTypes.addAll(Arrays.asList(types));
            }
            return this;
        }

    }

    public interface AdRequestListener extends AdRequest.AdRequestListener<NativeRequest> {

    }

    private class NativeUnifiedAdRequestParams extends BaseUnifiedAdRequestParams
            implements UnifiedNativeAdRequestParams {

        NativeUnifiedAdRequestParams(@NonNull TargetingParams targetingParams,
                                     @NonNull DataRestrictions dataRestrictions) {
            super(targetingParams, dataRestrictions);
        }

        @Override
        public boolean containsAssetType(MediaAssetType assetType) {
            return NativeRequest.this.containsAssetType(assetType);
        }

    }

}
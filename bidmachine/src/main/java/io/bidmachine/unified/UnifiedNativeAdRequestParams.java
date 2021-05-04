package io.bidmachine.unified;

import io.bidmachine.MediaAssetType;

public interface UnifiedNativeAdRequestParams extends UnifiedAdRequestParams {

    boolean containsAssetType(MediaAssetType assetType);

}
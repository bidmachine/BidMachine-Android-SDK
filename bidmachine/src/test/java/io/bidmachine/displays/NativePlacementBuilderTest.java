package io.bidmachine.displays;

import com.explorestack.protobuf.adcom.Placement;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.bidmachine.MediaAssetType;
import io.bidmachine.TargetingParams;
import io.bidmachine.TestUtils;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.nativead.NativeRequest;
import io.bidmachine.unified.UnifiedNativeAdRequestParams;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NativePlacementBuilderTest {

    @Test
    public void createIconAsset() {
        // Contains MediaAssetType.Icon - true
        NativeRequest nativeRequest = new NativeRequest.Builder()
                .setMediaAssetTypes(MediaAssetType.Icon)
                .build();
        UnifiedNativeAdRequestParams nativeAdRequestParams = TestUtils.createUnifiedNativeAdRequestParams(
                nativeRequest,
                new TargetingParams(),
                mock(DataRestrictions.class));
        Placement.DisplayPlacement.NativeFormat.AssetFormat assetFormat = NativePlacementBuilder
                .createIconAsset(nativeAdRequestParams);
        assertTrue(assetFormat.getReq());

        // Contains MediaAssetType.Icon - false
        nativeRequest = new NativeRequest.Builder()
                .setMediaAssetTypes(MediaAssetType.Image)
                .build();
        nativeAdRequestParams = TestUtils.createUnifiedNativeAdRequestParams(
                nativeRequest,
                new TargetingParams(),
                mock(DataRestrictions.class));
        assetFormat = NativePlacementBuilder
                .createIconAsset(nativeAdRequestParams);
        assertFalse(assetFormat.getReq());
    }

    @Test
    public void createImageAsset() {
        // Contains MediaAssetType.Image - true
        NativeRequest nativeRequest = new NativeRequest.Builder()
                .setMediaAssetTypes(MediaAssetType.Image)
                .build();
        UnifiedNativeAdRequestParams nativeAdRequestParams = TestUtils.createUnifiedNativeAdRequestParams(
                nativeRequest,
                new TargetingParams(),
                mock(DataRestrictions.class));
        Placement.DisplayPlacement.NativeFormat.AssetFormat assetFormat = NativePlacementBuilder
                .createImageAsset(nativeAdRequestParams);
        assertTrue(assetFormat.getReq());

        // Contains MediaAssetType.Image - false
        nativeRequest = new NativeRequest.Builder()
                .setMediaAssetTypes(MediaAssetType.Video)
                .build();
        nativeAdRequestParams = TestUtils.createUnifiedNativeAdRequestParams(
                nativeRequest,
                new TargetingParams(),
                mock(DataRestrictions.class));
        assetFormat = NativePlacementBuilder
                .createImageAsset(nativeAdRequestParams);
        assertFalse(assetFormat.getReq());
    }

    @Test
    public void createVideoAsset() {
        // Contains MediaAssetType.Video - true
        NativeRequest nativeRequest = new NativeRequest.Builder()
                .setMediaAssetTypes(MediaAssetType.Video)
                .build();
        UnifiedNativeAdRequestParams nativeAdRequestParams = TestUtils.createUnifiedNativeAdRequestParams(
                nativeRequest,
                new TargetingParams(),
                mock(DataRestrictions.class));
        Placement.DisplayPlacement.NativeFormat.AssetFormat assetFormat = NativePlacementBuilder
                .createVideoAsset(nativeAdRequestParams);
        assertTrue(assetFormat.getReq());

        // Contains MediaAssetType.Video - false
        nativeRequest = new NativeRequest.Builder()
                .setMediaAssetTypes(MediaAssetType.Icon)
                .build();
        nativeAdRequestParams = TestUtils.createUnifiedNativeAdRequestParams(
                nativeRequest,
                new TargetingParams(),
                mock(DataRestrictions.class));
        assetFormat = NativePlacementBuilder
                .createVideoAsset(nativeAdRequestParams);
        assertFalse(assetFormat.getReq());
    }

}
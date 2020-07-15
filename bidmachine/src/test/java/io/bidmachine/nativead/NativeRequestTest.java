package io.bidmachine.nativead;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.bidmachine.MediaAssetType;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NativeRequestTest {

    @Test
    public void containsAssetType_defaultMediaAssetTypes() {
        NativeRequest nativeRequest = new NativeRequest.Builder().build();
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Icon));
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Image));
        assertFalse(nativeRequest.containsAssetType(MediaAssetType.Video));
    }

    @Test
    public void setMediaAssetTypes_withoutMediaType() {
        NativeRequest nativeRequest = new NativeRequest.Builder()
                .build();
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Icon));
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Image));
        assertFalse(nativeRequest.containsAssetType(MediaAssetType.Video));
    }

    @Test
    public void setMediaAssetTypes_withEmptyMediaType() {
        NativeRequest nativeRequest = new NativeRequest.Builder()
                .setMediaAssetTypes()
                .build();
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Icon));
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Image));
        assertFalse(nativeRequest.containsAssetType(MediaAssetType.Video));
    }

    @Test
    public void setMediaAssetTypes_withIcon() {
        NativeRequest nativeRequest = new NativeRequest.Builder()
                .setMediaAssetTypes(MediaAssetType.Icon)
                .build();
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Icon));
        assertFalse(nativeRequest.containsAssetType(MediaAssetType.Image));
        assertFalse(nativeRequest.containsAssetType(MediaAssetType.Video));
    }

    @Test
    public void setMediaAssetTypes_withImage() {
        NativeRequest nativeRequest = new NativeRequest.Builder()
                .setMediaAssetTypes(MediaAssetType.Image)
                .build();
        assertFalse(nativeRequest.containsAssetType(MediaAssetType.Icon));
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Image));
        assertFalse(nativeRequest.containsAssetType(MediaAssetType.Video));
    }

    @Test
    public void setMediaAssetTypes_withVideo() {
        NativeRequest nativeRequest = new NativeRequest.Builder()
                .setMediaAssetTypes(MediaAssetType.Video)
                .build();
        assertFalse(nativeRequest.containsAssetType(MediaAssetType.Icon));
        assertFalse(nativeRequest.containsAssetType(MediaAssetType.Image));
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Video));
    }

    @Test
    public void setMediaAssetTypes_withIconImage() {
        NativeRequest nativeRequest = new NativeRequest.Builder()
                .setMediaAssetTypes(MediaAssetType.Icon, MediaAssetType.Image)
                .build();
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Icon));
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Image));
        assertFalse(nativeRequest.containsAssetType(MediaAssetType.Video));
    }

    @Test
    public void setMediaAssetTypes_withIconVideo() {
        NativeRequest nativeRequest = new NativeRequest.Builder()
                .setMediaAssetTypes(MediaAssetType.Icon, MediaAssetType.Video)
                .build();
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Icon));
        assertFalse(nativeRequest.containsAssetType(MediaAssetType.Image));
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Video));
    }

    @Test
    public void setMediaAssetTypes_withImageVideo() {
        NativeRequest nativeRequest = new NativeRequest.Builder()
                .setMediaAssetTypes(MediaAssetType.Image, MediaAssetType.Video)
                .build();
        assertFalse(nativeRequest.containsAssetType(MediaAssetType.Icon));
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Image));
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Video));
    }

    @Test
    public void setMediaAssetTypes_withIconImageVideo() {
        NativeRequest nativeRequest = new NativeRequest.Builder()
                .setMediaAssetTypes(MediaAssetType.Icon, MediaAssetType.Image, MediaAssetType.Video)
                .build();
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Icon));
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Image));
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Video));
    }

    @Test
    public void setMediaAssetTypes_withAll() {
        NativeRequest nativeRequest = new NativeRequest.Builder()
                .setMediaAssetTypes(MediaAssetType.All)
                .build();
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Icon));
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Image));
        assertTrue(nativeRequest.containsAssetType(MediaAssetType.Video));
    }

}
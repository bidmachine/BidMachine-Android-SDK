package io.bidmachine.ads.networks.nast;

import androidx.annotation.NonNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.MediaAssetType;
import io.bidmachine.unified.UnifiedAdCallback;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.unified.UnifiedNativeAdRequestParams;
import io.bidmachine.utils.IabUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NastParamsTest {

    @Test
    public void isValid_positive1() {
        UnifiedNativeAdRequestParams adRequestParams = mock(UnifiedNativeAdRequestParams.class);
        doReturn(true)
                .when(adRequestParams)
                .containsAssetType(MediaAssetType.Icon);
        Map<String, Object> params = new HashMap<>();
        params.put(IabUtils.KEY_TITLE, "test_title");
        params.put(IabUtils.KEY_CTA, "test_call_to_action");
        params.put(IabUtils.KEY_ICON_URL, "test_icon_url");
        NastParams nastParams = new NastParams(createParams(params));
        boolean valid = nastParams.isValid(adRequestParams, mock(UnifiedAdCallback.class));
        assertTrue(valid);
    }

    @Test
    public void isValid_positive2() {
        UnifiedNativeAdRequestParams adRequestParams = mock(UnifiedNativeAdRequestParams.class);
        doReturn(true)
                .when(adRequestParams)
                .containsAssetType(MediaAssetType.Image);
        Map<String, Object> params = new HashMap<>();
        params.put(IabUtils.KEY_TITLE, "test_title");
        params.put(IabUtils.KEY_CTA, "test_call_to_action");
        params.put(IabUtils.KEY_IMAGE_URL, "test_image_url");
        NastParams nastParams = new NastParams(createParams(params));
        boolean valid = nastParams.isValid(adRequestParams, mock(UnifiedAdCallback.class));
        assertTrue(valid);
    }

    @Test
    public void isValid_positive3() {
        UnifiedNativeAdRequestParams adRequestParams = mock(UnifiedNativeAdRequestParams.class);
        doReturn(true)
                .when(adRequestParams)
                .containsAssetType(MediaAssetType.Video);
        Map<String, Object> params = new HashMap<>();
        params.put(IabUtils.KEY_TITLE, "test_title");
        params.put(IabUtils.KEY_CTA, "test_call_to_action");
        params.put(IabUtils.KEY_VIDEO_URL, "test_video_url");
        NastParams nastParams = new NastParams(createParams(params));
        boolean valid = nastParams.isValid(adRequestParams, mock(UnifiedAdCallback.class));
        assertTrue(valid);
    }

    @Test
    public void isValid_positive4() {
        UnifiedNativeAdRequestParams adRequestParams = mock(UnifiedNativeAdRequestParams.class);
        doReturn(true)
                .when(adRequestParams)
                .containsAssetType(MediaAssetType.Video);
        Map<String, Object> params = new HashMap<>();
        params.put(IabUtils.KEY_TITLE, "test_title");
        params.put(IabUtils.KEY_CTA, "test_call_to_action");
        params.put(IabUtils.KEY_VIDEO_URL, "test_video_adm");
        NastParams nastParams = new NastParams(createParams(params));
        boolean valid = nastParams.isValid(adRequestParams, mock(UnifiedAdCallback.class));
        assertTrue(valid);
    }

    @Test
    public void isValid_negative1() {
        UnifiedNativeAdRequestParams adRequestParams = mock(UnifiedNativeAdRequestParams.class);
        Map<String, Object> params = new HashMap<>();
        NastParams nastParams = new NastParams(createParams(params));
        boolean valid = nastParams.isValid(adRequestParams, mock(UnifiedAdCallback.class));
        assertFalse(valid);
    }

    @Test
    public void isValid_negative2() {
        UnifiedNativeAdRequestParams adRequestParams = mock(UnifiedNativeAdRequestParams.class);
        Map<String, Object> params = new HashMap<>();
        params.put(IabUtils.KEY_TITLE, "test_title");
        NastParams nastParams = new NastParams(createParams(params));
        boolean valid = nastParams.isValid(adRequestParams, mock(UnifiedAdCallback.class));
        assertFalse(valid);
    }

    @Test
    public void isValid_negative3() {
        UnifiedNativeAdRequestParams adRequestParams = mock(UnifiedNativeAdRequestParams.class);
        doReturn(true)
                .when(adRequestParams)
                .containsAssetType(MediaAssetType.Icon);
        doReturn(true)
                .when(adRequestParams)
                .containsAssetType(MediaAssetType.Image);
        Map<String, Object> params = new HashMap<>();
        params.put(IabUtils.KEY_TITLE, "test_title");
        params.put(IabUtils.KEY_CTA, "test_call_to_action");
        NastParams nastParams = new NastParams(createParams(params));
        boolean valid = nastParams.isValid(adRequestParams, mock(UnifiedAdCallback.class));
        assertFalse(valid);
    }

    @Test
    public void isValid_negative4() {
        UnifiedNativeAdRequestParams adRequestParams = mock(UnifiedNativeAdRequestParams.class);
        doReturn(true)
                .when(adRequestParams)
                .containsAssetType(MediaAssetType.Icon);
        doReturn(true)
                .when(adRequestParams)
                .containsAssetType(MediaAssetType.Image);
        Map<String, Object> params = new HashMap<>();
        params.put(IabUtils.KEY_TITLE, "test_title");
        params.put(IabUtils.KEY_CTA, "test_call_to_action");
        params.put(IabUtils.KEY_ICON_URL, "test_icon_url");
        NastParams nastParams = new NastParams(createParams(params));
        boolean valid = nastParams.isValid(adRequestParams, mock(UnifiedAdCallback.class));
        assertFalse(valid);
    }

    @Test
    public void isValid_negative5() {
        UnifiedNativeAdRequestParams adRequestParams = mock(UnifiedNativeAdRequestParams.class);
        doReturn(true)
                .when(adRequestParams)
                .containsAssetType(MediaAssetType.Video);
        Map<String, Object> params = new HashMap<>();
        params.put(IabUtils.KEY_TITLE, "test_title");
        params.put(IabUtils.KEY_CTA, "test_call_to_action");
        NastParams nastParams = new NastParams(createParams(params));
        boolean valid = nastParams.isValid(adRequestParams, mock(UnifiedAdCallback.class));
        assertFalse(valid);
    }

    private UnifiedMediationParams createParams(final Map<String, Object> params) {
        UnifiedMediationParams.MappedUnifiedMediationParams.DataProvider dataProvider =
                new UnifiedMediationParams.MappedUnifiedMediationParams.DataProvider() {
                    @NonNull
                    @Override
                    public Map<String, Object> getData() {
                        return params;
                    }
                };
        return new UnifiedMediationParams.MappedUnifiedMediationParams(dataProvider);
    }

}
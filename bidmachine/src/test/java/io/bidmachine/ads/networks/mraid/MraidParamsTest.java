package io.bidmachine.ads.networks.mraid;

import com.explorestack.iab.utils.IabElementStyle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.bidmachine.TestUnifiedMediationParams;
import io.bidmachine.unified.UnifiedAdCallback;
import io.bidmachine.utils.IabUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MraidParamsTest {

    private TestUnifiedMediationParams unifiedAdRequestParams;

    @Before
    public void setUp() throws Exception {
        unifiedAdRequestParams = new TestUnifiedMediationParams();
    }

    @Test
    public void obtainParams() {
        IabElementStyle closeableViewStyle = new IabElementStyle();
        IabElementStyle countDownStyle = new IabElementStyle();
        IabElementStyle progressStyle = new IabElementStyle();

        unifiedAdRequestParams.put(IabUtils.KEY_CREATIVE_ADM, "test_creative");
        unifiedAdRequestParams.put(IabUtils.KEY_WIDTH, 320);
        unifiedAdRequestParams.put(IabUtils.KEY_HEIGHT, 50);
        unifiedAdRequestParams.put(IabUtils.KEY_PRELOAD, true);
        unifiedAdRequestParams.put(IabUtils.KEY_LOAD_SKIP_OFFSET, 10);
        unifiedAdRequestParams.put(IabUtils.KEY_SKIP_OFFSET, 15);
        unifiedAdRequestParams.put(IabUtils.KEY_COMPANION_SKIP_OFFSET, 20);
        unifiedAdRequestParams.put(IabUtils.KEY_USE_NATIVE_CLOSE, true);
        unifiedAdRequestParams.put(IabUtils.KEY_R1, true);
        unifiedAdRequestParams.put(IabUtils.KEY_R2, true);
        unifiedAdRequestParams.put(IabUtils.KEY_IGNORE_SAFE_AREA_LAYOUT_GUIDE, true);
        unifiedAdRequestParams.put(IabUtils.KEY_STORE_URL, "test_store_url");
        unifiedAdRequestParams.put(IabUtils.KEY_PROGRESS_DURATION, 25);
        unifiedAdRequestParams.put(IabUtils.KEY_CLOSABLE_VIEW_STYLE, closeableViewStyle);
        unifiedAdRequestParams.put(IabUtils.KEY_COUNTDOWN_STYLE, countDownStyle);
        unifiedAdRequestParams.put(IabUtils.KEY_PROGRESS_STYLE, progressStyle);

        MraidParams mraidParams = new MraidParams(unifiedAdRequestParams);

        assertEquals("test_creative", mraidParams.creativeAdm);
        assertEquals(320, mraidParams.width);
        assertEquals(50, mraidParams.height);
        assertTrue(mraidParams.canPreload);
        assertEquals(10, mraidParams.loadSkipOffset);
        assertEquals(15, mraidParams.skipOffset);
        assertEquals(20, mraidParams.companionSkipOffset);
        assertTrue(mraidParams.useNativeClose);
        assertTrue(mraidParams.r1);
        assertTrue(mraidParams.r2);
        assertTrue(mraidParams.ignoresSafeAreaLayoutGuide);
        assertEquals("test_store_url", mraidParams.storeUrl);
        assertEquals(25, mraidParams.progressDuration);
        assertEquals(closeableViewStyle, mraidParams.closeableViewStyle);
        assertEquals(countDownStyle, mraidParams.countDownStyle);
        assertEquals(progressStyle, mraidParams.progressStyle);
    }

    @Test
    public void obtainParams_preloadTrue_resultTrue() {
        unifiedAdRequestParams.put(IabUtils.KEY_PRELOAD, true);
        MraidParams mraidParams = new MraidParams(unifiedAdRequestParams);

        assertTrue(mraidParams.canPreload);
    }

    @Test
    public void obtainParams_useNativeCloseTrue_resultTrue() {
        unifiedAdRequestParams.put(IabUtils.KEY_USE_NATIVE_CLOSE, true);
        MraidParams mraidParams = new MraidParams(unifiedAdRequestParams);

        assertTrue(mraidParams.useNativeClose);
    }

    @Test
    public void obtainParams_r1True_resultTrue() {
        unifiedAdRequestParams.put(IabUtils.KEY_R1, true);
        MraidParams mraidParams = new MraidParams(unifiedAdRequestParams);

        assertTrue(mraidParams.r1);
    }

    @Test
    public void obtainParams_r2True_resultTrue() {
        unifiedAdRequestParams.put(IabUtils.KEY_R2, true);
        MraidParams mraidParams = new MraidParams(unifiedAdRequestParams);

        assertTrue(mraidParams.r2);
    }

    @Test
    public void obtainParams_ignoreSafeAreaLayoutGuideTrue_resultTrue() {
        unifiedAdRequestParams.put(IabUtils.KEY_IGNORE_SAFE_AREA_LAYOUT_GUIDE, true);
        MraidParams mraidParams = new MraidParams(unifiedAdRequestParams);

        assertTrue(mraidParams.ignoresSafeAreaLayoutGuide);
    }

    @Test
    public void isValid_emptyParams_returnFalse() {
        MraidParams mraidParams = new MraidParams(unifiedAdRequestParams);

        assertFalse(mraidParams.isValid(mock(UnifiedAdCallback.class)));
    }

    @Test
    public void isValid_wrongKey_returnFalse() {
        unifiedAdRequestParams.put("test_key", "test_value");
        MraidParams mraidParams = new MraidParams(unifiedAdRequestParams);

        assertFalse(mraidParams.isValid(mock(UnifiedAdCallback.class)));
    }

    @Test
    public void isValid_wrongCreativeType_returnFalse() {
        unifiedAdRequestParams.put(IabUtils.KEY_CREATIVE_ADM, 123);
        unifiedAdRequestParams.put(IabUtils.KEY_WIDTH, 320);
        unifiedAdRequestParams.put(IabUtils.KEY_HEIGHT, 50);
        MraidParams mraidParams = new MraidParams(unifiedAdRequestParams);

        assertFalse(mraidParams.isValid(mock(UnifiedAdCallback.class)));
    }

    @Test
    public void isValid_emptyCreative_returnFalse() {
        unifiedAdRequestParams.put(IabUtils.KEY_CREATIVE_ADM, "");
        unifiedAdRequestParams.put(IabUtils.KEY_WIDTH, 320);
        unifiedAdRequestParams.put(IabUtils.KEY_HEIGHT, 50);
        MraidParams mraidParams = new MraidParams(unifiedAdRequestParams);

        assertFalse(mraidParams.isValid(mock(UnifiedAdCallback.class)));
    }

    @Test
    public void isValid_wrongWidthType_returnFalse() {
        unifiedAdRequestParams.put(IabUtils.KEY_CREATIVE_ADM, "test_creative");
        unifiedAdRequestParams.put(IabUtils.KEY_WIDTH, "320");
        unifiedAdRequestParams.put(IabUtils.KEY_HEIGHT, 50);
        MraidParams mraidParams = new MraidParams(unifiedAdRequestParams);

        assertFalse(mraidParams.isValid(mock(UnifiedAdCallback.class)));
    }

    @Test
    public void isValid_wrongHeightContent_returnFalse() {
        unifiedAdRequestParams.put(IabUtils.KEY_CREATIVE_ADM, "test_creative");
        unifiedAdRequestParams.put(IabUtils.KEY_WIDTH, 320);
        unifiedAdRequestParams.put(IabUtils.KEY_HEIGHT, "50");
        MraidParams mraidParams = new MraidParams(unifiedAdRequestParams);

        assertFalse(mraidParams.isValid(mock(UnifiedAdCallback.class)));
    }

    @Test
    public void isValid_withoutWidth_returnFalse() {
        unifiedAdRequestParams.put(IabUtils.KEY_CREATIVE_ADM, "test_creative");
        unifiedAdRequestParams.put(IabUtils.KEY_WIDTH, 0);
        unifiedAdRequestParams.put(IabUtils.KEY_HEIGHT, 50);
        MraidParams mraidParams = new MraidParams(unifiedAdRequestParams);

        assertFalse(mraidParams.isValid(mock(UnifiedAdCallback.class)));
    }

    @Test
    public void isValid_withoutHeight_returnFalse() {
        unifiedAdRequestParams.put(IabUtils.KEY_CREATIVE_ADM, "test_creative");
        unifiedAdRequestParams.put(IabUtils.KEY_WIDTH, 320);
        unifiedAdRequestParams.put(IabUtils.KEY_HEIGHT, 0);
        MraidParams mraidParams = new MraidParams(unifiedAdRequestParams);

        assertFalse(mraidParams.isValid(mock(UnifiedAdCallback.class)));
    }

    @Test
    public void isValid_validParams_returnTrue() {
        unifiedAdRequestParams.put(IabUtils.KEY_CREATIVE_ADM, "test_creative");
        unifiedAdRequestParams.put(IabUtils.KEY_WIDTH, 320);
        unifiedAdRequestParams.put(IabUtils.KEY_HEIGHT, 50);
        MraidParams mraidParams = new MraidParams(unifiedAdRequestParams);

        assertTrue(mraidParams.isValid(mock(UnifiedAdCallback.class)));
    }

}
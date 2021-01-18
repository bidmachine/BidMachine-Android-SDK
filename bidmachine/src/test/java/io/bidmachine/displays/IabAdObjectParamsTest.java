package io.bidmachine.displays;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;

import com.explorestack.iab.utils.Assets;
import com.explorestack.iab.utils.IabElementStyle;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import io.bidmachine.protobuf.AdExtension;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.IabUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class IabAdObjectParamsTest {

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
    }

    @Test
    public void setCreativeId() {
        Ad ad = Ad.newBuilder()
                .setId("test_id")
                .build();
        IabAdObjectParams iabAdObjectParams = createIabAdObjectParams(ad);

        assertEquals("test_id", iabAdObjectParams.getData().get(IabUtils.KEY_CREATIVE_ID));
    }

    @Test
    public void setWidth() {
        Ad ad = Ad.newBuilder().build();
        IabAdObjectParams iabAdObjectParams = createIabAdObjectParams(ad);
        iabAdObjectParams.setWidth(320);

        assertEquals(320, iabAdObjectParams.getData().get(IabUtils.KEY_WIDTH));
    }

    @Test
    public void setHeight() {
        Ad ad = Ad.newBuilder().build();
        IabAdObjectParams iabAdObjectParams = createIabAdObjectParams(ad);
        iabAdObjectParams.setHeight(50);

        assertEquals(50, iabAdObjectParams.getData().get(IabUtils.KEY_HEIGHT));
    }

    @Test
    public void setCreativeAdm() {
        Ad ad = Ad.newBuilder().build();
        IabAdObjectParams iabAdObjectParams = createIabAdObjectParams(ad);
        iabAdObjectParams.setCreativeAdm("test_creative");

        assertEquals("test_creative", iabAdObjectParams.getData().get(IabUtils.KEY_CREATIVE_ADM));
    }

    @Test
    public void isValid_admIsNull_resultFalse() {
        Ad ad = Ad.newBuilder().build();
        IabAdObjectParams iabAdObjectParams = createIabAdObjectParams(ad);
        iabAdObjectParams.setCreativeAdm(null);

        assertFalse(iabAdObjectParams.isValid());
    }

    @Test
    public void isValid_admIsEmpty_resultFalse() {
        Ad ad = Ad.newBuilder().build();
        IabAdObjectParams iabAdObjectParams = createIabAdObjectParams(ad);
        iabAdObjectParams.setCreativeAdm("");

        assertFalse(iabAdObjectParams.isValid());
    }

    @Test
    public void isValid_admNotEmpty_resultFalse() {
        Ad ad = Ad.newBuilder().build();
        IabAdObjectParams iabAdObjectParams = createIabAdObjectParams(ad);
        iabAdObjectParams.setCreativeAdm("test_creative");

        assertTrue(iabAdObjectParams.isValid());
    }

    @Test
    public void prepareExtensions_adExtensionIsEmpty_resultDefault() {
        Response.Seatbid seatBid = Response.Seatbid.newBuilder().build();
        Response.Seatbid.Bid bid = Response.Seatbid.Bid.newBuilder().build();
        Ad ad = Ad.newBuilder().build();
        AdExtension adExtension = AdExtension.newBuilder().build();
        IabAdObjectParams iabAdObjectParams = new IabAdObjectParams(seatBid, bid, ad) {
        };
        iabAdObjectParams.prepareExtensions(seatBid, bid, adExtension);
        UnifiedMediationParams mediationParams = iabAdObjectParams.toMediationParams();

        assertFalse(mediationParams.getBool(IabUtils.KEY_PRELOAD));
        assertEquals(0, mediationParams.getInt(IabUtils.KEY_LOAD_SKIP_OFFSET));
        assertFalse(mediationParams.getBool(IabUtils.KEY_USE_NATIVE_CLOSE));
        assertEquals(0, mediationParams.getInt(IabUtils.KEY_SKIP_OFFSET));
        assertEquals(0, mediationParams.getInt(IabUtils.KEY_COMPANION_SKIP_OFFSET));
        assertFalse(mediationParams.getBool(IabUtils.KEY_R1));
        assertFalse(mediationParams.getBool(IabUtils.KEY_R2));
        assertFalse(mediationParams.getBool(IabUtils.KEY_IGNORE_SAFE_AREA_LAYOUT_GUIDE));
        assertEquals("", mediationParams.getString(IabUtils.KEY_STORE_URL));
        assertEquals(0, mediationParams.getInt(IabUtils.KEY_PROGRESS_DURATION));
        assertNotNull(mediationParams.getObject(IabUtils.KEY_CLOSABLE_VIEW_STYLE));
        assertNotNull(mediationParams.getObject(IabUtils.KEY_COUNTDOWN_STYLE));
        assertNotNull(mediationParams.getObject(IabUtils.KEY_PROGRESS_STYLE));
    }

    @Test
    public void prepareExtensions_adExtensionIsFilled_resultFilled() {
        Response.Seatbid seatBid = Response.Seatbid.newBuilder().build();
        Response.Seatbid.Bid bid = Response.Seatbid.Bid.newBuilder().build();
        Ad ad = Ad.newBuilder().build();

        AdExtension adExtension = AdExtension.newBuilder()
                .setPreload(true)
                .setLoadSkipoffset(1)
                .setUseNativeClose(true)
                .setSkipoffset(2)
                .setCompanionSkipoffset(3)
                .setR1(true)
                .setR2(true)
                .setIgnoresSafeAreaLayoutGuide(true)
                .setStoreUrl("test_store_url")
                .setProgressDuration(4)
                .setCloseButton(AdExtension.ControlAsset.newBuilder().build())
                .setCountdown(AdExtension.ControlAsset.newBuilder().build())
                .setProgress(AdExtension.ControlAsset.newBuilder().build())
                .build();
        IabAdObjectParams iabAdObjectParams = new IabAdObjectParams(seatBid, bid, ad) {
        };
        iabAdObjectParams.prepareExtensions(seatBid, bid, adExtension);
        UnifiedMediationParams mediationParams = iabAdObjectParams.toMediationParams();

        assertTrue(mediationParams.getBool(IabUtils.KEY_PRELOAD));
        assertEquals(1, mediationParams.getInt(IabUtils.KEY_LOAD_SKIP_OFFSET));
        assertTrue(mediationParams.getBool(IabUtils.KEY_USE_NATIVE_CLOSE));
        assertEquals(2, mediationParams.getInt(IabUtils.KEY_SKIP_OFFSET));
        assertEquals(3, mediationParams.getInt(IabUtils.KEY_COMPANION_SKIP_OFFSET));
        assertTrue(mediationParams.getBool(IabUtils.KEY_R1));
        assertTrue(mediationParams.getBool(IabUtils.KEY_R2));
        assertTrue(mediationParams.getBool(IabUtils.KEY_IGNORE_SAFE_AREA_LAYOUT_GUIDE));
        assertEquals("test_store_url", mediationParams.getString(IabUtils.KEY_STORE_URL));
        assertEquals(4, mediationParams.getInt(IabUtils.KEY_PROGRESS_DURATION));
        assertNotNull(mediationParams.getObject(IabUtils.KEY_CLOSABLE_VIEW_STYLE));
        assertNotNull(mediationParams.getObject(IabUtils.KEY_COUNTDOWN_STYLE));
        assertNotNull(mediationParams.getObject(IabUtils.KEY_PROGRESS_STYLE));
    }

    @Test
    public void createIabElementStyle_controlViewIsNull_resultNull() {
        Ad ad = Ad.newBuilder().build();
        IabAdObjectParams iabAdObjectParams = createIabAdObjectParams(ad);

        assertNull(iabAdObjectParams.transform(null));
    }

    @Test
    public void createIabElementStyle_controlViewIsEmpty_resultDefault() {
        Ad ad = Ad.newBuilder().build();
        IabAdObjectParams iabAdObjectParams = createIabAdObjectParams(ad);
        AdExtension.ControlAsset controlView = AdExtension.ControlAsset.newBuilder().build();
        IabElementStyle iabElementStyle = iabAdObjectParams.transform(controlView);

        assertNotNull(iabElementStyle);
        assertEquals(0, iabElementStyle.getMarginLeft(context).longValue());
        assertEquals(0, iabElementStyle.getMarginTop(context).longValue());
        assertEquals(0, iabElementStyle.getMarginRight(context).longValue());
        assertEquals(0, iabElementStyle.getMarginBottom(context).longValue());
        assertEquals(0, iabElementStyle.getPaddingLeft(context).longValue());
        assertEquals(0, iabElementStyle.getPaddingTop(context).longValue());
        assertEquals(0, iabElementStyle.getPaddingRight(context).longValue());
        assertEquals(0, iabElementStyle.getPaddingBottom(context).longValue());
        assertEquals("", iabElementStyle.getContent());
        assertEquals(Assets.backgroundColor, iabElementStyle.getFillColor().longValue());
        assertEquals(0, iabElementStyle.getFontStyle().longValue());
        assertEquals(0, iabElementStyle.getWidth(context).longValue());
        assertEquals(0, iabElementStyle.getHeight(context).longValue());
        assertNotNull(iabElementStyle.getHideAfter());
        assertEquals(0.0F, iabElementStyle.getHideAfter(), 0);
        assertEquals(Gravity.LEFT, iabElementStyle.getHorizontalPosition().longValue());
        assertEquals(Gravity.TOP, iabElementStyle.getVerticalPosition().longValue());
        assertEquals(0, iabElementStyle.getOpacity(), 0);
        assertFalse(iabElementStyle.isOutlined());
        assertEquals(Assets.mainAssetsColor, iabElementStyle.getStrokeColor().longValue());
        assertEquals(0.0F, iabElementStyle.getStrokeWidth(context), 0);
        assertEquals("", iabElementStyle.getStyle());
        assertFalse(iabElementStyle.isVisible());
    }

    @Test
    public void createIabElementStyle_controlViewIsFilled_resultFilled() {
        Ad ad = Ad.newBuilder().build();
        IabAdObjectParams iabAdObjectParams = createIabAdObjectParams(ad);
        AdExtension.ControlAsset controlView = AdExtension.ControlAsset.newBuilder()
                .setMargin("1 2 3 4")
                .setPadding("5 6 7 8")
                .setContent("test_content")
                .setFill("#000000")
                .setFontStyle(9)
                .setWidth(320)
                .setHeight(50)
                .setHideafter(10)
                .setX("right")
                .setY("bottom")
                .setOpacity(0.5F)
                .setOutlined(true)
                .setStroke("#FFFFFF")
                .setStrokeWidth(11)
                .setStyle("test_style")
                .setVisible(true)
                .build();
        IabElementStyle iabElementStyle = iabAdObjectParams.transform(controlView);

        assertNotNull(iabElementStyle);
        assertEquals(1, iabElementStyle.getMarginTop(context).longValue());
        assertEquals(2, iabElementStyle.getMarginRight(context).longValue());
        assertEquals(3, iabElementStyle.getMarginBottom(context).longValue());
        assertEquals(4, iabElementStyle.getMarginLeft(context).longValue());
        assertEquals(5, iabElementStyle.getPaddingTop(context).longValue());
        assertEquals(6, iabElementStyle.getPaddingRight(context).longValue());
        assertEquals(7, iabElementStyle.getPaddingBottom(context).longValue());
        assertEquals(8, iabElementStyle.getPaddingLeft(context).longValue());
        assertEquals("test_content", iabElementStyle.getContent());
        assertEquals(Color.BLACK, iabElementStyle.getFillColor().longValue());
        assertEquals(9, iabElementStyle.getFontStyle().longValue());
        assertEquals(320, iabElementStyle.getWidth(context).longValue());
        assertEquals(50, iabElementStyle.getHeight(context).longValue());
        assertNotNull(iabElementStyle.getHideAfter());
        assertEquals(10, iabElementStyle.getHideAfter(), 0);
        assertEquals(Gravity.RIGHT, iabElementStyle.getHorizontalPosition().longValue());
        assertEquals(Gravity.BOTTOM, iabElementStyle.getVerticalPosition().longValue());
        assertEquals(0.5F, iabElementStyle.getOpacity(), 0);
        assertTrue(iabElementStyle.isOutlined());
        assertEquals(Color.WHITE, iabElementStyle.getStrokeColor().longValue());
        assertEquals(11.0F, iabElementStyle.getStrokeWidth(context), 0);
        assertEquals("test_style", iabElementStyle.getStyle());
        assertTrue(iabElementStyle.isVisible());
    }

    @Test
    public void parseColor() {
        Ad ad = Ad.newBuilder().build();
        IabAdObjectParams iabAdObjectParams = createIabAdObjectParams(ad);

        assertNull(iabAdObjectParams.parseColor(null));
        assertNull(iabAdObjectParams.parseColor(""));
        assertNull(iabAdObjectParams.parseColor("test"));

        Integer result = iabAdObjectParams.parseColor("#000000");
        assertNotNull(result);
        assertEquals(Color.BLACK, result.longValue());

    }

    @Test
    public void parseHorizontalPosition() {
        Ad ad = Ad.newBuilder().build();
        IabAdObjectParams iabAdObjectParams = createIabAdObjectParams(ad);

        assertNull(iabAdObjectParams.parseHorizontalPosition(null));
        assertNull(iabAdObjectParams.parseHorizontalPosition(""));
        assertNull(iabAdObjectParams.parseHorizontalPosition("test"));

        Integer result = iabAdObjectParams.parseHorizontalPosition("left");
        assertNotNull(result);
        assertEquals(Gravity.LEFT, result.longValue());

        result = iabAdObjectParams.parseHorizontalPosition("right");
        assertNotNull(result);
        assertEquals(Gravity.RIGHT, result.longValue());

        result = iabAdObjectParams.parseHorizontalPosition("center");
        assertNotNull(result);
        assertEquals(Gravity.CENTER_HORIZONTAL, result.longValue());
    }

    @Test
    public void parseVerticalPosition() {
        Ad ad = Ad.newBuilder().build();
        IabAdObjectParams iabAdObjectParams = createIabAdObjectParams(ad);

        assertNull(iabAdObjectParams.parseVerticalPosition(null));
        assertNull(iabAdObjectParams.parseVerticalPosition(""));
        assertNull(iabAdObjectParams.parseVerticalPosition("test"));

        Integer result = iabAdObjectParams.parseVerticalPosition("top");
        assertNotNull(result);
        assertEquals(Gravity.TOP, result.longValue());

        result = iabAdObjectParams.parseVerticalPosition("bottom");
        assertNotNull(result);
        assertEquals(Gravity.BOTTOM, result.longValue());

        result = iabAdObjectParams.parseVerticalPosition("center");
        assertNotNull(result);
        assertEquals(Gravity.CENTER_VERTICAL, result.longValue());
    }


    private IabAdObjectParams createIabAdObjectParams(Ad ad) {
        Response.Seatbid seatBid = Response.Seatbid.newBuilder().build();
        Response.Seatbid.Bid bid = Response.Seatbid.Bid.newBuilder().build();
        return new IabAdObjectParams(seatBid, bid, ad) {
        };
    }

}
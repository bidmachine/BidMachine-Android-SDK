package io.bidmachine.nativead;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.HashSet;
import java.util.Set;

import io.bidmachine.AdProcessCallback;
import io.bidmachine.ContextProvider;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.nativead.view.NativeMediaView;
import io.bidmachine.unified.UnifiedNativeAd;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NativeAdObjectTest {

    private Context context;
    private NativeAdObject nativeAdObject;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;

        NativeRequest nativeRequest = new NativeRequest.Builder().build();
        nativeAdObject = new NativeAdObject(
                mock(ContextProvider.class),
                mock(AdProcessCallback.class),
                nativeRequest,
                mock(AdObjectParams.class),
                mock(UnifiedNativeAd.class));
    }

    @Test
    public void isNativeAdViewValid_positive1() {
        FrameLayout container = new FrameLayout(context);
        ImageView iconView = new ImageView(context);
        container.addView(iconView);
        boolean result = nativeAdObject.isNativeAdViewValid(
                container,
                iconView,
                null,
                null);
        assertTrue(result);
    }

    @Test
    public void isNativeAdViewValid_positive2() {
        FrameLayout container = new FrameLayout(context);
        NativeMediaView nativeMediaView = new NativeMediaView(context);
        container.addView(nativeMediaView);
        boolean result = nativeAdObject.isNativeAdViewValid(
                container,
                null,
                nativeMediaView,
                null);
        assertTrue(result);
    }

    @Test
    public void isNativeAdViewValid_positive3() {
        FrameLayout container = new FrameLayout(context);
        NativeMediaView nativeMediaView = new NativeMediaView(context);
        container.addView(nativeMediaView);

        Button callToAction = new Button(context);
        container.addView(callToAction);
        Set<View> clickableViews = new HashSet<>();
        clickableViews.add(callToAction);

        boolean result = nativeAdObject.isNativeAdViewValid(
                container,
                null,
                nativeMediaView,
                clickableViews);
        assertTrue(result);
    }

    @Test
    public void isNativeAdViewValid_negative1() {
        boolean result = nativeAdObject.isNativeAdViewValid(
                null,
                null,
                null,
                null);
        assertFalse(result);
    }

    @Test
    public void isNativeAdViewValid_negative2() {
        FrameLayout container = new FrameLayout(context);
        boolean result = nativeAdObject.isNativeAdViewValid(
                container,
                null,
                null,
                null);
        assertFalse(result);
    }

    @Test
    public void isNativeAdViewValid_negative3() {
        FrameLayout container = new FrameLayout(context);
        ImageView iconView = new ImageView(context);
        boolean result = nativeAdObject.isNativeAdViewValid(
                container,
                iconView,
                null,
                null);
        assertFalse(result);
    }

    @Test
    public void isNativeAdViewValid_negative4() {
        FrameLayout container = new FrameLayout(context);
        NativeMediaView nativeMediaView = new NativeMediaView(context);
        boolean result = nativeAdObject.isNativeAdViewValid(
                container,
                null,
                nativeMediaView,
                null);
        assertFalse(result);
    }

    @Test
    public void isNativeAdViewValid_negative5() {
        FrameLayout container = new FrameLayout(context);
        ImageView iconView = new ImageView(context);
        NativeMediaView nativeMediaView = new NativeMediaView(context);
        boolean result = nativeAdObject.isNativeAdViewValid(
                container,
                iconView,
                nativeMediaView,
                null);
        assertFalse(result);
    }

    @Test
    public void isNativeAdViewValid_negative6() {
        FrameLayout container = new FrameLayout(context);
        ImageView iconView = new ImageView(context);
        container.addView(iconView);
        NativeMediaView nativeMediaView = new NativeMediaView(context);
        boolean result = nativeAdObject.isNativeAdViewValid(
                container,
                iconView,
                nativeMediaView,
                null);
        assertFalse(result);
    }

    @Test
    public void isNativeAdViewValid_negative7() {
        FrameLayout container = new FrameLayout(context);
        NativeMediaView nativeMediaView = new NativeMediaView(context);
        container.addView(nativeMediaView);

        Button callToAction = new Button(context);
        Set<View> clickableViews = new HashSet<>();
        clickableViews.add(callToAction);

        boolean result = nativeAdObject.isNativeAdViewValid(
                container,
                null,
                nativeMediaView,
                clickableViews);
        assertFalse(result);
    }

}
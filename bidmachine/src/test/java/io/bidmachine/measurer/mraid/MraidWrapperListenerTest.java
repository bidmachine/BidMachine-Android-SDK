package io.bidmachine.measurer.mraid;

import android.content.Context;
import android.view.View;
import android.webkit.WebView;

import com.explorestack.iab.mraid.MRAIDView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import io.bidmachine.measurer.IABMeasurer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MraidWrapperListenerTest {

    private Context context;
    private MraidIABMeasurer measurer;
    private MraidWrapperListener wrapperListener;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        measurer = spy(new MraidIABMeasurer());
        wrapperListener = new MraidWrapperListener(measurer, null);
    }

    @Test
    public void onWebViewLoaded() {
        IABMeasurer.initialize(context, "TestSDK", "1.2.3", "test_script", null);

        View[] views = new View[]{};
        MRAIDView mraidView = mock(MRAIDView.class);
        WebView webView = new WebView(context);
        doReturn(views).when(mraidView).getNativeViews();
        wrapperListener.onWebViewLoaded(mraidView, webView);
        wrapperListener.onWebViewLoaded(mraidView, webView);

        verify(measurer).configure(any(Context.class), eq(webView));
        verify(measurer).registerAdView(webView);
        verify(measurer).addIgnoredViews(views);
        verify(measurer).startSession();
        verify(measurer).loaded();
    }

}
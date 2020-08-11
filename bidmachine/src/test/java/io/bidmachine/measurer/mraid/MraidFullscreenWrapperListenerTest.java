package io.bidmachine.measurer.mraid;

import android.webkit.WebView;

import com.explorestack.iab.mraid.MRAIDInterstitial;
import com.explorestack.iab.mraid.MRAIDInterstitialListener;
import com.explorestack.iab.mraid.MRAIDView;
import com.explorestack.iab.mraid.MRAIDViewLifecycleListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MraidFullscreenWrapperListenerTest {

    private MRAIDInterstitial mraidInterstitial;
    private MraidIABMeasurer measurer;
    private MRAIDInterstitialListener listener;
    private MRAIDViewLifecycleListener lifecycleListener;
    private MraidFullscreenWrapperListener wrapperListener;

    @Before
    public void setUp() throws Exception {
        mraidInterstitial = mock(MRAIDInterstitial.class);
        measurer = mock(MraidIABMeasurer.class);
        listener = mock(MRAIDInterstitialListener.class);
        lifecycleListener = mock(MRAIDViewLifecycleListener.class);
        wrapperListener = new MraidFullscreenWrapperListener(measurer, listener, lifecycleListener);
    }

    @Test
    public void onWebViewLoaded() {
        MRAIDView mraidView = mock(MRAIDView.class);
        WebView webView = mock(WebView.class);
        wrapperListener.onWebViewLoaded(mraidView, webView);
        verify(measurer).startSession();
        verify(measurer).loaded();
        verify(lifecycleListener).onWebViewLoaded(mraidView, webView);
    }

    @Test
    public void mraidInterstitialLoaded() {
        wrapperListener.mraidInterstitialLoaded(mraidInterstitial);
        verify(listener).mraidInterstitialLoaded(mraidInterstitial);
    }

    @Test
    public void mraidInterstitialShow() {
        wrapperListener.mraidInterstitialShow(mraidInterstitial);
        verify(measurer).shown();
        verify(listener).mraidInterstitialShow(mraidInterstitial);
    }

    @Test
    public void mraidInterstitialHide() {
        wrapperListener.mraidInterstitialHide(mraidInterstitial);
        verify(measurer).destroy();
        verify(listener).mraidInterstitialHide(mraidInterstitial);
    }

    @Test
    public void mraidInterstitialNoFill() {
        wrapperListener.mraidInterstitialNoFill(mraidInterstitial);
        verify(listener).mraidInterstitialNoFill(mraidInterstitial);
    }

}
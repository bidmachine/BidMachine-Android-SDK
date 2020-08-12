package io.bidmachine.measurer.vast;

import android.content.Context;

import com.explorestack.iab.vast.VastActivityListener;
import com.explorestack.iab.vast.VastClickCallback;
import com.explorestack.iab.vast.VastPlaybackListener;
import com.explorestack.iab.vast.VastRequest;
import com.explorestack.iab.vast.activity.VastActivity;
import com.explorestack.iab.vast.activity.VastView;
import com.explorestack.iab.vast.processor.VastAd;
import com.iab.omid.library.appodeal.adsession.VerificationScriptResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import io.bidmachine.measurer.BMIABMeasurer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class VastWrapperListenerTest {

    private VastActivity vastActivity;
    private VastRequest vastRequest;
    private VastIABMeasurer measurer;
    private VastActivityListener activityListener;
    private VastPlaybackListener playbackListener;
    private VastWrapperListener wrapperListener;

    @Before
    public void setUp() throws Exception {
        vastActivity = mock(VastActivity.class);
        vastRequest = mock(VastRequest.class);
        measurer = spy(new VastIABMeasurer());
        activityListener = mock(VastActivityListener.class);
        playbackListener = mock(VastPlaybackListener.class);
        wrapperListener = new VastWrapperListener(measurer, activityListener, playbackListener);

        Context context = RuntimeEnvironment.application;
        BMIABMeasurer.initialize(context,
                                 "test_partner_name",
                                 "test_partner_version",
                                 "test_measurer_js",
                                 null);
    }

    @Test
    public void started() {
        wrapperListener.started(256, 0.5F);
        verify(measurer).videoStarted(256, 0.5F);
        verify(playbackListener).started(256, 0.5F);

        wrapperListener.started(512, 0.6F);
        verify(measurer).videoStarted(512, 0.6F);
        verify(playbackListener).started(512, 0.6F);
    }

    @Test
    public void firstQuartile() {
        wrapperListener.firstQuartile();
        verify(measurer).videoFirstQuartile();
        verify(playbackListener).firstQuartile();
    }

    @Test
    public void midpoint() {
        wrapperListener.midpoint();
        verify(measurer).videoMidpoint();
        verify(playbackListener).midpoint();
    }

    @Test
    public void thirdQuartile() {
        wrapperListener.thirdQuartile();
        verify(measurer).videoThirdQuartile();
        verify(playbackListener).thirdQuartile();
    }

    @Test
    public void complete() {
        wrapperListener.complete();
        verify(measurer).videoCompleted();
        verify(playbackListener).complete();
    }

    @Test
    public void pause() {
        wrapperListener.pause();
        verify(measurer).videoPaused();
        verify(playbackListener).pause();
    }

    @Test
    public void resume() {
        wrapperListener.resume();
        verify(measurer).videoResumed();
        verify(playbackListener).resume();
    }

    @Test
    public void volumeChanged() {
        wrapperListener.volumeChanged(0.5F);
        verify(measurer).videoVolumeChanged(0.5F);
        verify(playbackListener).volumeChanged(0.5F);

        wrapperListener.volumeChanged(0.7F);
        verify(measurer).videoVolumeChanged(0.7F);
        verify(playbackListener).volumeChanged(0.7F);
    }

    @Test
    public void skipped() {
        wrapperListener.skipped();
        verify(measurer).videoSkipped();
        verify(playbackListener).skipped();
    }

    @Test
    public void onVastShown() {
        int skipTime = 10;
        VastView vastView = mock(VastView.class);
        VastAd vastAd = mock(VastAd.class);
        doReturn(vastView).when(vastActivity).getVastView();
        doReturn(vastAd).when(vastRequest).getVastAd();
        doReturn(skipTime).when(vastView).getSkipTime();

        assertFalse(measurer.isSessionStarted());
        wrapperListener.onVastShown(vastActivity, vastRequest);
        assertTrue(measurer.isSessionStarted());
        wrapperListener.onVastShown(vastActivity, vastRequest);

        verify(measurer).addVerificationScriptResources(anyListOf(VerificationScriptResource.class));
        verify(measurer).setAutoPlay(true);
        verify(measurer).setSkipOffset(skipTime);
        verify(measurer).configure(any(Context.class), eq(vastView));
        verify(measurer).registerAdView(vastView);
        verify(measurer).startSession();
        verify(measurer).loaded();
        verify(measurer).shown();
    }

    @Test
    public void onVastComplete() {
        wrapperListener.onVastComplete(vastActivity, vastRequest);
        verify(activityListener).onVastComplete(vastActivity, vastRequest);
    }

    @Test
    public void onVastClick() {
        VastClickCallback vastClickCallback = mock(VastClickCallback.class);
        String url = "http://test.com";

        wrapperListener.onVastClick(vastActivity, vastRequest, vastClickCallback, url);
        verify(measurer).clicked();
        verify(activityListener).onVastClick(vastActivity, vastRequest, vastClickCallback, url);
    }

    @Test
    public void onVastDismiss() {
        wrapperListener.onVastDismiss(vastActivity, vastRequest, true);
        verify(activityListener).onVastDismiss(vastActivity, vastRequest, true);

        wrapperListener.onVastDismiss(vastActivity, vastRequest, false);
        verify(activityListener).onVastDismiss(vastActivity, vastRequest, false);
        verify(measurer, times(2)).destroy();
    }

    @Test
    public void onVastError() {
        wrapperListener.onVastError(vastActivity, vastRequest, 100);
        verify(activityListener).onVastError(vastActivity, vastRequest, 100);

        wrapperListener.onVastError(vastActivity, vastRequest, 200);
        verify(activityListener).onVastError(vastActivity, vastRequest, 200);
    }

}
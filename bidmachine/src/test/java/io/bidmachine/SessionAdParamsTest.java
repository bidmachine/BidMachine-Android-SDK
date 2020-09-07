package io.bidmachine;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SessionAdParamsTest {

    @Test
    public void merge_bothParamsAreEmpty_returnNull() {
        SessionAdParams sessionAdParams = new SessionAdParams();
        SessionAdParams publisherSessionAdParams = new SessionAdParams();
        publisherSessionAdParams.merge(sessionAdParams);
        assertNull(publisherSessionAdParams.getSessionDuration());
        assertNull(publisherSessionAdParams.getImpressionCount());
        assertNull(publisherSessionAdParams.getClickRate());
        assertNull(publisherSessionAdParams.getUserClickedOnLastAd());
        assertNull(publisherSessionAdParams.getCompletionRate());
    }

    @Test
    public void merge_publisherNotSetParams_returnBidMachineParams() {
        SessionAdParams sessionAdParams = new SessionAdParams()
                .setSessionDuration(100)
                .setImpressionCount(200)
                .setClickRate(50.50F)
                .setIsUserClickedOnLastAd(true)
                .setCompletionRate(60.60F);
        SessionAdParams publisherSessionAdParams = new SessionAdParams();
        publisherSessionAdParams.merge(sessionAdParams);
        assertEquals(100, publisherSessionAdParams.getSessionDuration().intValue());
        assertEquals(200, publisherSessionAdParams.getImpressionCount().intValue());
        assertEquals(50.50F, publisherSessionAdParams.getClickRate(), 0);
        assertEquals(true, publisherSessionAdParams.getUserClickedOnLastAd());
        assertEquals(60.60F, publisherSessionAdParams.getCompletionRate(), 0);
    }

    @Test
    public void merge_presentBothParams_returnPublisherParams() {
        SessionAdParams sessionAdParams = new SessionAdParams()
                .setSessionDuration(100)
                .setImpressionCount(200)
                .setClickRate(50.50F)
                .setIsUserClickedOnLastAd(true)
                .setCompletionRate(60.60F);
        SessionAdParams publisherSessionAdParams = new SessionAdParams()
                .setSessionDuration(110)
                .setImpressionCount(220)
                .setClickRate(55.55F)
                .setIsUserClickedOnLastAd(false)
                .setCompletionRate(66.66F);
        publisherSessionAdParams.merge(sessionAdParams);
        assertEquals(110, publisherSessionAdParams.getSessionDuration().intValue());
        assertEquals(220, publisherSessionAdParams.getImpressionCount().intValue());
        assertEquals(55.55F, publisherSessionAdParams.getClickRate(), 0);
        assertEquals(false, publisherSessionAdParams.getUserClickedOnLastAd());
        assertEquals(66.66F, publisherSessionAdParams.getCompletionRate(), 0);
    }

}
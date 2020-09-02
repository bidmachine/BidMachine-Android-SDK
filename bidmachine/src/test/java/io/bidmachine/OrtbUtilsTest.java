package io.bidmachine;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class OrtbUtilsTest {

    @Test
    public void addEvent() {
        Map<TrackEventType, List<String>> trackUrls = new EnumMap<>(TrackEventType.class);
        OrtbUtils.addEvent(trackUrls, TrackEventType.MediationWin, null);
        OrtbUtils.addEvent(trackUrls, TrackEventType.MediationWin, "");
        OrtbUtils.addEvent(trackUrls, TrackEventType.MediationWin, "test_url_win");
        OrtbUtils.addEvent(trackUrls, TrackEventType.MediationLoss, "test_url_loss");

        assertEquals(2, trackUrls.size());
        List<String> urlList = trackUrls.get(TrackEventType.MediationWin);
        assertNotNull(urlList);
        assertEquals(1, urlList.size());
        assertEquals("test_url_win", urlList.get(0));
        urlList = trackUrls.get(TrackEventType.MediationLoss);
        assertNotNull(urlList);
        assertEquals(1, urlList.size());
        assertEquals("test_url_loss", urlList.get(0));
        urlList = trackUrls.get(TrackEventType.Load);
        assertNull(urlList);
    }

}
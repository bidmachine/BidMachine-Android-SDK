package io.bidmachine;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SimpleTrackingObjectTest {

    @Test
    public void getTrackingKey_autoGenerationTrackingKey_trackingKeyNotEmpty() {
        TrackingObject trackingObject = new SimpleTrackingObject();
        String resultTrackingKey = (String) trackingObject.getTrackingKey();

        assertNotNull(resultTrackingKey);
        assertTrue(resultTrackingKey.length() > 0);
    }

    @Test
    public void getTrackingKey_parameterTrackingKey_trackingKeyNotEmpty() {
        String trackingKey = "tracking_key";
        TrackingObject trackingObject = new SimpleTrackingObject(trackingKey);
        String resultTrackingKey = (String) trackingObject.getTrackingKey();

        assertNotNull(resultTrackingKey);
        assertEquals(trackingKey, resultTrackingKey);
    }

}
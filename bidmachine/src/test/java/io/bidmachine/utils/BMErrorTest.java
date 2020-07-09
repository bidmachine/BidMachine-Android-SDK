package io.bidmachine.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BMErrorTest {

    @Test
    public void trackErrorTest() {
        BMError bmError = BMError.AlreadyShown;
        assertTrue(bmError.isTrackError());
        bmError.setTrackError(true);
        assertTrue(bmError.isTrackError());
        bmError.setTrackError(false);
        assertFalse(bmError.isTrackError());
        bmError.setTrackError(true);
        assertTrue(bmError.isTrackError());
    }

}
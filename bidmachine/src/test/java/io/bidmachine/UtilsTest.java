package io.bidmachine;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class UtilsTest {

    @Test
    public void capitalize() {
        assertEquals("Test_string", Utils.capitalize("test_string"));
        assertEquals("Test_string", Utils.capitalize("Test_string"));
        assertEquals("Test_string", Utils.capitalize("tEST_STRING"));
    }

}
package io.bidmachine;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SessionManagerTest {

    @Before
    public void setUp() throws Exception {
        SessionManager.get().clear();
    }

    @Test
    public void pause_beforeResume_durationIsZero() throws Exception {
        SessionManager sessionManager = SessionManager.get();
        sessionManager.pause();
        assertEquals(0, sessionManager.getSessionDuration());
    }

    @Test
    public void resume() throws Exception {
        SessionManager sessionManager = SessionManager.get();
        sessionManager.resume();
        Thread.sleep(1000);
        sessionManager.pause();
        Thread.sleep(1000);
        sessionManager.resume();
        Thread.sleep(1000);

        assertEquals(2, sessionManager.getSessionDuration());
    }

}
package io.bidmachine;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SessionManagerTest {

    private SessionManager sessionManager;

    @Before
    public void setUp() throws Exception {
        sessionManager = SessionManager.get();
        sessionManager.startNewSession();
        sessionManager.setSessionResetAfter(0);
    }

    @Test
    public void pause_beforeResume_durationIsZero() throws Exception {
        sessionManager.pause();
        assertEquals(0, sessionManager.getSessionDuration());
    }

    @Test
    public void resume() throws Exception {
        sessionManager.resume();
        Thread.sleep(1000);
        sessionManager.pause();
        Thread.sleep(1000);
        sessionManager.resume();
        Thread.sleep(1000);

        assertEquals(2, sessionManager.getSessionDuration());
    }

    @Test
    public void startNewSession_resetAfterSecIsDefault_sessionIdSame() throws Exception {
        String oldSessionId = sessionManager.getSessionId();
        sessionManager.resume();
        sessionManager.pause();
        Thread.sleep(2000);
        sessionManager.resume();

        assertEquals(oldSessionId, sessionManager.getSessionId());
    }

    @Test
    public void startNewSession_resetAfterSecIs1SessionDurationMore1_sessionIdNew() throws Exception {
        String oldSessionId = sessionManager.getSessionId();
        sessionManager.setSessionResetAfter(1);
        sessionManager.resume();
        sessionManager.pause();
        Thread.sleep(2000);
        sessionManager.resume();

        assertNotEquals(oldSessionId, sessionManager.getSessionId());
    }

    @Test
    public void startNewSession_resetAfterSecIs5SessionDurationLess5_sessionIdSame() throws Exception {
        String oldSessionId = sessionManager.getSessionId();
        sessionManager.setSessionResetAfter(5);
        sessionManager.resume();
        sessionManager.pause();
        Thread.sleep(2000);
        sessionManager.resume();

        assertEquals(oldSessionId, sessionManager.getSessionId());
    }

    @Test
    public void startNewSession_resetAfterSecIs5SessionDurationMore5PauseBeforeResume_sessionIdSame() throws Exception {
        String oldSessionId = sessionManager.getSessionId();
        sessionManager.setSessionResetAfter(5);
        sessionManager.pause();
        Thread.sleep(10000);
        sessionManager.resume();
        assertEquals(oldSessionId, sessionManager.getSessionId());
    }

}
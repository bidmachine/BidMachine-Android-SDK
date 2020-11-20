package io.bidmachine;

import androidx.annotation.NonNull;

import java.util.UUID;

public class SessionManager {

    private static volatile SessionManager sessionManager;

    @NonNull
    public static SessionManager get() {
        if (sessionManager == null) {
            synchronized (SessionManager.class) {
                if (sessionManager == null) {
                    sessionManager = new SessionManager();
                }
            }
        }
        return sessionManager;
    }

    private String sessionId;
    private long sessionResetAfterSec;
    private long sessionDuration;
    private long pauseTime;
    private long resumeTime;

    public SessionManager() {
        startNewSession();
    }

    void startNewSession() {
        sessionId = UUID.randomUUID().toString();
        sessionDuration = 0;
        pauseTime = 0;
        resumeTime = 0;

        for (AdsType adsType : AdsType.values()) {
            BidMachineImpl.get().getSessionAdParams(adsType).clear();
        }
    }

    String getSessionId() {
        return sessionId;
    }

    public void setSessionResetAfter(long sessionResetAfterSec) {
        this.sessionResetAfterSec = sessionResetAfterSec;
    }

    public void resume() {
        resumeTime = System.currentTimeMillis();
        if (sessionResetAfterSec > 0
                && pauseTime > 0
                && resumeTime - pauseTime >= sessionResetAfterSec * 1000) {
            startNewSession();
        }
    }

    public void pause() {
        if (resumeTime == 0) {
            return;
        }
        pauseTime = System.currentTimeMillis();
        sessionDuration += pauseTime - resumeTime;
    }

    public int getSessionDuration() {
        if (resumeTime == 0) {
            return 0;
        }
        long afterResume = System.currentTimeMillis() - resumeTime;
        return (int) ((sessionDuration + afterResume) / 1000);
    }

}
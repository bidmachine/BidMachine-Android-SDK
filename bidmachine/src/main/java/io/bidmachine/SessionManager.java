package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

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

    private long sessionDuration;
    private long resumeTime;

    public synchronized void resume() {
        resumeTime = System.currentTimeMillis();
    }

    public synchronized void pause() {
        if (resumeTime == 0) {
            return;
        }
        sessionDuration += System.currentTimeMillis() - resumeTime;
    }

    public synchronized long getSessionDuration() {
        if (resumeTime == 0) {
            return 0;
        }
        long afterResume = System.currentTimeMillis() - resumeTime;
        return sessionDuration + afterResume / 1000;
    }

    @VisibleForTesting
    void clear() {
        sessionDuration = 0;
        resumeTime = 0;
    }

}
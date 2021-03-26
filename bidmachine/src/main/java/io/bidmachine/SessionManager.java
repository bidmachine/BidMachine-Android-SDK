package io.bidmachine;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import java.util.EnumMap;
import java.util.UUID;

public class SessionManager {

    private static volatile SessionManager instance;

    @NonNull
    public static SessionManager get() {
        SessionManager sessionManager = instance;
        if (sessionManager == null) {
            synchronized (SessionManager.class) {
                sessionManager = instance;
                if (sessionManager == null) {
                    sessionManager = new SessionManager();
                    instance = sessionManager;
                }
            }
        }
        return sessionManager;
    }

    private final EnumMap<AdsType, SessionAdParams> sessionAdParamsMap = new EnumMap<>(AdsType.class);

    private String sessionId;
    private long sessionResetAfterSec;
    private long sessionDuration;
    private long pauseTime;
    private long resumeTime;

    private SessionManager() {
        startNewSession();
    }

    @VisibleForTesting
    void startNewSession() {
        sessionId = UUID.randomUUID().toString();
        sessionDuration = 0;
        pauseTime = 0;
        resumeTime = 0;

        for (AdsType adsType : AdsType.values()) {
            getSessionAdParams(adsType).clear();
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

    @NonNull
    synchronized SessionAdParams getSessionAdParams(@NonNull AdsType adsType) {
        SessionAdParams sessionAdParams = sessionAdParamsMap.get(adsType);
        if (sessionAdParams == null) {
            sessionAdParams = new SessionAdParams();
            sessionAdParamsMap.put(adsType, sessionAdParams);
        }
        return sessionAdParams;
    }

}
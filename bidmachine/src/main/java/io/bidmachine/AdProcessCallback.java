package io.bidmachine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.bidmachine.utils.BMError;

public interface AdProcessCallback {

    void processLoadSuccess();

    void processLoadFail(@NonNull BMError error);

    void processShown();

    void processShowFail(@NonNull BMError error);

    void processClicked();

    void processImpression();

    void processFinished();

    void processClosed();

    void processExpired();

    void processDestroy();

    void trackEvent(@NonNull TrackEventType eventType, @Nullable BMError error);

    void log(String message);

}
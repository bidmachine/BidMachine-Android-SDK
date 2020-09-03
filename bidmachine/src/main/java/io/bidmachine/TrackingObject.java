package io.bidmachine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public abstract class TrackingObject {

    public abstract Object getTrackingKey();

    @Nullable
    List<String> getTrackingUrls(@NonNull TrackEventType eventType) {
        return null;
    }

}
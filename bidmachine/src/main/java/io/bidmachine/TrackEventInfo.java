package io.bidmachine;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class TrackEventInfo {

    final long startTimeMs;
    long finishTimeMs;

    private HashMap<String, Object> eventParameters;

    public TrackEventInfo() {
        startTimeMs = System.currentTimeMillis();
    }

    public TrackEventInfo withParameter(@NonNull String key, @NonNull Object value) {
        if (eventParameters == null) {
            eventParameters = new HashMap<>();
        }
        eventParameters.put(key, value);
        return this;
    }

    public Map<String, Object> getEventParameters() {
        return eventParameters;
    }

}
package io.bidmachine;

import androidx.annotation.NonNull;

public class TestTrackingObject extends TrackingObject {

    private final Object trackingKey;

    public TestTrackingObject(@NonNull Object trackingKey) {
        this.trackingKey = trackingKey;
    }

    @Override
    public Object getTrackingKey() {
        return trackingKey;
    }

}
package io.bidmachine;

import androidx.annotation.NonNull;

import java.util.UUID;

public class SimpleTrackingObject extends TrackingObject {

    private final Object trackingKey;

    public SimpleTrackingObject() {
        this(UUID.randomUUID().toString());
    }

    public SimpleTrackingObject(@NonNull Object trackingKey) {
        this.trackingKey = trackingKey;
    }

    @Override
    public Object getTrackingKey() {
        return trackingKey;
    }

}
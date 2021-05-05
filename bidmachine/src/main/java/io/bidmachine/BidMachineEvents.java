package io.bidmachine;

import androidx.annotation.Nullable;

import io.bidmachine.utils.BMError;

public class BidMachineEvents {

    public static void eventStart(@Nullable TrackingObject trackingObject,
                                  @Nullable TrackEventType trackEventType) {
        eventStart(trackingObject, trackEventType, null);
    }

    public static void eventStart(@Nullable TrackingObject trackingObject,
                                  @Nullable TrackEventType trackEventType,
                                  @Nullable TrackEventInfo trackEventInfo) {
        SessionTracker sessionTracker = BidMachineImpl.get().getSessionTracker();
        if (sessionTracker != null) {
            sessionTracker.trackEventStart(trackingObject, trackEventType, trackEventInfo);
        }
    }

    public static void eventFinish(@Nullable TrackingObject trackingObject,
                                   @Nullable TrackEventType trackEventType,
                                   @Nullable AdsType adsType,
                                   @Nullable BMError error) {
        SessionTracker sessionTracker = BidMachineImpl.get().getSessionTracker();
        if (sessionTracker != null) {
            sessionTracker.trackEventFinish(trackingObject, trackEventType, adsType, error);
        }
    }

    public static void clearEvent(@Nullable TrackingObject trackingObject,
                                  @Nullable TrackEventType trackEventType) {
        SessionTracker sessionTracker = BidMachineImpl.get().getSessionTracker();
        if (sessionTracker != null) {
            sessionTracker.clearTrackingEvent(trackingObject, trackEventType);
        }
    }

    public static void clear(@Nullable TrackingObject trackingObject) {
        SessionTracker sessionTracker = BidMachineImpl.get().getSessionTracker();
        if (sessionTracker != null) {
            sessionTracker.clearTrackers(trackingObject);
        }
    }

}
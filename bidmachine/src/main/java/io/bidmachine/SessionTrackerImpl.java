package io.bidmachine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.bidmachine.utils.BMError;

class SessionTrackerImpl extends SessionTracker {

    private static class EventsHolder {
        @Nullable
        private final EventsHolder referenceHolder;

        private final EnumMap<TrackEventType, AtomicInteger> trackingMap =
                new EnumMap<>(TrackEventType.class);


        EventsHolder(@Nullable EventsHolder referenceHolder) {
            this.referenceHolder = referenceHolder;
        }

        public void track(TrackEventType eventType) {
            if (referenceHolder != null) {
                referenceHolder.track(eventType);
            }
            if (trackingMap.get(eventType) == null) {
                trackingMap.put(eventType, new AtomicInteger(1));
            } else {
                trackingMap.get(eventType).incrementAndGet();
            }
        }

        public int getCount(TrackEventType eventType) {
            return trackingMap.containsKey(eventType) ? trackingMap.get(eventType).get() : 0;
        }
    }

    private final String sessionId = UUID.randomUUID().toString();

    @VisibleForTesting
    final Map<AdsType, EventsHolder> trackingMap = new ConcurrentHashMap<>();

    @VisibleForTesting
    final Map<Object, EnumMap<TrackEventType, TrackEventInfo>> intervalHolders = new ConcurrentHashMap<>();

    private final EventsHolder totalHolder = new EventsHolder(null);

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void trackEventStart(@Nullable TrackingObject trackingObject,
                                @Nullable TrackEventType trackEventType,
                                @Nullable TrackEventInfo trackEventInfo,
                                @Nullable AdsType adsType) {
        if (trackingObject == null || trackEventType == null) {
            return;
        }
        Object key = trackingObject.getTrackingKey();
        if (key == null) {
            return;
        }
        EnumMap<TrackEventType, TrackEventInfo> eventsMap = intervalHolders.get(key);
        if (eventsMap == null) {
            eventsMap = new EnumMap<>(TrackEventType.class);
            intervalHolders.put(key, eventsMap);
        }
        if (!eventsMap.containsKey(trackEventType)) {
            eventsMap.put(trackEventType, trackEventInfo != null ? trackEventInfo : new TrackEventInfo());
        }
    }

    @Override
    public void trackEventFinish(@Nullable TrackingObject trackingObject,
                                 @Nullable TrackEventType trackEventType,
                                 @Nullable AdsType adsType,
                                 @Nullable BMError error) {
        if (trackingObject == null || trackEventType == null) {
            return;
        }
        TrackEventInfo trackEventInfo = null;
        Object key = trackingObject.getTrackingKey();
        if (key == null) {
            return;
        }
        EnumMap<TrackEventType, TrackEventInfo> eventsMap = intervalHolders.get(key);
        if (eventsMap != null && eventsMap.containsKey(trackEventType)) {
            trackEventInfo = eventsMap.get(trackEventType);
            if (trackEventInfo != null) {
                trackEventInfo.finishTimeMs = System.currentTimeMillis();
            }
            eventsMap.remove(trackEventType);
            if (eventsMap.isEmpty()) {
                clearTrackers(trackingObject);
            }
        }
        notifyTrack(trackingObject, trackEventType, trackEventInfo, error);
        if (adsType != null && error == null) {
            obtainHolder(adsType).track(trackEventType);
        }
    }

    @Override
    void clearTrackingEvent(@Nullable TrackingObject trackingObject,
                            @Nullable TrackEventType trackEventType) {
        if (trackingObject == null || trackEventType == null) {
            return;
        }
        Object key = trackingObject.getTrackingKey();
        if (key == null) {
            return;
        }
        EnumMap<TrackEventType, TrackEventInfo> eventsMap = intervalHolders.get(key);
        if (eventsMap != null) {
            eventsMap.remove(trackEventType);
        }
    }

    @Override
    void clearTrackers(@Nullable TrackingObject trackingObject) {
        if (trackingObject != null) {
            intervalHolders.remove(trackingObject.getTrackingKey());
        }
    }

    @Override
    public int getEventCount(@NonNull AdsType adsType, @Nullable TrackEventType eventType) {
        return obtainHolder(adsType).getCount(eventType);
    }

    @Override
    public int getTotalEventCount(@Nullable TrackEventType eventType) {
        return totalHolder.getCount(eventType);
    }

    private EventsHolder obtainHolder(@NonNull AdsType adsType) {
        EventsHolder holder;
        if (!trackingMap.containsKey(adsType)) {
            holder = new EventsHolder(totalHolder);
            trackingMap.put(adsType, holder);
        } else {
            holder = trackingMap.get(adsType);
        }
        return holder;
    }
}

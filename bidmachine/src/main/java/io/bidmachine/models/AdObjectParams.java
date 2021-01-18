package io.bidmachine.models;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.explorestack.protobuf.InvalidProtocolBufferException;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.bidmachine.TrackEventType;
import io.bidmachine.Utils;
import io.bidmachine.protobuf.AdExtension;
import io.bidmachine.unified.UnifiedMediationParams;

public abstract class AdObjectParams {

    private static final long DEF_VIEWABILITY_TIME_THRESHOLD = 1;
    private static final float DEF_VIEWABILITY_PIXEL_THRESHOLD = 1;
    private static final boolean DEF_VIEWABILITY_IGNORE_WINDOW_FOCUS = false;

    private long viewabilityTimeThresholdSec = DEF_VIEWABILITY_TIME_THRESHOLD;
    private float viewabilityPixelThreshold = DEF_VIEWABILITY_PIXEL_THRESHOLD;
    private boolean viewabilityIgnoreWindowFocus = DEF_VIEWABILITY_IGNORE_WINDOW_FOCUS;

    private Map<TrackEventType, List<String>> trackUrls = new EnumMap<>(TrackEventType.class);

    public AdObjectParams(@NonNull Response.Seatbid seatbid,
                          @NonNull Response.Seatbid.Bid bid,
                          @NonNull Ad ad) {
        if (ad.getExtProtoCount() > 0) {
            for (int i = 0; i < ad.getExtProtoCount(); i++) {
                if (ad.getExtProto(i).is(AdExtension.class)) {
                    try {
                        AdExtension extension = ad.getExtProto(i).unpack(AdExtension.class);
                        prepareExtensions(seatbid, bid, extension);
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @CallSuper
    protected void prepareExtensions(@NonNull Response.Seatbid seatbid,
                                     @NonNull Response.Seatbid.Bid bid,
                                     @NonNull AdExtension extension) {
        viewabilityTimeThresholdSec = Utils.getOrDefault(extension.getViewabilityTimeThreshold(),
                AdExtension.getDefaultInstance().getViewabilityTimeThreshold(),
                DEF_VIEWABILITY_TIME_THRESHOLD);
        viewabilityPixelThreshold = Utils.getOrDefault(extension.getViewabilityPixelThreshold(),
                AdExtension.getDefaultInstance().getViewabilityPixelThreshold(),
                DEF_VIEWABILITY_PIXEL_THRESHOLD); //possibly should be multiplied by 100;
        viewabilityIgnoreWindowFocus = extension.getViewabilityIgnoreWindowFocus();
        prepareEvents(extension.getEventList());
    }

    protected void prepareEvents(@Nullable List<Ad.Event> events) {
        if (events == null || events.size() == 0) {
            return;
        }
        for (Ad.Event event : events) {
            TrackEventType eventType = TrackEventType.fromNumber(event.getTypeValue());
            if (eventType == null) continue;
            addEvent(eventType, event.getUrl());
        }
    }

    protected void addEvent(@NonNull TrackEventType eventType, String url) {
        List<String> urlList = trackUrls.get(eventType);
        if (urlList == null) {
            urlList = new ArrayList<>(1);
            trackUrls.put(eventType, urlList);
        }
        urlList.add(url);
    }

    @Nullable
    public List<String> getTrackUrls(@NonNull TrackEventType eventType) {
        return trackUrls.get(eventType);
    }

    public long getViewabilityTimeThresholdMs() {
        return TimeUnit.SECONDS.toMillis(viewabilityTimeThresholdSec);
    }

    public float getViewabilityPixelThreshold() {
        return viewabilityPixelThreshold;
    }

    public boolean isViewabilityIgnoreWindowFocus() {
        return viewabilityIgnoreWindowFocus;
    }

    @NonNull
    public abstract UnifiedMediationParams toMediationParams();

    public abstract boolean isValid();

}

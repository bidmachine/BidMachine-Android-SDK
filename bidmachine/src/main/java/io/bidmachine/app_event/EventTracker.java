package io.bidmachine.app_event;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.bidmachine.ApiRequest;
import io.bidmachine.core.NetworkRequest;

class EventTracker {

    private final String requestId;
    private final Map<String, String> eventParams = new HashMap<>();

    EventTracker() {
        requestId = UUID.randomUUID().toString();
    }

    void addParam(String key, String value) {
        eventParams.put(key, value);
    }

    void addParams(Map<String, String> params) {
        eventParams.putAll(params);
    }

    void send(@NonNull Event event) {
        send(event, null);
    }

    void send(@NonNull Event event, @Nullable Map<String, String> additionalParameters) {
        String url = fuseUrl(event, additionalParameters);
        new ApiRequest.Builder<>()
                .url(url)
                .setMethod(NetworkRequest.Method.Get)
                .request();
    }

    private String fuseUrl(@NonNull Event event, @Nullable Map<String, String> parameters) {
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority("event.bidmachine.io")
                .appendQueryParameter("integration_type", "GAM")
                .appendQueryParameter("session_id", BMAdManager.sessionId)
                .appendQueryParameter("request_id", requestId)
                .appendQueryParameter("event", event.name())
                .appendQueryParameter("time_stamp", String.valueOf(System.currentTimeMillis()))
                .appendQueryParameter("bm_platform", "android");
        appendQueryParameter(builder, eventParams);
        appendQueryParameter(builder, parameters);
        return builder.build().toString();
    }

    private void appendQueryParameter(@NonNull Uri.Builder builder,
                                      @Nullable Map<String, String> parameters) {
        if (parameters == null) {
            return;
        }
        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            String key = null;
            String value = null;
            if (parameter != null) {
                key = parameter.getKey();
                value = parameter.getValue();
            }
            if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                builder.appendQueryParameter(key, value);
            }
        }
    }

}
package io.bidmachine.app_event;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Map;
import java.util.UUID;

import io.bidmachine.ApiRequest;
import io.bidmachine.core.NetworkRequest;

public enum Event {

    BMRequestStart,
    BMRequestSuccess,
    GAMLoadStart,
    GAMLoaded,
    GAMFailToLoad,
    GAMAppEvent,
    GAMMetadata,
    BMLoadStart,
    BMLoaded,
    BMFailToLoad,
    BMIsLoaded,
    BMShow,
    BMShown,
    BMFailToShow,
    BMExpired;

    private static String requestId;

    static void newRequest() {
        requestId = UUID.randomUUID().toString();
    }

    void send(@Nullable Map<String, String> parameters) {
        String url = fuseUrl(parameters);
        new ApiRequest.Builder<>()
                .url(url)
                .setMethod(NetworkRequest.Method.Get)
                .request();
    }

    private String fuseUrl(@Nullable Map<String, String> parameters) {
        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority("event.bidmachine.io")
                .appendQueryParameter("integration_type", "GAM")
                .appendQueryParameter("session_id", BMAdManager.sessionId)
                .appendQueryParameter("request_id", requestId)
                .appendQueryParameter("event", name())
                .appendQueryParameter("time_stamp", String.valueOf(System.currentTimeMillis()))
                .appendQueryParameter("bm_platform", "android");
        if (parameters != null) {
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
        return builder.build().toString();
    }

}
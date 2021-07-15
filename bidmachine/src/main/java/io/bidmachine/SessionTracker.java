package io.bidmachine;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.bidmachine.core.Logger;
import io.bidmachine.core.NetworkRequest;
import io.bidmachine.utils.BMError;

abstract class SessionTracker {

    abstract void trackEventStart(@Nullable TrackingObject trackingObject,
                                  @Nullable TrackEventType trackEventType,
                                  @Nullable TrackEventInfo trackEventInfo);

    abstract void trackEventFinish(@Nullable TrackingObject trackingObject,
                                   @Nullable TrackEventType trackEventType,
                                   @Nullable AdsType adsType,
                                   @Nullable BMError error);

    abstract void clearTrackingEvent(@Nullable TrackingObject trackingObject,
                                     @Nullable TrackEventType trackEventType);

    abstract void clearTrackers(@Nullable TrackingObject trackingObject);

    abstract int getEventCount(@NonNull AdsType adsType, @Nullable TrackEventType trackEventType);

    abstract int getTotalEventCount(@Nullable TrackEventType eventType);

    static void notifyTrack(@NonNull TrackingObject trackingObject,
                            @NonNull TrackEventType eventType,
                            @Nullable TrackEventInfo eventInfo,
                            @Nullable BMError error) {
        if (error != null) {
            if (error.isTrackError()) {
                notifyError(collectTrackingUrls(trackingObject, TrackEventType.Error),
                            collectTrackingUrls(trackingObject, TrackEventType.TrackingError),
                            eventInfo,
                            eventType.getOrtbActionValue(),
                            error);
            }
        } else {
            notifyTrack(collectTrackingUrls(trackingObject, eventType),
                        collectTrackingUrls(trackingObject, TrackEventType.TrackingError),
                        eventInfo,
                        eventType);
        }
    }

    @Nullable
    private static List<String> collectTrackingUrls(@NonNull TrackingObject trackingObject,
                                                    @NonNull TrackEventType trackEventType) {
        List<String> outList = null;
        List<String> baseUrls = BidMachineImpl.get().getTrackingUrls(trackEventType);
        if (baseUrls != null) {
            if (outList == null) {
                outList = new ArrayList<>(baseUrls);
            } else {
                outList.addAll(baseUrls);
            }
        }
        List<String> trackingObjectUrls = trackingObject.getTrackingUrls(trackEventType);
        if (trackingObjectUrls != null) {
            if (outList == null) {
                outList = new ArrayList<>(trackingObjectUrls);
            } else {
                outList.addAll(trackingObjectUrls);
            }
        }
        return outList;
    }

    private static void notifyTrack(@Nullable List<String> urls,
                                    @Nullable final List<String> trackErrorUrls,
                                    @Nullable final TrackEventInfo eventInfo,
                                    @NonNull final TrackEventType eventType) {
        if (urls == null) {
            return;
        }
        Logger.log(String.format("dispatching event to server: %s", eventType));
        for (String url : urls) {
            executeNotify(replaceMacros(url, eventInfo, eventType.getOrtbActionValue(), -1),
                          new NetworkRequest.Callback<String, BMError>() {
                              @Override
                              public void onSuccess(@Nullable String result) {
                                  // ignore
                              }

                              @Override
                              public void onFail(@Nullable BMError result) {
                                  if (result == null) {
                                      result = BMError.internal("Failed to track URL");
                                  }
                                  notifyTrackingError(trackErrorUrls,
                                                      eventInfo,
                                                      eventType.getOrtbActionValue(),
                                                      result);
                              }
                          });
        }

    }

    private static void notifyError(@Nullable List<String> urls,
                                    @Nullable final List<String> trackErrorUrls,
                                    @Nullable final TrackEventInfo info,
                                    int processCode,
                                    @NonNull BMError error) {
        if (urls == null) {
            return;
        }
        if (error.getCode() == BMError.NOT_SET) {
            return;
        }
        Logger.log(String.format("dispatching error event to server: (%s-%s) - %s",
                                 processCode,
                                 error.getCode(),
                                 error.getMessage()));
        for (String url : urls) {
            executeNotify(replaceMacros(url, info, processCode, error.getCode()),
                          new NetworkRequest.Callback<String, BMError>() {
                              @Override
                              public void onSuccess(@Nullable String s) {
                                  // ignore
                              }

                              @Override
                              public void onFail(@Nullable BMError result) {
                                  if (result == null) {
                                      result = BMError.internal("Failed to track URL");
                                  }
                                  notifyTrackingError(trackErrorUrls,
                                                      info,
                                                      TrackEventType.Error.getOrtbActionValue(),
                                                      result);
                              }
                          });
        }
    }

    private static void notifyTrackingError(@Nullable List<String> urls,
                                            @Nullable TrackEventInfo eventInfo,
                                            int processCode,
                                            @NonNull BMError error) {
        if (urls == null) {
            return;
        }
        if (error.getCode() == BMError.NOT_SET) {
            return;
        }
        Logger.log(String.format("dispatching tracking fail to server: %s", error));
        for (String url : urls) {
            executeNotify(replaceMacros(url, eventInfo, processCode, error.getCode()), null);
        }
    }

    @Nullable
    @VisibleForTesting
    static String replaceMacros(@Nullable String url,
                                @Nullable TrackEventInfo info,
                                int processCode,
                                int errorCode) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        assert url != null;
        String outUrl = url;
        outUrl = replaceMacros(outUrl, "BM_EVENT_CODE", processCode);
        outUrl = replaceMacros(outUrl, "BM_ACTION_CODE", processCode);
        outUrl = replaceMacros(outUrl, "BM_ERROR_REASON", errorCode);
        if (info != null) {
            outUrl = replaceMacros(outUrl, "BM_ACTION_START", info.startTimeMs);
            outUrl = replaceMacros(outUrl, "BM_ACTION_FINISH", info.finishTimeMs);
            Map<String, Object> eventParameters = info.getEventParameters();
            if (eventParameters != null) {
                for (Map.Entry<String, Object> parameter : eventParameters.entrySet()) {
                    outUrl = replaceMacros(outUrl, parameter.getKey(), parameter.getValue());
                }
            }
        }
        return outUrl;
    }

    private static String replaceMacros(@NonNull String url,
                                        @NonNull String macros,
                                        @NonNull Object replace) {
        return url
                .replace("${" + macros + "}", String.valueOf(replace))
                .replace("%24%7B" + macros + "%7D", String.valueOf(replace));
    }

    private static void executeNotify(@Nullable String url,
                                      @Nullable NetworkRequest.Callback<String, BMError> callback) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        new ApiRequest.Builder<Object, String>()
                .url(url)
                .setMethod(NetworkRequest.Method.Get)
                .setDataBinder(new ApiRequest.ApiDataBinder<Object, String>() {
                    @Override
                    protected void prepareHeaders(NetworkRequest<Object, String, BMError> request,
                                                  URLConnection connection) {
                        connection.addRequestProperty("Accept", "application/json");
                        connection.addRequestProperty("Content-Type", "application/json");
                    }

                    @Nullable
                    @Override
                    protected byte[] obtainData(NetworkRequest<Object, String, BMError> request,
                                                URLConnection connection,
                                                @Nullable Object requestData) throws Exception {
                        return null;
                    }

                    @Override
                    protected String createSuccessResult(NetworkRequest<Object, String, BMError> request,
                                                         URLConnection connection,
                                                         byte[] resultData) throws Exception {
                        return null;
                    }
                })
                .setCallback(callback)
                .request();
    }

}
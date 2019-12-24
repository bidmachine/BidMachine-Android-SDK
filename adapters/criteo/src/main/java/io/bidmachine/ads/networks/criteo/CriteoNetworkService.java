package io.bidmachine.ads.networks.criteo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import io.bidmachine.BMLog;
import io.bidmachine.NetworkService;
import io.bidmachine.models.DeviceInfo;
import io.bidmachine.unified.UnifiedAdRequestParams;

public final class CriteoNetworkService extends NetworkService {

    private static final String TAG = "CriteoNetworkService";

    private static final String CRITEO_URL = "https://gum.criteo.com/appevent/v1/%s?gaid=%s&appId=%s&eventType=%s&limitedAdTracking=%d";
    private static final String EVENT_LAUNCH = "Launch";
    private static final String EVENT_ACTIVE = "Active";
    private static final String EVENT_INACTIVE = "Inactive";

    private static final Executor networkExecutor = Executors.newFixedThreadPool(2);

    @Nullable
    private String senderId;
    @Nullable
    private DeviceInfo deviceInfo;
    private volatile long nextValidRequestTime;

    @Override
    public String getName() {
        return "criteo";
    }

    @Override
    public void onCreated() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        Context applicationContext = context.getApplicationContext();
        if (applicationContext instanceof Application) {
            Application application = (Application) applicationContext;
            application.registerActivityLifecycleCallbacks(lifecycleCallbacks);
        }
    }

    @Override
    public void initialize(@Nullable String data, @NonNull UnifiedAdRequestParams adRequestParams) {
        super.initialize(data, adRequestParams);
        Context context = getContext();
        if (context != null && data != null) {
            senderId = data;
            if (senderId == null) {
                Log.e(TAG, "Initialize failed: sender_id not provided");
                return;
            }
            deviceInfo = adRequestParams.getDeviceInfo();
            sendRequest(getContext(), EVENT_LAUNCH);
        }
    }

    private boolean maySendRequest(@NonNull Context context, @NonNull DeviceInfo deviceInfo) {
        boolean mayByThrottle = nextValidRequestTime == 0
                || System.currentTimeMillis() > nextValidRequestTime;
        return !TextUtils.isEmpty(senderId)
                && !TextUtils.isEmpty(deviceInfo.getHttpAgent(context))
                && mayByThrottle;
    }

    private URL getUrl(@NonNull Context context,
                       @NonNull String eventType,
                       @NonNull DeviceInfo deviceInfo) throws Exception {
        String url = String.format(Locale.ENGLISH,
                                   CRITEO_URL,
                                   senderId,
                                   deviceInfo.getIfa(context),
                                   context.getPackageName(),
                                   eventType,
                                   deviceInfo.isLimitAdTrackingEnabled() ? 1 : 0);
        return new URL(url);
    }

    private void sendRequest(@NonNull final Context context, @NonNull final String eventType) {
        BMLog.log(TAG, String.format("Sending event: %s", eventType));
        if (deviceInfo == null || !maySendRequest(context, deviceInfo)) {
            BMLog.log(TAG, "Event sending consumed");
            return;
        }
        networkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                HttpsURLConnection urlConnection = null;
                InputStream inputStream = null;
                try {
                    urlConnection = (HttpsURLConnection) getUrl(context,
                                                                eventType,
                                                                deviceInfo).openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("User-Agent",
                                                     deviceInfo.getHttpAgent(context));
                    urlConnection.setConnectTimeout(10000);
                    urlConnection.setReadTimeout(10000);

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        long time = 0;
                        inputStream = urlConnection.getInputStream();
                        JSONObject response = getResponse(inputStream);
                        if (response != null && response.has("throttleSec")) {
                            int throttleSec = response.getInt("throttleSec");
                            time = System.currentTimeMillis() + throttleSec * 1000;
                        }
                        nextValidRequestTime = time;
                    } else if (responseCode == HttpsURLConnection.HTTP_BAD_REQUEST) {
                        inputStream = urlConnection.getErrorStream();
                        JSONObject response = getResponse(inputStream);
                        if (response != null && response.has("error")) {
                            BMLog.log(TAG,
                                      String.format(Locale.ENGLISH,
                                                    "Error: %s",
                                                    response.getString("error")));
                        }
                    }
                } catch (Exception e) {
                    BMLog.log(e);
                } finally {
                    try {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                    } catch (Exception ignore) {
                    }
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (Exception ignore) {
                    }
                }
            }
        });
    }

    private JSONObject getResponse(InputStream inputStream) {
        BufferedReader reader = null;
        try {
            StringBuilder builder = new StringBuilder(inputStream.available());
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            if (builder.length() > 0) {
                builder.setLength(builder.length() - 1);
            }
            return new JSONObject(builder.toString());
        } catch (Exception e) {
            BMLog.log(e);
            return null;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception ignore) {
            }
        }
    }

    private final Application.ActivityLifecycleCallbacks lifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            //ignore
        }

        @Override
        public void onActivityStarted(Activity activity) {
            //ignore
        }

        @Override
        public void onActivityResumed(Activity activity) {
            sendRequest(activity, EVENT_ACTIVE);
        }

        @Override
        public void onActivityPaused(Activity activity) {
            sendRequest(activity, EVENT_INACTIVE);
        }

        @Override
        public void onActivityStopped(Activity activity) {
            //ignore
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            //ignore
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            //ignore
        }
    };

}
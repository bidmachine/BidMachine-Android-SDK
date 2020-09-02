package io.bidmachine.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Pair;

import androidx.annotation.NonNull;

public class AdvertisingIdClientInfo implements Runnable {

    private static final int RESULT = 0;

    private Context context;
    private Handler handler;
    private Closure closure;

    public static void executeTask(Context context, Closure closure) {
        if (context == null || closure == null) {
            return;
        }
        BackgroundTaskManager.async(new AdvertisingIdClientInfo(context, closure));
    }

    public AdvertisingIdClientInfo(Context context, Closure closure) {
        this.context = context;
        this.closure = closure;

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                AdvertisingIdClientInfo.this.closure.executed((AdvertisingProfile) msg.obj);
            }
        };
    }

    @Override
    public void run() {
        handler.sendMessage(handler.obtainMessage(RESULT, getAdvertisingIdInfo(context)));
    }

    @NonNull
    private static AdvertisingProfile getAdvertisingIdInfo(Context context) {
        AdvertisingProfile advertisingProfile = new AdvertisingProfile();
        try {
            Class<?> advertisingIdClientClass = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient");
            Object advertisingIdInfoObject = Utils.invokeMethodByName(advertisingIdClientClass, advertisingIdClientClass, "getAdvertisingIdInfo", new Pair<Class, Object>(Context.class, context));
            if (advertisingIdInfoObject != null) {
                String id = (String) Utils.invokeMethodByName(advertisingIdInfoObject, "getId");
                boolean limitAdTrackingEnabled = (boolean) Utils.invokeMethodByName(advertisingIdInfoObject, "isLimitAdTrackingEnabled");

                advertisingProfile.setId(id);
                advertisingProfile.setLimitAdTrackingEnabled(limitAdTrackingEnabled);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return advertisingProfile;
    }

    public static class AdvertisingProfile {

        private String id;
        private boolean limitAdTrackingEnabled;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public boolean isLimitAdTrackingEnabled() {
            return limitAdTrackingEnabled;
        }

        void setLimitAdTrackingEnabled(boolean limitAdTrackingEnabled) {
            this.limitAdTrackingEnabled = limitAdTrackingEnabled;
        }

    }

    public interface Closure {
        void executed(@NonNull AdvertisingProfile adInfo);
    }

}
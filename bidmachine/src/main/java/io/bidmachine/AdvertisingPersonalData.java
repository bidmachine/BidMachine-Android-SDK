package io.bidmachine;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;

class AdvertisingPersonalData {

    private final static String ADVERTISING_CLIENT_CLASS = "com.google.android.gms.ads.identifier.AdvertisingIdClient";
    private final static String DEFAULT_ADVERTISING_ID = "00000000-0000-0000-0000-000000000000";

    private static String deviceAdvertisingId;
    private static boolean limitAdTrackingEnabled = false;

    /**
     * Updating current advertising information.
     * Only execute this method on a background thread.
     *
     * @param context Your application context.
     */
    static void updateInfo(@NonNull Context context) {
        try {
            Class<?> advertisingClientClass = Class.forName(ADVERTISING_CLIENT_CLASS);
            Object advertisingIdInfoObject =
                    Utils.invokeMethodByName(advertisingClientClass,
                                             advertisingClientClass,
                                             "getAdvertisingIdInfo",
                                             new Pair<Class<?>, Object>(Context.class, context));
            if (advertisingIdInfoObject != null) {
                deviceAdvertisingId = (String) Utils.invokeMethodByName(advertisingIdInfoObject,
                                                                        "getId");
                limitAdTrackingEnabled = (boolean) Utils.invokeMethodByName(advertisingIdInfoObject,
                                                                            "isLimitAdTrackingEnabled");
            }
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    static boolean isLimitAdTrackingEnabled() {
        return limitAdTrackingEnabled;
    }

    @NonNull
    static String getAdvertisingId(Context context, boolean blocked) {
        if (blocked) {
            return DEFAULT_ADVERTISING_ID;
        } else if (deviceAdvertisingId == null) {
            String uuid = Utils.getAdvertisingUUID(context);
            if (uuid != null) {
                return uuid;
            }
            return DEFAULT_ADVERTISING_ID;
        } else {
            return deviceAdvertisingId;
        }
    }

}
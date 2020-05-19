package io.bidmachine;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.util.concurrent.CountDownLatch;

import io.bidmachine.core.AdvertisingIdClientInfo;
import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;

class AdvertisingPersonalData {

    private static String deviceAdvertisingId;
    private static boolean deviceAdvertisingIdWasGenerated;
    private static boolean limitAdTrackingEnabled = false;

    @VisibleForTesting
    public final static String DEFAULT_ADVERTISING_ID = "00000000-0000-0000-0000-000000000000";

    static boolean isLimitAdTrackingEnabled() {
        return limitAdTrackingEnabled;
    }

    static void setLimitAdTrackingEnabled(boolean limitAdTrackingEnabled) {
        AdvertisingPersonalData.limitAdTrackingEnabled = limitAdTrackingEnabled;
    }

    static void setDeviceAdvertisingId(String deviceAdvertisingId) {
        AdvertisingPersonalData.deviceAdvertisingId = deviceAdvertisingId;
    }

    @NonNull
    static String getAdvertisingId(Context context, boolean blocked) {
        if (blocked) {
            return DEFAULT_ADVERTISING_ID;
        } else if (AdvertisingPersonalData.deviceAdvertisingId == null) {
            deviceAdvertisingIdWasGenerated = true;
            String uuid = Utils.getAdvertisingUUID(context);
            if (uuid != null) {
                return uuid;
            }
            return DEFAULT_ADVERTISING_ID;
        } else {
            deviceAdvertisingIdWasGenerated = false;
            return AdvertisingPersonalData.deviceAdvertisingId;
        }
    }

    public static boolean isDeviceAdvertisingIdWasGenerated() {
        return deviceAdvertisingIdWasGenerated;
    }

    static void syncUpdateInfo(Context context) {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            AdvertisingIdClientInfo.executeTask(context, new AdvertisingIdClientInfo.Closure() {
                @Override
                public void executed(@NonNull AdvertisingIdClientInfo.AdvertisingProfile advertisingProfile) {
                    setLimitAdTrackingEnabled(advertisingProfile.isLimitAdTrackingEnabled());
                    setDeviceAdvertisingId(advertisingProfile.getId());
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
        } catch (Exception e) {
            Logger.log(e);
            countDownLatch.countDown();
        }
    }

}
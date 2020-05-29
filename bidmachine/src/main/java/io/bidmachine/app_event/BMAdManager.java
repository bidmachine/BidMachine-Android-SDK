package io.bidmachine.app_event;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;

import java.util.UUID;

import io.bidmachine.BidMachine;

public class BMAdManager {

    static String sessionId = UUID.randomUUID().toString();
    static String appPackageName;
    static String appVersionName;

    public static void initialize(@NonNull Context context, @NonNull String sellerId) {
        try {
            appPackageName = context.getPackageName();
            appVersionName = context.getPackageManager()
                    .getPackageInfo(appPackageName, 0)
                    .versionName;
        } catch (Throwable ignore) {
        }

        BidMachine.initialize(context, sellerId);
    }

    static String decryptGAMErrorCode(int errorCode) {
        switch (errorCode) {
            case PublisherAdRequest.ERROR_CODE_INTERNAL_ERROR:
                return "INTERNAL_ERROR";
            case PublisherAdRequest.ERROR_CODE_INVALID_REQUEST:
                return "INVALID_REQUEST";
            case PublisherAdRequest.ERROR_CODE_NETWORK_ERROR:
                return "NETWORK_ERROR";
            case PublisherAdRequest.ERROR_CODE_NO_FILL:
                return "NO_FILL";
            default:
                return "UNKNOWN";
        }
    }

}
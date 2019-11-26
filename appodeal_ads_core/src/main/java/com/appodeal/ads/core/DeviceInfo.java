package com.appodeal.ads.core;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;

public class DeviceInfo {

    private static DeviceInfo cachedInfo;

    public static DeviceInfo obtain(Context context) {
        if (cachedInfo == null) {
            cachedInfo = new DeviceInfo(context);
        }
        return cachedInfo;
    }

    public final String osName;
    public final String osVersion;

    public final String model;
    public final String deviceModel;
    public final String manufacturer;

    @Nullable
    public final String httpAgent;

    public final int screenDpi;
    public final float screenDensity;
    public final boolean isTablet;

    private DeviceInfo(Context context) {
        osName = "android";
        osVersion = String.valueOf(Build.VERSION.SDK_INT);

        model = Build.MANUFACTURER != null && Build.MODEL != null
                ? String.format("%s %s", Build.MANUFACTURER, Build.MODEL)
                : Build.MANUFACTURER != null
                ? Build.MANUFACTURER
                : Build.MODEL;

        deviceModel = Build.MODEL;
        manufacturer = Build.MANUFACTURER;

        httpAgent = Utils.obtainHttpAgentString(context);

        screenDpi = Utils.getScreenDpi(context);
        screenDensity = Utils.getScreenDensity(context);
        isTablet = Utils.isTablet(context);
    }

}

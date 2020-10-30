package io.bidmachine;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.bidmachine.core.Logger;

public class DeviceInfo {

    private static volatile DeviceInfo instance;

    @NonNull
    public static DeviceInfo obtain(Context context) {
        DeviceInfo deviceInfo = instance;
        if (deviceInfo == null) {
            synchronized (DeviceInfo.class) {
                deviceInfo = instance;
                if (deviceInfo == null) {
                    deviceInfo = new DeviceInfo(context);
                    instance = deviceInfo;
                }
            }
        }
        return deviceInfo;
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
    public final String telephonyNetworkOperator;
    public String telephonyNetworkOperatorName;


    private Boolean isRooted;
    @Nullable
    private String hwv;
    @Nullable
    private Long totalRAMInB;
    @Nullable
    private Long totalDiskSpaceInMB;

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

        httpAgent = io.bidmachine.core.Utils.obtainHttpAgentString(context);

        screenDpi = io.bidmachine.core.Utils.getScreenDpi(context);
        screenDensity = io.bidmachine.core.Utils.getScreenDensity(context);
        isTablet = io.bidmachine.core.Utils.isTablet(context);

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            String networkOperator = telephonyManager.getNetworkOperator();
            if (networkOperator != null && networkOperator.length() >= 3) {
                telephonyNetworkOperator = networkOperator.substring(0, 3)
                        + '-'
                        + networkOperator.substring(3);
            } else {
                telephonyNetworkOperator = null;
            }
            try {
                telephonyNetworkOperatorName = telephonyManager.getNetworkOperatorName();
            } catch (Exception e) {
                Logger.log(e);
            }
        } else {
            telephonyNetworkOperator = null;
        }
    }

    boolean isDeviceRooted() {
        if (isRooted != null) {
            return isRooted;
        }
        try {
            String[] paths = {
                    "/sbin/su",
                    "/system/bin/su",
                    "/system/xbin/su",
                    "/data/local/xbin/su",
                    "/data/local/bin/su",
                    "/system/sd/xbin/su",
                    "/system/bin/failsafe/su",
                    "/data/local/su"
            };
            for (String path : paths) {
                if (new File(path).exists()) {
                    return isRooted = true;
                }
            }
        } catch (Exception ignore) {
        }

        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return isRooted = (in.readLine() != null);
        } catch (Exception ignore) {
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        return isRooted = false;
    }

    @Nullable
    String getHWV() {
        if (hwv != null) {
            return hwv;
        }
        String kernelVersion = getKernelVersionThroughProcVersion();
        if (!TextUtils.isEmpty(kernelVersion)) {
            return hwv = kernelVersion;
        }
        return hwv = getKernelVersionThroughUName();
    }

    @Nullable
    private String getKernelVersionThroughProcVersion() {
        RandomAccessFile reader = null;
        try {
            reader = new RandomAccessFile("/proc/version", "r");
            return reader.readLine();
        } catch (Exception e) {
            return null;
        } finally {
            io.bidmachine.core.Utils.close(reader);
        }
    }

    @Nullable
    private String getKernelVersionThroughUName() {
        Process process = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            process = Runtime.getRuntime().exec("uname -a");
            inputStreamReader = new InputStreamReader(process.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            return bufferedReader.readLine();
        } catch (Exception e) {
            return null;
        } finally {
            io.bidmachine.core.Utils.close(bufferedReader);
            io.bidmachine.core.Utils.close(inputStreamReader);
            if (process != null) {
                process.destroy();
            }
        }
    }

    @Nullable
    Long getTotalRAMInB() {
        if (totalRAMInB != null) {
            return totalRAMInB;
        }
        RandomAccessFile randomAccessFile = null;
        String load;
        try {
            randomAccessFile = new RandomAccessFile("/proc/meminfo", "r");
            load = randomAccessFile.readLine();

            Pattern p = Pattern.compile("(\\d+)");
            Matcher m = p.matcher(load);
            String value = "";
            while (m.find()) {
                value = m.group(1);
            }
            return totalRAMInB = Long.parseLong(value);
        } catch (Exception ignore) {
        } finally {
            io.bidmachine.core.Utils.close(randomAccessFile);
        }
        return null;
    }

    @Nullable
    Long getTotalDiskSpaceInMB() {
        if (totalDiskSpaceInMB != null) {
            return totalDiskSpaceInMB;
        }
        try {
            if (!io.bidmachine.core.Utils.isExternalMemoryAvailable()) {
                return null;
            }
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long totalBytes;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                totalBytes = statFs.getBlockSizeLong() * statFs.getBlockCountLong();
            } else {
                totalBytes = (long) statFs.getBlockSize() * (long) statFs.getBlockCount();
            }
            return totalDiskSpaceInMB = (totalBytes / 1048576L);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    Long getAvailableDiskSpaceInMB() {
        try {
            if (!io.bidmachine.core.Utils.isExternalMemoryAvailable()) {
                return null;
            }
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long availableBytes;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                availableBytes = statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();
            } else {
                availableBytes = (long) statFs.getBlockSize() * (long) statFs.getAvailableBlocks();
            }
            return availableBytes / 1048576L;
        } catch (Exception ignore) {
            return null;
        }
    }

}

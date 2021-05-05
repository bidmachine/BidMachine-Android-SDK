package io.bidmachine.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.bidmachine.core.Utils;

public class DeviceUtils {

    public static int getOrientation(@Nullable Context context) {
        return context != null
                ? context.getResources().getConfiguration().orientation
                : Configuration.ORIENTATION_UNDEFINED;
    }

    @NonNull
    public static Set<String> getInputLanguageSet(@NonNull Context context) {
        Set<String> languageSet = new HashSet<>();
        try {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager == null) {
                return languageSet;
            }
            List<InputMethodInfo> inputMethodInfoList = inputMethodManager.getEnabledInputMethodList();
            for (InputMethodInfo inputMethodInfo : inputMethodInfoList) {
                List<InputMethodSubtype> inputMethodSubtypeList =
                        inputMethodManager.getEnabledInputMethodSubtypeList(inputMethodInfo,
                                                                            true);
                for (InputMethodSubtype inputMethodSubtype : inputMethodSubtypeList) {
                    if (inputMethodSubtype.getMode().equals("keyboard")) {
                        String locale = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            locale = inputMethodSubtype.getLanguageTag();
                        }
                        if (TextUtils.isEmpty(locale)) {
                            locale = inputMethodSubtype.getLocale();
                        }
                        if (!TextUtils.isEmpty(locale)) {
                            assert locale != null;
                            int index = locale.indexOf("_");
                            if (index > 0) {
                                locale = locale.substring(0, index);
                            }
                            languageSet.add(locale);
                        }
                    }
                }
            }
            String defaultLanguage = Locale.getDefault().getLanguage();
            if (!TextUtils.isEmpty(defaultLanguage)) {
                languageSet.add(defaultLanguage);
            }
        } catch (Exception ignore) {
        }
        return languageSet;
    }

    @Nullable
    public static Boolean isRingMuted(@NonNull Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            return null;
        }
        int ringerMode = audioManager.getRingerMode();
        return ringerMode == AudioManager.RINGER_MODE_SILENT
                || ringerMode == AudioManager.RINGER_MODE_VIBRATE;
    }

    @Nullable
    public static Boolean isCharging(@NonNull Context context) {
        try {
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent intent = context.registerReceiver(null, intentFilter);
            if (intent != null) {
                int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                boolean result = plugged == BatteryManager.BATTERY_PLUGGED_AC
                        || plugged == BatteryManager.BATTERY_PLUGGED_USB;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    result = result || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
                }
                return result;
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    @Nullable
    public static Integer getBatteryLevel(@NonNull Context context) {
        float batteryPercent = Utils.getBatteryPercent(context);
        if (batteryPercent >= 85) {
            return 8;
        } else if (batteryPercent >= 70 && batteryPercent < 85) {
            return 7;
        } else if (batteryPercent >= 55 && batteryPercent < 70) {
            return 6;
        } else if (batteryPercent >= 40 && batteryPercent < 55) {
            return 5;
        } else if (batteryPercent >= 25 && batteryPercent < 40) {
            return 4;
        } else if (batteryPercent >= 10 && batteryPercent < 25) {
            return 3;
        } else if (batteryPercent >= 5 && batteryPercent < 10) {
            return 2;
        } else if (batteryPercent < 5) {
            return 1;
        }
        return null;
    }

    @Nullable
    public static Boolean isBatterySaverEnabled(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return powerManager != null ? powerManager.isPowerSaveMode() : null;
        }
        return null;
    }

    public static boolean isDarkModeEnabled(@NonNull Context context) {
        int uiMode = context.getResources().getConfiguration().uiMode;
        return (uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    @Nullable
    public static Boolean isAirplaneModeOn(@NonNull Context context) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            int value;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                value = Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON);
            } else {
                value = Settings.System.getInt(contentResolver, Settings.System.AIRPLANE_MODE_ON);
            }
            return value != 0;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static Boolean isDoNotDisturbOn(@NonNull Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return Settings.Global.getInt(context.getContentResolver(), "zen_mode") != 0;
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    @Nullable
    public static String getDeviceName(@NonNull Context context) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            String deviceName;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                deviceName = Settings.Global.getString(contentResolver, "device_name");
            } else {
                deviceName = Settings.System.getString(contentResolver, "device_name");
            }
            if (!TextUtils.isEmpty(deviceName)) {
                return deviceName;
            }
            deviceName = Settings.Secure.getString(contentResolver, "bluetooth_name");
            if (!TextUtils.isEmpty(deviceName)) {
                return deviceName;
            }
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null) {
                return bluetoothAdapter.getName();
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    @Nullable
    public static Integer getScreenBrightness(@NonNull Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(),
                                          Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static Float getScreenBrightnessRatio(@NonNull Context context) {
        Integer brightness = getScreenBrightness(context);
        if (brightness == null) {
            return null;
        }
        float value = brightness / 255F;
        value = Math.round(value * 100F) / 100F;
        return value;
    }

}
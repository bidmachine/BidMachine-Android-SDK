package io.bidmachine;

import android.content.Context;
import android.content.res.Configuration;

public class Utils {

    public static int getOrDefault(int target, int targetDefault, int def) {
        return target == targetDefault ? def : target;
    }

    public static long getOrDefault(long target, long targetDefault, long def) {
        return target == targetDefault ? def : target;
    }

    public static float getOrDefault(float target, float targetDefault, float def) {
        return target == targetDefault ? def : target;
    }

    public static String capitalize(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    public static int getOrientation() {
        Context context = BidMachineImpl.get().getAppContext();
        if (context == null) {
            return Configuration.ORIENTATION_UNDEFINED;
        }
        return context.getResources().getConfiguration().orientation;
    }

}

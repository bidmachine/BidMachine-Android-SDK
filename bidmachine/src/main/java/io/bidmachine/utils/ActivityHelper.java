package io.bidmachine.utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;

import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.Map;

public class ActivityHelper {

    @Nullable
    public static Activity getTopActivity() {
        Activity outActivity = null;
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            if (activityThread == null) {
                return null;
            }

            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Map activities = (Map) activitiesField.get(activityThread);
            if (activities == null || activities.isEmpty()) {
                return null;
            }
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field activityInfoField = activityRecordClass.getDeclaredField("activityInfo");
                activityInfoField.setAccessible(true);
                ActivityInfo activityInfo = (ActivityInfo) activityInfoField.get(activityRecord);
                if (activityInfo == null) {
                    continue;
                }

                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);

                Field activityField = activityRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    return (Activity) activityField.get(activityRecord);
                } else if (outActivity == null) {
                    outActivity = (Activity) activityField.get(activityRecord);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outActivity;
    }

}
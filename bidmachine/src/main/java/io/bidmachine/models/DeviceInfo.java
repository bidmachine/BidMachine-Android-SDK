package io.bidmachine.models;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface DeviceInfo {

    @Nullable
    String getHttpAgent(@NonNull Context context);

    @Nullable
    String getIfa(@NonNull Context context);

    boolean isLimitAdTrackingEnabled();

}
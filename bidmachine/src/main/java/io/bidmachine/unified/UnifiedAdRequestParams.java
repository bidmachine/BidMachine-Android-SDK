package io.bidmachine.unified;

import android.support.annotation.Nullable;

import io.bidmachine.AdRequest;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.DeviceInfo;
import io.bidmachine.models.TargetingInfo;

public interface UnifiedAdRequestParams {

    @Nullable
    AdRequest getAdRequest();

    DataRestrictions getDataRestrictions();

    TargetingInfo getTargetingParams();

    DeviceInfo getDeviceInfo();

    boolean isTestMode();

}

package io.bidmachine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.DeviceInfo;
import io.bidmachine.models.TargetingInfo;
import io.bidmachine.unified.UnifiedAdRequestParams;

class UnifiedAdRequestParamsImpl implements UnifiedAdRequestParams {

    @NonNull
    private final TargetingInfo targetingInfo;
    @NonNull
    private final DeviceInfo deviceInfo;
    @NonNull
    private final DataRestrictions dataRestrictions;

    UnifiedAdRequestParamsImpl(@NonNull TargetingParams targetingParams,
                               @NonNull DataRestrictions dataRestrictions) {
        this.targetingInfo = new TargetingInfoImpl(dataRestrictions, targetingParams);
        this.deviceInfo = new DeviceInfoImpl(dataRestrictions);
        this.dataRestrictions = dataRestrictions;
    }

    @Nullable
    @Override
    public AdRequest getAdRequest() {
        return null;
    }

    @NonNull
    @Override
    public DataRestrictions getDataRestrictions() {
        return dataRestrictions;
    }

    @Override
    public TargetingInfo getTargetingParams() {
        return targetingInfo;
    }

    @Override
    @NonNull
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    @Override
    public boolean isTestMode() {
        return BidMachineImpl.get().isTestMode();
    }

}
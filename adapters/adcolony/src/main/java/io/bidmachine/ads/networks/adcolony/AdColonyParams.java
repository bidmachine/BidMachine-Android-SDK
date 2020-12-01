package io.bidmachine.ads.networks.adcolony;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import io.bidmachine.unified.UnifiedAdCallback;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.unified.UnifiedParams;
import io.bidmachine.utils.BMError;

class AdColonyParams extends UnifiedParams {

    final String zoneId;
    final String adm;

    public AdColonyParams(@NonNull UnifiedMediationParams mediationParams) {
        super(mediationParams);

        zoneId = mediationParams.getString(AdColonyConfig.KEY_ZONE_ID);
        adm = mediationParams.getString(AdColonyConfig.KEY_ADM);
    }

    @Override
    public boolean isValid(@NonNull UnifiedAdCallback callback) {
        if (TextUtils.isEmpty(zoneId)) {
            callback.onAdLoadFailed(BMError.requestError("zone_id not provided"));
            return false;
        }
        if (TextUtils.isEmpty(adm)) {
            callback.onAdLoadFailed(BMError.requestError("adm not provided"));
            return false;
        }
        return true;
    }

}
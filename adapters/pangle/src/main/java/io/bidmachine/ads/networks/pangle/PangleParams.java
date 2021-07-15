package io.bidmachine.ads.networks.pangle;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import io.bidmachine.unified.UnifiedAdCallback;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.unified.UnifiedParams;
import io.bidmachine.utils.BMError;

class PangleParams extends UnifiedParams {

    final String slotId;
    final String bidPayload;

    PangleParams(@NonNull UnifiedMediationParams mediationParams) {
        super(mediationParams);

        slotId = mediationParams.getString(PangleConfig.KEY_SLOT_ID);
        bidPayload = mediationParams.getString(PangleConfig.KEY_BID_PAYLOAD);
    }

    @Override
    public boolean isValid(@NonNull UnifiedAdCallback callback) {
        if (slotId == null) {
            callback.onAdLoadFailed(BMError.notFound(PangleConfig.KEY_SLOT_ID));
            return false;
        }
        if (TextUtils.isEmpty(bidPayload)) {
            callback.onAdLoadFailed(BMError.notFound(PangleConfig.KEY_BID_PAYLOAD));
            return false;
        }
        return true;
    }

}
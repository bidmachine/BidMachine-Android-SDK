package io.bidmachine.ads.networks.facebook;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import io.bidmachine.unified.UnifiedAdCallback;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.unified.UnifiedParams;
import io.bidmachine.utils.BMError;

class FacebookParams extends UnifiedParams {

    final String placementId;
    final String bidPayload;

    FacebookParams(@NonNull UnifiedMediationParams mediationParams) {
        super(mediationParams);
        placementId = mediationParams.getString(FacebookConfig.KEY_PLACEMENT_ID);
        bidPayload = mediationParams.getString(FacebookConfig.KEY_BID_PAYLOAD);
    }

    @Override
    public boolean isValid(@NonNull UnifiedAdCallback callback) {
        if (TextUtils.isEmpty(placementId)) {
            callback.onAdLoadFailed(BMError.requestError("placement_id not provided"));
            return false;
        }
        if (TextUtils.isEmpty(bidPayload)) {
            callback.onAdLoadFailed(BMError.requestError("bid_payload not provided"));
            return false;
        }
        return true;
    }
}

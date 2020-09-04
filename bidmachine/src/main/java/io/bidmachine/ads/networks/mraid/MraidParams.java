package io.bidmachine.ads.networks.mraid;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import io.bidmachine.unified.UnifiedAdCallback;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.unified.UnifiedParams;
import io.bidmachine.utils.BMError;
import io.bidmachine.utils.IabUtils;

class MraidParams extends UnifiedParams {

    final String creativeAdm;
    final int width;
    final int height;
    final boolean canPreload;
    final int loadSkipOffset;
    final boolean useNativeClose;
    final int skipOffset;
    final int companionSkipOffset;

    MraidParams(@NonNull UnifiedMediationParams mediationParams) {
        super(mediationParams);
        creativeAdm = mediationParams.getString(IabUtils.KEY_CREATIVE_ADM);
        width = mediationParams.getInt(IabUtils.KEY_WIDTH);
        height = mediationParams.getInt(IabUtils.KEY_HEIGHT);
        canPreload = mediationParams.getBool(IabUtils.KEY_PRELOAD);
        loadSkipOffset = mediationParams.getInt(IabUtils.KEY_LOAD_SKIP_OFFSET);
        skipOffset = mediationParams.getInt(IabUtils.KEY_SKIP_OFFSET);
        companionSkipOffset = mediationParams.getInt(IabUtils.KEY_COMPANION_SKIP_OFFSET);
        useNativeClose = mediationParams.getBool(IabUtils.KEY_USE_NATIVE_CLOSE);
    }

    @Override
    public boolean isValid(@NonNull UnifiedAdCallback callback) {
        if (TextUtils.isEmpty(creativeAdm) || width == 0 || height == 0) {
            callback.onAdLoadFailed(BMError.IncorrectAdUnit);
            return false;
        }
        return true;
    }

}

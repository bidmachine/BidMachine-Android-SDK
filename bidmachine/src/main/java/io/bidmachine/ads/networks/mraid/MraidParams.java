package io.bidmachine.ads.networks.mraid;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.explorestack.iab.utils.IabElementStyle;

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
    final boolean omsdkEnabled;
    final int skipOffset;
    final int companionSkipOffset;
    final boolean r1;
    final boolean r2;
    final boolean ignoresSafeAreaLayoutGuide;
    final String storeUrl;
    final int progressDuration;
    final IabElementStyle closeableViewStyle;
    final IabElementStyle countDownStyle;
    final IabElementStyle progressStyle;

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
        omsdkEnabled = mediationParams.getBool(IabUtils.KEY_OM_SDK_ENABLED, true);
        r1 = mediationParams.getBool(IabUtils.KEY_R1);
        r2 = mediationParams.getBool(IabUtils.KEY_R2);
        ignoresSafeAreaLayoutGuide = mediationParams.getBool(IabUtils.KEY_IGNORE_SAFE_AREA_LAYOUT_GUIDE);
        storeUrl = mediationParams.getString(IabUtils.KEY_STORE_URL);
        progressDuration = mediationParams.getInt(IabUtils.KEY_PROGRESS_DURATION);
        closeableViewStyle = mediationParams.getObject(IabUtils.KEY_CLOSABLE_VIEW_STYLE);
        countDownStyle = mediationParams.getObject(IabUtils.KEY_COUNTDOWN_STYLE);
        progressStyle = mediationParams.getObject(IabUtils.KEY_PROGRESS_STYLE);
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

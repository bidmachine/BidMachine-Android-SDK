package io.bidmachine.ads.networks.nast;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import io.bidmachine.MediaAssetType;
import io.bidmachine.unified.UnifiedAdCallback;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.unified.UnifiedNativeAdRequestParams;
import io.bidmachine.unified.UnifiedParams;
import io.bidmachine.utils.BMError;
import io.bidmachine.utils.IabUtils;

public class NastParams extends UnifiedParams {

    final String title;
    final String description;
    final String callToAction;
    final float rating;
    final String iconUrl;
    final String imageUrl;
    final String videoUrl;
    final String videoAdm;
    final String clickUrl;

    NastParams(@NonNull UnifiedMediationParams mediationParams) {
        super(mediationParams);

        title = mediationParams.getString(IabUtils.KEY_TITLE);
        description = mediationParams.getString(IabUtils.KEY_DESCRIPTION);
        callToAction = mediationParams.getString(IabUtils.KEY_CTA);
        rating = mediationParams.getFloat(IabUtils.KEY_RATING);
        iconUrl = mediationParams.getString(IabUtils.KEY_ICON_URL);
        imageUrl = mediationParams.getString(IabUtils.KEY_IMAGE_URL);
        videoUrl = mediationParams.getString(IabUtils.KEY_VIDEO_URL);
        videoAdm = mediationParams.getString(IabUtils.KEY_VIDEO_ADM);
        clickUrl = mediationParams.getString(IabUtils.KEY_CLICK_URL);
    }

    @Override
    public boolean isValid(@NonNull UnifiedAdCallback callback) {
        if (TextUtils.isEmpty(title)) {
            callback.onAdLoadFailed(BMError.requestError("title not provided"));
            return false;
        }
        if (TextUtils.isEmpty(callToAction)) {
            callback.onAdLoadFailed(BMError.requestError("callToAction not provided"));
            return false;
        }
        return true;
    }

    boolean isValid(@NonNull UnifiedNativeAdRequestParams adRequestParams,
                    @NonNull UnifiedAdCallback callback) {
        if (!isValid(callback)) {
            return false;
        }
        if (adRequestParams.containsAssetType(MediaAssetType.Icon)
                && TextUtils.isEmpty(iconUrl)) {
            callback.onAdLoadFailed(BMError.requestError(
                    "Your request settings require ICON, but it not provided in response"));
            return false;
        }
        if (adRequestParams.containsAssetType(MediaAssetType.Image)
                && TextUtils.isEmpty(imageUrl)) {
            callback.onAdLoadFailed(BMError.requestError(
                    "Your request settings require IMAGE, but it not provided in response"));
            return false;
        }
        if (adRequestParams.containsAssetType(MediaAssetType.Video)
                && TextUtils.isEmpty(videoAdm)
                && TextUtils.isEmpty(videoUrl)) {
            callback.onAdLoadFailed(BMError.requestError(
                    "Your request settings require VIDEO, but it not provided in response"));
            return false;
        }
        return true;
    }

}
package io.bidmachine.ads.networks.nast;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.bidmachine.ContextProvider;
import io.bidmachine.nativead.NativeNetworkAdapter;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.unified.UnifiedNativeAd;
import io.bidmachine.unified.UnifiedNativeAdCallback;
import io.bidmachine.unified.UnifiedNativeAdRequestParams;

class NastNative extends UnifiedNativeAd {

    @Override
    public void load(@NonNull ContextProvider contextProvider,
                     @NonNull UnifiedNativeAdCallback callback,
                     @NonNull UnifiedNativeAdRequestParams adRequestParams,
                     @NonNull UnifiedMediationParams mediationParams) throws Throwable {
        NastParams nastParams = new NastParams(mediationParams);
        if (!nastParams.isValid(adRequestParams, callback)) {
            return;
        }
        callback.onAdLoaded(new NastAdapter(nastParams));
    }

    private static final class NastAdapter extends NativeNetworkAdapter {

        private final NastParams nastParams;

        NastAdapter(@NonNull NastParams nastParams) {
            this.nastParams = nastParams;
        }

        @Override
        public String getTitle() {
            return nastParams.title;
        }

        @Override
        public String getDescription() {
            return nastParams.description;
        }

        @Override
        public String getCallToAction() {
            return nastParams.callToAction;
        }

        @Override
        public float getRating() {
            return nastParams.rating;
        }

        @Nullable
        @Override
        public String getIconUrl() {
            return nastParams.iconUrl;
        }

        @Nullable
        @Override
        public String getImageUrl() {
            return nastParams.imageUrl;
        }

        @Nullable
        @Override
        public String getVideoUrl() {
            return nastParams.videoUrl;
        }

        @Nullable
        @Override
        public String getVideoAdm() {
            return nastParams.videoAdm;
        }

        @Nullable
        @Override
        public String getClickUrl() {
            return nastParams.clickUrl;
        }

        @Override
        public boolean hasVideo() {
            return !TextUtils.isEmpty(getVideoAdm()) || !TextUtils.isEmpty(getVideoUrl());
        }

    }

}
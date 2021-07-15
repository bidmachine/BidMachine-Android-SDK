package io.bidmachine.ads.networks.pangle;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.util.List;

import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedBannerAd;
import io.bidmachine.unified.UnifiedBannerAdCallback;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.unified.UnifiedMediationParams;
import io.bidmachine.utils.BMError;

class PangleBannerAd extends UnifiedBannerAd {

    @Nullable
    private LoadListener loadListener;
    @Nullable
    private InteractionListener interactionlistener;
    @Nullable
    private TTAdNative ttAdNative;
    @Nullable
    private TTNativeExpressAd ttNativeExpressAd;

    @Override
    public void load(@NonNull ContextProvider context,
                     @NonNull UnifiedBannerAdCallback callback,
                     @NonNull UnifiedBannerAdRequestParams requestParams,
                     @NonNull UnifiedMediationParams mediationParams) throws Throwable {
        PangleParams params = new PangleParams(mediationParams);
        if (!params.isValid(callback)) {
            return;
        }
        assert params.slotId != null;
        assert params.bidPayload != null;

        AdSlot.Builder adSlotBuilder = new AdSlot.Builder()
                .setCodeId(params.slotId)
                .withBid(params.bidPayload);
        switch (requestParams.getBannerSize()) {
            case Size_728x90:
                adSlotBuilder.setExpressViewAcceptedSize(728, 90);
                break;
            case Size_300x250:
                adSlotBuilder.setExpressViewAcceptedSize(300, 250);
                break;
            default:
                adSlotBuilder.setExpressViewAcceptedSize(320, 50);
                break;
        }

        loadListener = new LoadListener(this, callback);
        ttAdNative = TTAdSdk.getAdManager().createAdNative(context.getApplicationContext());
        ttAdNative.loadBannerExpressAd(adSlotBuilder.build(), loadListener);
    }

    void prepareToShow(@NonNull TTNativeExpressAd ttNativeExpressAd,
                       @NonNull UnifiedBannerAdCallback callback) {
        this.ttNativeExpressAd = ttNativeExpressAd;

        interactionlistener = new InteractionListener(callback);
        ttNativeExpressAd.setExpressInteractionListener(interactionlistener);
        ttNativeExpressAd.render();
    }

    @Override
    public void onDestroy() {
        ttAdNative = null;
        loadListener = null;
        interactionlistener = null;
        if (ttNativeExpressAd != null) {
            ttNativeExpressAd.setExpressInteractionListener((TTNativeExpressAd.ExpressAdInteractionListener) null);
            ttNativeExpressAd.destroy();
            ttNativeExpressAd = null;
        }
    }


    private static final class LoadListener implements TTAdNative.NativeExpressAdListener {

        @NonNull
        private final PangleBannerAd pangleBannerAd;
        @NonNull
        private final UnifiedBannerAdCallback callback;

        public LoadListener(@NonNull PangleBannerAd pangleBannerAd,
                            @NonNull UnifiedBannerAdCallback callback) {
            this.pangleBannerAd = pangleBannerAd;
            this.callback = callback;
        }

        @Override
        public void onNativeExpressAdLoad(List<TTNativeExpressAd> bannerList) {
            if (bannerList == null || bannerList.size() == 0) {
                return;
            }

            for (TTNativeExpressAd ttNativeExpressAd : bannerList) {
                if (ttNativeExpressAd != null) {
                    pangleBannerAd.prepareToShow(ttNativeExpressAd, callback);
                    break;
                }
            }
        }

        @Override
        public void onError(int errorCode, String message) {
            callback.onAdLoadFailed(BMError.noFill());
        }

    }

    private static final class InteractionListener implements TTNativeExpressAd.ExpressAdInteractionListener {

        private final UnifiedBannerAdCallback callback;

        public InteractionListener(@NonNull UnifiedBannerAdCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onRenderSuccess(View view, float width, float height) {
            callback.onAdLoaded(view);
        }

        @Override
        public void onRenderFail(View view, String message, int errorCode) {
            callback.onAdLoadFailed(BMError.noFill());
        }

        @Override
        public void onAdShow(View view, int type) {

        }

        @Override
        public void onAdClicked(View view, int type) {
            callback.onAdClicked();
        }

    }

}
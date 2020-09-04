package io.bidmachine.ads.networks.mraid;

import android.webkit.WebView;

import com.explorestack.iab.mraid.MRAIDNativeFeatureListener;
import com.explorestack.iab.mraid.MRAIDView;
import com.explorestack.iab.mraid.MRAIDViewListener;
import com.explorestack.iab.utils.Utils;

import io.bidmachine.unified.UnifiedBannerAdCallback;
import io.bidmachine.utils.BMError;

class MraidBannerAdListener implements MRAIDViewListener, MRAIDNativeFeatureListener {

    private MraidBannerAd adObject;
    private UnifiedBannerAdCallback callback;

    MraidBannerAdListener(MraidBannerAd adObject, UnifiedBannerAdCallback callback) {
        this.adObject = adObject;
        this.callback = callback;
    }

    @Override
    public void mraidViewLoaded(MRAIDView mraidView) {
        adObject.processMraidViewLoaded(callback);
    }

    @Override
    public void mraidViewExpand(MRAIDView mraidView) {
    }

    @Override
    public void mraidViewClose(MRAIDView mraidView) {
    }

    @Override
    public boolean mraidViewResize(MRAIDView mraidView, int width, int height, int offsetX, int offsetY) {
        return false;
    }

    @Override
    public void mraidViewNoFill(MRAIDView mraidView) {
        callback.onAdLoadFailed(BMError.noFillError(null));
    }

    @Override
    public void mraidNativeFeatureCallTel(String url) {
    }

    @Override
    public void mraidNativeFeatureCreateCalendarEvent(String eventJSON) {
    }

    @Override
    public void mraidNativeFeaturePlayVideo(String url) {
    }

    @Override
    public void mraidNativeFeatureOpenBrowser(String url, WebView view) {
        callback.onAdClicked();
        if (url != null && adObject.mraidView != null) {
            Utils.addBannerSpinnerView(adObject.mraidView);
            Utils.openBrowser(adObject.mraidView.getContext(), url, new Runnable() {
                @Override
                public void run() {
                    Utils.hideBannerSpinnerView(adObject.mraidView);
                }
            });
        }
    }

    @Override
    public void mraidNativeFeatureStorePicture(String url) {
    }

    @Override
    public void mraidNativeFeatureSendSms(String url) {
    }

}
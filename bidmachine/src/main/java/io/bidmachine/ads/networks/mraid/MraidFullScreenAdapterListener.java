package io.bidmachine.ads.networks.mraid;

import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.explorestack.iab.mraid.MRAIDInterstitial;
import com.explorestack.iab.mraid.MRAIDInterstitialListener;
import com.explorestack.iab.mraid.MRAIDNativeFeatureListener;
import com.explorestack.iab.utils.Utils;

import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.utils.BMError;

class MraidFullScreenAdapterListener implements MRAIDInterstitialListener,
        MRAIDNativeFeatureListener {

    @NonNull
    private final MraidFullScreenAd adObject;
    @Nullable
    private Runnable afterStartShowRunnable;
    @NonNull
    private UnifiedFullscreenAdCallback callback;

    MraidFullScreenAdapterListener(@NonNull MraidFullScreenAd adObject,
                                   @NonNull UnifiedFullscreenAdCallback callback) {
        this.adObject = adObject;
        this.callback = callback;
    }

    @Override
    public void mraidInterstitialLoaded(MRAIDInterstitial mraidInterstitial) {
        callback.onAdLoaded();
    }

    @Override
    public void mraidInterstitialShow(MRAIDInterstitial mraidInterstitial) {
        if (afterStartShowRunnable != null) {
            afterStartShowRunnable.run();
        }
        callback.onAdShown();
    }

    @Override
    public void mraidInterstitialHide(MRAIDInterstitial mraidInterstitial) {
        callback.onAdFinished();
        callback.onAdClosed();
        final MraidActivity showingActivity = adObject.getShowingActivity();
        if (showingActivity != null) {
            showingActivity.finish();
            showingActivity.overridePendingTransition(0, 0);
            adObject.setShowingActivity(null);
        }
    }

    @Override
    public void mraidInterstitialNoFill(MRAIDInterstitial mraidInterstitial) {
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
        if (url != null && adObject.getShowingActivity() != null) {
            if (adObject.getShowingActivity() != null) {
                adObject.getShowingActivity().showProgressBar();
                Utils.openBrowser(adObject.getShowingActivity().getApplicationContext(), url,
                        new Runnable() {
                            @Override
                            public void run() {
                                if (adObject.getShowingActivity() != null) {
                                    adObject.getShowingActivity().hideProgressBar();
                                }
                            }
                        });
            }
        }
    }

    @Override
    public void mraidNativeFeatureStorePicture(String url) {

    }

    @Override
    public void mraidNativeFeatureSendSms(String url) {

    }

    void setAfterStartShowRunnable(@Nullable Runnable afterStartShowRunnable) {
        this.afterStartShowRunnable = afterStartShowRunnable;
    }
}
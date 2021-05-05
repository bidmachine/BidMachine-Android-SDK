package io.bidmachine.ads.networks.vast;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.explorestack.iab.utils.IabClickCallback;
import com.explorestack.iab.utils.Utils;
import com.explorestack.iab.vast.VastActivityListener;
import com.explorestack.iab.vast.VastError;
import com.explorestack.iab.vast.VastRequest;
import com.explorestack.iab.vast.VastRequestListener;
import com.explorestack.iab.vast.activity.VastActivity;
import com.explorestack.iab.vast.processor.VastAd;
import com.explorestack.iab.vast.tags.AdVerificationsExtensionTag;

import java.util.List;

import io.bidmachine.measurer.VastOMSDKAdMeasurer;
import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.utils.BMError;

class VastFullScreenAdapterListener implements VastRequestListener, VastActivityListener {

    @NonNull
    private final UnifiedFullscreenAdCallback callback;
    @Nullable
    private final VastOMSDKAdMeasurer vastOMSDKAdMeasurer;

    VastFullScreenAdapterListener(@NonNull UnifiedFullscreenAdCallback callback,
                                  @Nullable VastOMSDKAdMeasurer vastOMSDKAdMeasurer) {
        this.callback = callback;
        this.vastOMSDKAdMeasurer = vastOMSDKAdMeasurer;
    }

    @Override
    public void onVastLoaded(@NonNull VastRequest vastRequest) {
        if (vastOMSDKAdMeasurer != null) {
            VastAd vastAd = vastRequest.getVastAd();
            List<AdVerificationsExtensionTag> adVerificationsExtensionTagList = vastAd != null
                    ? vastAd.getAdVerificationsExtensionList()
                    : null;
            vastOMSDKAdMeasurer.addVerificationScriptResourceList(adVerificationsExtensionTagList);
            vastOMSDKAdMeasurer.setSkipOffset(vastRequest.getVideoCloseTime());
        }

        callback.onAdLoaded();
    }

    @Override
    public void onVastError(@NonNull Context context, @NonNull VastRequest vastRequest, int error) {
        //TODO: implement vast error mapping
        switch (error) {
            case VastError.ERROR_CODE_NO_NETWORK: {
                callback.onAdLoadFailed(BMError.noFillError(BMError.Connection));
                break;
            }
            default: {
                callback.onAdLoadFailed(BMError.noFillError(null));
                break;
            }
        }
    }

    @Override
    public void onVastShown(@NonNull VastActivity vastActivity, @NonNull VastRequest vastRequest) {
        if (vastOMSDKAdMeasurer != null) {
            vastOMSDKAdMeasurer.onAdShown();
        }

        callback.onAdShown();
    }

    @Override
    public void onVastClick(@NonNull VastActivity vastActivity,
                            @NonNull VastRequest vastRequest,
                            @NonNull final IabClickCallback iabClickCallback,
                            @Nullable String url) {
        if (vastOMSDKAdMeasurer != null) {
            vastOMSDKAdMeasurer.onAdClicked();
        }

        callback.onAdClicked();
        if (url != null) {
            Utils.openBrowser(vastActivity, url, new Runnable() {
                @Override
                public void run() {
                    iabClickCallback.clickHandled();
                }
            });
        } else {
            iabClickCallback.clickHandleCanceled();
        }
    }

    @Override
    public void onVastComplete(@NonNull VastActivity vastActivity,
                               @NonNull VastRequest vastRequest) {
        callback.onAdFinished();
    }

    @Override
    public void onVastDismiss(@NonNull VastActivity vastActivity,
                              @Nullable VastRequest vastRequest,
                              boolean finished) {
        if (vastOMSDKAdMeasurer != null) {
            vastOMSDKAdMeasurer.destroy();
        }

        callback.onAdClosed();
    }

}
package io.bidmachine.ads.networks.vast;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.explorestack.iab.utils.Utils;
import com.explorestack.iab.vast.VastActivityListener;
import com.explorestack.iab.vast.VastClickCallback;
import com.explorestack.iab.vast.VastError;
import com.explorestack.iab.vast.VastRequest;
import com.explorestack.iab.vast.VastRequestListener;
import com.explorestack.iab.vast.activity.VastActivity;

import io.bidmachine.unified.UnifiedFullscreenAdCallback;
import io.bidmachine.utils.BMError;

class VastFullScreenAdapterListener implements VastRequestListener, VastActivityListener {

    @NonNull
    private UnifiedFullscreenAdCallback callback;

    VastFullScreenAdapterListener(@NonNull UnifiedFullscreenAdCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onVastLoaded(@NonNull VastRequest vastRequest) {
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
        callback.onAdShown();
    }

    @Override
    public void onVastClick(@NonNull VastActivity vastActivity,
                            @NonNull VastRequest vastRequest,
                            @NonNull final VastClickCallback vastClickCallback,
                            @Nullable String url) {
        callback.onAdClicked();
        if (url != null) {
            Utils.openBrowser(vastActivity, url, new Runnable() {
                @Override
                public void run() {
                    vastClickCallback.clickHandled();
                }
            });
        } else {
            vastClickCallback.clickHandleCanceled();
        }
    }

    @Override
    public void onVastComplete(@NonNull VastActivity vastActivity, @NonNull VastRequest vastRequest) {
        callback.onAdFinished();
    }

    @Override
    public void onVastDismiss(@NonNull VastActivity vastActivity, @Nullable VastRequest vastRequest, boolean finished) {
        callback.onAdClosed();
    }

}

package io.bidmachine.ads.networks.mraid;

import androidx.annotation.NonNull;

import com.explorestack.iab.mraid.MraidError;
import com.explorestack.iab.mraid.MraidView;
import com.explorestack.iab.mraid.MraidViewListener;
import com.explorestack.iab.utils.IabClickCallback;
import com.explorestack.iab.utils.Utils;

import io.bidmachine.ContextProvider;
import io.bidmachine.unified.UnifiedBannerAdCallback;
import io.bidmachine.utils.BMError;

class MraidBannerAdListener implements MraidViewListener {

    @NonNull
    private final ContextProvider contextProvider;
    @NonNull
    private final UnifiedBannerAdCallback callback;

    MraidBannerAdListener(@NonNull ContextProvider contextProvider,
                          @NonNull UnifiedBannerAdCallback callback) {
        this.contextProvider = contextProvider;
        this.callback = callback;
    }

    @Override
    public void onLoaded(@NonNull MraidView mraidView) {
        if (contextProvider.getActivity() != null && mraidView.getParent() == null) {
            mraidView.show(contextProvider.getActivity());
            callback.onAdLoaded(mraidView);
        } else {
            callback.onAdLoadFailed(BMError.internal(
                    "Activity is null or ad view already have parent"));
        }
    }

    @Override
    public void onError(@NonNull MraidView mraidView, int i) {
        if (i == MraidError.SHOW_ERROR) {
            callback.onAdShowFailed(BMError.internal("Error when showing banner object"));
        } else {
            callback.onAdLoadFailed(BMError.noFill());
        }
    }

    @Override
    public void onShown(@NonNull MraidView mraidView) {

    }

    @Override
    public void onOpenBrowser(@NonNull final MraidView mraidView,
                              @NonNull String url,
                              @NonNull final IabClickCallback iabClickCallback) {
        callback.onAdClicked();

        Utils.openBrowser(mraidView.getContext(), url, new Runnable() {
            @Override
            public void run() {
                iabClickCallback.clickHandled();
            }
        });
    }

    @Override
    public void onExpand(@NonNull MraidView mraidView) {

    }

    @Override
    public void onPlayVideo(@NonNull MraidView mraidView, @NonNull String s) {

    }

    @Override
    public void onClose(@NonNull MraidView mraidView) {

    }

}
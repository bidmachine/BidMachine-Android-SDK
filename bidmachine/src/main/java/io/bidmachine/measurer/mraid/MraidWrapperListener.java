package io.bidmachine.measurer.mraid;

import android.support.annotation.NonNull;
import android.webkit.WebView;

import com.explorestack.iab.mraid.MRAIDView;
import com.explorestack.iab.mraid.MRAIDViewLifecycleListener;

import io.bidmachine.core.Utils;

public class MraidWrapperListener implements MRAIDViewLifecycleListener {

    private final MraidIABMeasurer measurer;

    public MraidWrapperListener(@NonNull MraidIABMeasurer measurer) {
        this.measurer = measurer;
    }

    @Override
    public void onWebViewLoaded(@NonNull final MRAIDView mraidView,
                                @NonNull final WebView webView) {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                if (!measurer.isSessionStarted()) {
                    measurer.configure(webView.getContext(), webView);
                    measurer.registerAdView(webView);
                    measurer.startSession();
                    measurer.loaded();
                }
            }
        });
    }

}

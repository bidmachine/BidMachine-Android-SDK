package io.bidmachine.measurer.mraid;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.explorestack.iab.mraid.MRAIDInterstitial;
import com.explorestack.iab.mraid.MRAIDInterstitialListener;
import com.explorestack.iab.mraid.MRAIDView;

import io.bidmachine.core.Utils;

public class MraidFullscreenWrapperListener extends MraidWrapperListener implements MRAIDInterstitialListener {

    private final MraidIABMeasurer measurer;
    private final MRAIDInterstitialListener listener;

    public MraidFullscreenWrapperListener(@NonNull MraidIABMeasurer measurer,
                                          @Nullable MRAIDInterstitialListener listener) {
        super(measurer);
        this.measurer = measurer;
        this.listener = listener;
    }

    @Override
    public void mraidInterstitialLoaded(MRAIDInterstitial mraidInterstitial) {
        if (listener != null) {
            listener.mraidInterstitialLoaded(mraidInterstitial);
        }
    }

    @Override
    public void mraidInterstitialShow(final MRAIDInterstitial mraidInterstitial) {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                MRAIDView mraidView = mraidInterstitial.getMraidView();
                if (mraidView != null) {
                    measurer.addIgnoredViews(mraidView.getNativeViews());
                }
                measurer.shown();
            }
        });

        if (listener != null) {
            listener.mraidInterstitialShow(mraidInterstitial);
        }
    }

    @Override
    public void mraidInterstitialHide(MRAIDInterstitial mraidInterstitial) {
        measurer.destroy();

        if (listener != null) {
            listener.mraidInterstitialHide(mraidInterstitial);
        }
    }

    @Override
    public void mraidInterstitialNoFill(MRAIDInterstitial mraidInterstitial) {
        if (listener != null) {
            listener.mraidInterstitialNoFill(mraidInterstitial);
        }
    }

}
package io.bidmachine.measurer;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import com.explorestack.iab.measurer.AdMeasurer;
import com.iab.omid.library.appodeal.adsession.AdEvents;
import com.iab.omid.library.appodeal.adsession.AdSession;

import java.lang.ref.WeakReference;

import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;

public abstract class OMSDKAdMeasurer<AdView extends View> implements AdMeasurer<AdView> {

    private static final String TAG = "AdMeasurer";

    private AdSession adSession;
    private AdEvents adEvents;

    private WeakReference<View> adViewWeak;

    @Override
    public void registerAdContainer(@NonNull final ViewGroup viewGroup) {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (adSession != null) {
                        registerAdView(adSession, viewGroup);
                    } else {
                        adViewWeak = new WeakReference<>((View) viewGroup);
                    }
                } catch (Throwable t) {
                    Logger.log(t);
                }
            }
        });
    }

    @Override
    public void registerAdView(@NonNull AdView adView) {

    }

    protected boolean isSessionPrepared() {
        return adSession != null;
    }

    /**
     * Execute after ad has loaded
     */
    @UiThread
    protected void prepareAdSession(@NonNull AdSession adSession) {
        try {
            this.adSession = adSession;
            adEvents = AdEvents.createAdEvents(adSession);
            registerViews(adSession);
            adSession.start();
            onAdLoaded(adEvents);

            log("prepareAdSession");
        } catch (Throwable t) {
            Logger.log(t);
        }
    }

    @UiThread
    private void registerViews(@NonNull AdSession adSession) throws Throwable {
        View view = adViewWeak != null ? adViewWeak.get() : null;
        if (view != null) {
            registerAdView(adSession, view);
        }
        adViewWeak = null;
    }

    @UiThread
    private void registerAdView(@NonNull AdSession adSession,
                                @NonNull View view) throws Throwable {
        adSession.registerAdView(view);

        log("registerAdView");
    }

    public void onAdShown() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (adEvents != null) {
                        adEvents.impressionOccurred();
                    }

                    log("onAdShown");
                } catch (Throwable t) {
                    Logger.log(t);
                }
            }
        });
    }

    public void destroy() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    adEvents = null;
                    if (adSession != null) {
                        adSession.finish();
                    }

                    log("destroy");

                    // sessionFinished do not sent if adSession reference is cleared
                    // adSession = null;
                } catch (Throwable t) {
                    Logger.log(t);
                }
            }
        });
    }

    protected void log(@NonNull String message) {
        Logger.log(TAG, message);
    }


    @UiThread
    protected abstract void onAdLoaded(@NonNull AdEvents adEvents) throws Throwable;

}
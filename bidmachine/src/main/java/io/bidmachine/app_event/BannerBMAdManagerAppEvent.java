package io.bidmachine.app_event;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.ref.WeakReference;

import io.bidmachine.core.Utils;

public class BannerBMAdManagerAppEvent extends BMAdManagerAppEvent {

    private final static long REFRESH_TIME = 15000;

    private BannerViewBMAdManagerAppEvent shownBannerView;
    private BannerViewBMAdManagerAppEvent notShownBannerView;
    private WeakReference<Context> contextWeakReference;
    private boolean isHidden = true;
    private boolean refreshTimeReached = true;
    private boolean showInitiated = false;

    public BannerBMAdManagerAppEvent(String adUnitId) {
        super(adUnitId);
    }

    @Override
    public void load(@NonNull final Context context) {
        contextWeakReference = new WeakReference<>(context);

        notShownBannerView = new BannerViewBMAdManagerAppEvent(adUnitId);
        notShownBannerView.setListener(new WrapperListener());
        notShownBannerView.load(context);
    }

    private void setShownBannerView() {
        if (shownBannerView != null) {
            shownBannerView.destroy();
        }
        shownBannerView = notShownBannerView;
        notShownBannerView = null;
    }

    @Override
    public boolean isLoaded() {
        return notShownBannerViewIsLoaded();
    }

    private boolean notShownBannerViewIsLoaded() {
        return notShownBannerView != null && notShownBannerView.isLoaded();
    }

    private boolean shownBannerViewIsLoaded() {
        return shownBannerView != null && shownBannerView.isLoaded();
    }

    @Override
    public void show(@NonNull final Context context) {
        show(context, true);
    }

    private void show(Context context, boolean showInitiated) {
        hide();
        this.showInitiated = showInitiated;
        if (notShownBannerViewIsLoaded()) {
            isHidden = false;
            refreshTimeReached = false;
            notShownBannerView.show(context);
            setShownBannerView();
            loadNextAd(context);
        } else if (shownBannerViewIsLoaded()) {
            Log.e(TAG, "New banner don't loaded yet. Will be shown old banner");
            isHidden = false;
            shownBannerView.show(context);
        } else if (refreshTimeReached) {
            Log.e(TAG, "Banner will be shown after load");
        } else {
            Log.e(TAG, "Could not find loaded banner");
        }
    }

    private void loadNextAd(@NonNull final Context context) {
        contextWeakReference = new WeakReference<>(context);
        load(context);
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                if (notShownBannerView.isLoaded()) {
                    if (!isHidden) {
                        show(context, false);
                    }
                } else {
                    refreshTimeReached = true;
                }
            }
        }, REFRESH_TIME);
    }

    @Override
    public void hide() {
        showInitiated = false;
        isHidden = true;
        if (notShownBannerView != null) {
            notShownBannerView.hide();
        }
        if (shownBannerView != null) {
            shownBannerView.hide();
        }
    }

    @Override
    public void destroy() {
        if (notShownBannerView != null) {
            notShownBannerView.destroy();
        }
        if (shownBannerView != null) {
            shownBannerView.destroy();
        }
    }

    private final class WrapperListener implements BMAdManagerAppEventListener {

        @Override
        public void onAdLoaded() {
            if (refreshTimeReached && (!isHidden || showInitiated)) {
                showInitiated = false;
                Context context = contextWeakReference.get();
                if (context != null) {
                    show(context, false);
                } else {
                    Log.d(TAG, "Context is null");
                }
            }
            if (listener != null) {
                listener.onAdLoaded();
            }
        }

        @Override
        public void onAdFailToLoad() {
            Context context = contextWeakReference.get();
            if (context != null) {
                load(context);
            }
            if (listener != null) {
                listener.onAdFailToLoad();
            }
        }

        @Override
        public void onAdShown() {
            if (listener != null) {
                listener.onAdShown();
            }
        }

        @Override
        public void onAdClicked() {
            if (listener != null) {
                listener.onAdClicked();
            }
        }

        @Override
        public void onAdClosed() {
            if (listener != null) {
                listener.onAdClosed();
            }
        }

        @Override
        public void onAdExpired() {
            if (listener != null) {
                listener.onAdExpired();
            }
        }

    }

}
package io.bidmachine;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.core.VisibilityTracker;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.unified.UnifiedAd;
import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedBannerAdCallback;
import io.bidmachine.utils.BMError;

public final class ViewAdObject<
        AdRequestType extends AdRequest<AdRequestType, UnifiedAdRequestParamsType>,
        UnifiedAdType extends UnifiedAd<UnifiedBannerAdCallback, UnifiedAdRequestParamsType>,
        UnifiedAdRequestParamsType extends UnifiedAdRequestParams>
        extends AdObjectImpl<AdRequestType, AdObjectParams, UnifiedAdType, UnifiedBannerAdCallback, UnifiedAdRequestParamsType> {

    private View adView;

    private final MeasureMode widthMeasureMode = MeasureMode.Direct;
    private final MeasureMode heightMeasureMode = MeasureMode.Direct;

    private int width;
    private int height;

    public ViewAdObject(@NonNull ContextProvider contextProvider,
                        @NonNull AdProcessCallback processCallback,
                        @NonNull AdRequestType adRequest,
                        @NonNull AdObjectParams adObjectParams,
                        @NonNull UnifiedAdType unifiedAd) {
        super(contextProvider, processCallback, adRequest, adObjectParams, unifiedAd);
    }

    @NonNull
    @Override
    public UnifiedBannerAdCallback createUnifiedCallback(@NonNull final AdProcessCallback processCallback) {
        return new UnifiedViewAdCallbackImpl(processCallback);
    }

    void show(@Nullable ViewGroup container) {
        if (container == null) {
            Logger.log("Target container is null");
            getUnifiedAdCallback().onAdShowFailed(BMError.Internal);
            return;
        }
        if (getWidth() == 0 || getHeight() == 0) {
            Logger.log("Width or height are not provided");
            getUnifiedAdCallback().onAdShowFailed(BMError.Internal);
            return;
        }
        Context context = container.getContext();
        if (adView != null) {
            VisibilityTracker.stopTracking(adView);
        }
        container.removeAllViews();
        final ViewGroup.LayoutParams params;
        if (container instanceof FrameLayout) {
            params = new FrameLayout.LayoutParams(getScaledWidth(context),
                                                  getScaledHeight(context),
                                                  Gravity.CENTER);
        } else {
            params = new ViewGroup.LayoutParams(getScaledWidth(context), getScaledHeight(context));
        }
        container.addView(adView, params);
        VisibilityTracker.startTracking(adView,
                                        getParams().getViewabilityTimeThresholdMs(),
                                        getParams().getViewabilityPixelThreshold(),
                                        getParams().isViewabilityIgnoreWindowFocus(),
                                        new VisibilityTracker.VisibilityChangeCallback() {
                                            @Override
                                            public void onViewShown() {
                                                getProcessCallback().processShown();
                                            }

                                            @Override
                                            public void onViewTrackingFinished() {
                                                getProcessCallback().processImpression();
                                            }
                                        });
    }

    @Override
    public void onImpression() {
        super.onImpression();

        VisibilityTracker.stopTracking(adView);
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            if (adView.getParent() instanceof ViewGroup) {
                ((ViewGroup) adView.getParent()).removeView(adView);
            }
            VisibilityTracker.stopTracking(adView);
        }

        super.onDestroy();
    }

    public void setWidth(int width) {
        this.width = width;
    }

    private int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    private int getHeight() {
        return height;
    }

    private int getScaledWidth(@NonNull Context context) {
        return widthMeasureMode.getSize(context, getWidth());
    }

    private int getScaledHeight(@NonNull Context context) {
        return heightMeasureMode.getSize(context, getHeight());
    }

    protected enum MeasureMode {

        Match, Wrap, Direct;

        int getSize(Context context, int directSize) {
            switch (this) {
                case Direct: {
                    return Math.round(directSize * Utils.getScreenDensity(context));
                }
                case Wrap: {
                    return ViewGroup.LayoutParams.WRAP_CONTENT;
                }
                default:
                    return ViewGroup.LayoutParams.MATCH_PARENT;
            }
        }

    }

    private final class UnifiedViewAdCallbackImpl extends BaseUnifiedAdCallback implements UnifiedBannerAdCallback {

        UnifiedViewAdCallbackImpl(@NonNull AdProcessCallback processCallback) {
            super(processCallback);
        }

        @Override
        public void onAdLoaded(@Nullable View adView) {
            if (ViewAdObject.this.adView != null) {
                VisibilityTracker.stopTracking(ViewAdObject.this.adView);
            }
            ViewAdObject.this.adView = adView;
            processCallback.processLoadSuccess();
        }

    }

}
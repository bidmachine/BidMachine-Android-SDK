package io.bidmachine;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.bidmachine.core.Logger;
import io.bidmachine.models.AuctionResult;
import io.bidmachine.models.RequestBuilder;
import io.bidmachine.utils.BMError;
import io.bidmachine.utils.ParamsHelper;

public abstract class AdView<
        SelfType extends AdView<SelfType, AdType, AdRequestType, AdObjectType, ExternalAdListenerType>,
        AdType extends ViewAd<AdType, AdRequestType, AdObjectType, ?, AdListener<AdType>>,
        AdRequestType extends AdRequest<AdRequestType, ?>,
        AdObjectType extends ViewAdObject<AdRequestType, ?, ?>,
        ExternalAdListenerType extends AdListener<SelfType>>
        extends FrameLayout
        implements IAd<SelfType, AdRequestType> {

    private static final long MIN_REFRESH_RATE_MS = 15 * 1000;

    private AdType currentAd;
    private AdType pendingAd;

    protected AdRequestType currentAdRequest;

    private long refreshRate = -1;
    private boolean isAutoLoad = false;
    private boolean isShowPending = false;
    private boolean isAttachedToWindow = false;

    @Nullable
    private ExternalAdListenerType externalListener;

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isLoaded(pendingAd) || !performShow()) {
                isShowPending = true;
            }
        }
    };

    public AdView(@NonNull Context context) {
        this(context, null);
    }

    public AdView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            final TypedArray params = context.getTheme()
                    .obtainStyledAttributes(attrs, R.styleable.AdView, defStyleAttr, 0);
//            setRefreshRate(params.getInteger(R.styleable.AdView_refreshRate, -1));
            isAutoLoad = params.getBoolean(R.styleable.AdView_autoLoad, false);
            params.recycle();

            final RequestBuilder<? extends RequestBuilder, AdRequestType> requestBuilder =
                    createAdRequest(context, attrs, defStyleAttr);
            ParamsHelper.parseRequestParams(requestBuilder, context, attrs, defStyleAttr);

            final AdRequestType request = requestBuilder.build();
            if (isAutoLoad) {
                if (request.isValid()) {
                    currentAdRequest = request;
                    loadIfAuto();
                } else {
                    Logger.log("Request parameters are not valid to handle \"autoLoad\" params!");
                }
            }
        }
    }

    protected abstract RequestBuilder<? extends RequestBuilder, AdRequestType> createAdRequest(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr);

    @SuppressWarnings("unchecked")
    public SelfType setListener(ExternalAdListenerType listener) {
        externalListener = listener;
        return (SelfType) this;
    }

    //TODO:
    // if we desire to implement this function in release version, it's should contains logic
    // for reset refresh runnable and and current requests
    @SuppressWarnings("unchecked")
    private SelfType setRefreshRate(long refreshRateMs) {
        if (refreshRateMs != -1 && refreshRateMs < MIN_REFRESH_RATE_MS) {
            Logger.log("Minimal refresh rate for banner is 15 seconds");
            refreshRate = MIN_REFRESH_RATE_MS;
        } else {
            refreshRate = refreshRateMs;
        }
        return (SelfType) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SelfType load(AdRequestType request) {
        currentAdRequest = request;
        load(true);
        return (SelfType) this;
    }

    private void loadIfAuto() {
        if (isAutoLoad && currentAdRequest != null && currentAdRequest.isValid()) {
            load(true);
        }
    }

    private void load(boolean force) {
        if (force) {
            isShowPending = true;
            removeCallbacks(refreshRunnable);
        }
        if (pendingAd != null) {
            pendingAd.destroy();
        }
        pendingAd = createAd(getContext());
        pendingAd.setListener(adListener);
        pendingAd.load(currentAdRequest);
    }

    @Override
    public void destroy() {
        removeCallbacks(refreshRunnable);
        if (currentAd != null) {
            currentAd.destroy();
            currentAd = null;
        }
        if (pendingAd != null) {
            pendingAd.destroy();
            pendingAd = null;
        }
    }

    @Override
    public boolean isLoaded() {
        return isLoaded(currentAd) || isLoaded(pendingAd);
    }

    private boolean isLoaded(@Nullable IAd ad) {
        return ad != null && ad.isLoaded();
    }

    @Override
    public boolean isLoading() {
        return pendingAd != null && pendingAd.isLoading();
    }

    @Override
    public boolean isExpired() {
        return pendingAd != null ? pendingAd.isExpired() : currentAd != null && currentAd.isExpired();
    }

    @Override
    public boolean isDestroyed() {
        return pendingAd != null ? pendingAd.isDestroyed() : currentAd != null && currentAd.isDestroyed();
    }

    @Nullable
    @Override
    public AuctionResult getAuctionResult() {
        return currentAd != null ? currentAd.getAuctionResult()
                : pendingAd != null ? pendingAd.getAuctionResult() : null;
    }

    protected abstract AdType createAd(Context context);

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (getVisibility() == View.VISIBLE && !performShow()) {
            loadIfAuto();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;
        performShow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
    }

    @Override
    public boolean canShow() {
        return canShow(currentAd) || canShow(pendingAd);
    }

    private boolean canShow(@Nullable IAd ad) {
        return ad != null && ad.canShow();
    }

    private boolean canPerformShow() {
        return isAttachedToWindow && isShowPending && getVisibility() != View.GONE;
    }

    private void prepareDisplayRequest() {
        if (pendingAd != null && pendingAd.isLoaded()) {
            if (currentAd != null) {
                currentAd.destroy();
            }
            currentAd = pendingAd;
            pendingAd = null;
        }
    }

    private boolean performShow() {
        if (canPerformShow()) {
            prepareDisplayRequest();
            if (canShow()) {
                currentAd.show(this);
                isShowPending = false;
                return true;
            }
        }
        return false;
    }

    private final AdListener<AdType> adListener = new AdListener<AdType>() {

        @Override
        @SuppressWarnings("unchecked")
        public void onAdLoaded(@NonNull AdType ad) {
            if (externalListener != null) {
                externalListener.onAdLoaded((SelfType) AdView.this);
            }
            performShow();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onAdLoadFailed(@NonNull AdType ad, @NonNull BMError error) {
            if (externalListener != null) {
                externalListener.onAdLoadFailed((SelfType) AdView.this, error);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onAdShown(@NonNull AdType ad) {
            if (externalListener != null) {
                externalListener.onAdShown((SelfType) AdView.this);
            }
            if (refreshRate > -1) {
                postDelayed(refreshRunnable, refreshRate);
                load(false);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onAdClicked(@NonNull AdType ad) {
            if (externalListener != null) {
                externalListener.onAdClicked((SelfType) AdView.this);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onAdImpression(@NonNull AdType ad) {
            if (externalListener != null) {
                externalListener.onAdImpression((SelfType) AdView.this);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onAdExpired(@NonNull AdType ad) {
            if (externalListener != null) {
                externalListener.onAdExpired((SelfType) AdView.this);
            }
        }

    };

}


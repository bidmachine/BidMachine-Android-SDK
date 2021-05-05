package io.bidmachine;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.bidmachine.models.AuctionResult;
import io.bidmachine.utils.BMError;

public abstract class AdView<
        SelfType extends AdView<SelfType, AdType, AdRequestType, AdObjectType, ExternalAdListenerType>,
        AdType extends ViewAd<AdType, AdRequestType, AdObjectType, ?, AdListener<AdType>>,
        AdRequestType extends AdRequest<AdRequestType, ?>,
        AdObjectType extends ViewAdObject<AdRequestType, ?, ?>,
        ExternalAdListenerType extends AdListener<SelfType>>
        extends FrameLayout
        implements IAd<SelfType, AdRequestType> {

    private AdType currentAd;
    private AdType pendingAd;

    private boolean isShowPending = false;
    private boolean isAttachedToWindow = false;

    @Nullable
    private ExternalAdListenerType externalListener;

    public AdView(@NonNull Context context) {
        this(context, null);
    }

    public AdView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unchecked")
    public SelfType setListener(ExternalAdListenerType listener) {
        externalListener = listener;
        return (SelfType) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SelfType load(AdRequestType request) {
        isShowPending = true;
        if (pendingAd != null) {
            pendingAd.destroy();
        }
        pendingAd = createAd(getContext());
        pendingAd.setListener(adListener);
        pendingAd.load(request);
        return (SelfType) this;
    }

    @Override
    public void destroy() {
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

    private boolean isLoaded(@Nullable AdType ad) {
        return ad != null && ad.isLoaded();
    }

    @Override
    public boolean isLoading() {
        return pendingAd != null && pendingAd.isLoading();
    }

    @Override
    public boolean isExpired() {
        return pendingAd != null
                ? pendingAd.isExpired()
                : currentAd != null && currentAd.isExpired();
    }

    @Override
    public boolean isDestroyed() {
        return pendingAd != null
                ? pendingAd.isDestroyed()
                : currentAd != null && currentAd.isDestroyed();
    }

    @Nullable
    @Override
    public AuctionResult getAuctionResult() {
        return currentAd != null
                ? currentAd.getAuctionResult()
                : pendingAd != null
                        ? pendingAd.getAuctionResult()
                        : null;
    }

    protected abstract AdType createAd(Context context);

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        if (getVisibility() == View.VISIBLE) {
            performShow();
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
        isShowPending = true;
    }

    @Override
    public boolean canShow() {
        return canShow(currentAd) || canShow(pendingAd);
    }

    private boolean canShow(@Nullable AdType ad) {
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

    private void performShow() {
        if (canPerformShow()) {
            prepareDisplayRequest();
            if (canShow()) {
                currentAd.show(this);
                isShowPending = false;
            }
        }
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
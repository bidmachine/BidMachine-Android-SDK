package io.bidmachine.nativead;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Set;

import io.bidmachine.AdProcessCallback;
import io.bidmachine.AdsType;
import io.bidmachine.BidMachineAd;
import io.bidmachine.ContextProvider;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.core.Logger;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.nativead.view.NativeMediaView;
import io.bidmachine.unified.UnifiedNativeAd;
import io.bidmachine.unified.UnifiedNativeAdRequestParams;

public final class NativeAd
        extends BidMachineAd<NativeAd, NativeRequest, NativeAdObject, AdObjectParams, UnifiedNativeAdRequestParams, NativeListener>
        implements NativePublicData, NativeContainer {

    public NativeAd(@NonNull Context context) {
        super(context, AdsType.Native);
    }

    @Override
    protected NativeAdObject createAdObject(@NonNull ContextProvider contextProvider,
                                            @NonNull NativeRequest adRequest,
                                            @NonNull NetworkAdapter adapter,
                                            @NonNull AdObjectParams adObjectParams,
                                            @NonNull AdProcessCallback processCallback) {
        UnifiedNativeAd unifiedNativeAd = adapter.createNativeAd();
        if (unifiedNativeAd == null) {
            return null;
        }
        return new NativeAdObject(contextProvider,
                                  processCallback,
                                  adRequest,
                                  adObjectParams,
                                  unifiedNativeAd);
    }

    @Nullable
    @Override
    public String getTitle() {
        return hasLoadedObject() ? getLoadedObject().getTitle() : null;
    }

    @Nullable
    @Override
    public String getDescription() {
        return hasLoadedObject() ? getLoadedObject().getDescription() : null;
    }

    @Nullable
    @Override
    public String getCallToAction() {
        return hasLoadedObject() ? getLoadedObject().getCallToAction() : null;
    }

    @Override
    public float getRating() {
        return hasLoadedObject() ? getLoadedObject().getRating() : NativeAdObject.DEFAULT_RATING;
    }

    @Override
    public boolean hasVideo() {
        return hasLoadedObject() && getLoadedObject().hasVideo();
    }

    @Nullable
    @Override
    public View getProviderView(Context context) {
        return hasLoadedObject() ? getLoadedObject().getProviderView(context) : null;
    }

    @Override
    public void registerView(@Nullable ViewGroup nativeAdView,
                             @Nullable View iconView,
                             @Nullable NativeMediaView nativeMediaView,
                             @Nullable Set<View> clickableViews) {
        if (hasLoadedObject()) {
            getLoadedObject().registerView(nativeAdView, iconView, nativeMediaView, clickableViews);
        }
    }

    @Override
    public void unregisterView() {
        if (hasLoadedObject()) {
            getLoadedObject().unregisterView();
        }
    }

    @Override
    public boolean isViewRegistered() {
        return hasLoadedObject() && getLoadedObject().isViewRegistered();
    }

    private boolean hasLoadedObject() {
        if (getLoadedObject() == null) {
            Logger.log(toStringShort() + ": not loaded, please load ads first!");
            return false;
        }
        return true;
    }

}
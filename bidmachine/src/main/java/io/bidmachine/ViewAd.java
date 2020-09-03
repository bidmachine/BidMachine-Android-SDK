package io.bidmachine;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import io.bidmachine.models.AdObjectParams;
import io.bidmachine.unified.UnifiedAdRequestParams;

public abstract class ViewAd<
        AdType extends ViewAd<AdType, AdRequestType, AdObjectType, UnifiedAdRequestParamsType, AdListenerType>,
        AdRequestType extends AdRequest<AdRequestType, UnifiedAdRequestParamsType>,
        AdObjectType extends ViewAdObject<AdRequestType, ?, UnifiedAdRequestParamsType>,
        UnifiedAdRequestParamsType extends UnifiedAdRequestParams,
        AdListenerType extends AdListener<AdType>>
        extends BidMachineAd<AdType, AdRequestType, AdObjectType, AdObjectParams, UnifiedAdRequestParamsType, AdListenerType> {

    protected ViewAd(@NonNull Context context, @NonNull AdsType adsType) {
        super(context, adsType);
    }

    void show(ViewGroup container) {
        final AdObjectType loadedObject = getLoadedObject();
        if (prepareShow() && loadedObject != null) {
            loadedObject.show(container);
        }
    }

}
package io.bidmachine;

import android.content.Context;
import android.view.ViewGroup;

import io.bidmachine.displays.DisplayAdObjectParams;

public abstract class ViewAd<
        AdType extends ViewAd<AdType, AdRequestType, AdObjectType, AdListenerType>,
        AdRequestType extends AdRequest<AdRequestType>,
        AdObjectType extends ViewAdObject<AdType>,
        AdListenerType extends AdListener<AdType>>
        extends OrtbAd<AdType, AdRequestType, AdObjectType, DisplayAdObjectParams, AdListenerType> {

    public ViewAd(Context context) {
        super(context);
    }

    void show(ViewGroup container) {
        final AdObjectType loadedObject = getLoadedObject();
        if (prepareShow() && loadedObject != null) {
            loadedObject.show(container);
        }
    }

}
package io.bidmachine;

import android.content.Context;

import androidx.annotation.NonNull;

import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.utils.BMError;

public abstract class FullScreenAd<
        SelfType extends FullScreenAd<SelfType, AdRequestType, AdObjectType, ListenerType>,
        AdRequestType extends FullScreenAdRequest<AdRequestType>,
        AdObjectType extends FullScreenAdObject<AdRequestType>,
        ListenerType extends AdListener<SelfType>>
        extends BidMachineAd<SelfType, AdRequestType, AdObjectType, AdObjectParams, UnifiedFullscreenAdRequestParams, ListenerType> {

    protected FullScreenAd(@NonNull Context context, @NonNull AdsType adsType) {
        super(context, adsType);
    }

    public void show() {
        final AdObjectType loadedObject = getLoadedObject();
        if (!prepareShow() || loadedObject == null) {
            return;
        }
        if (!Utils.isNetworkAvailable(getContext())) {
            processCallback.processShowFail(BMError.NoConnection);
        } else {
            try {
                loadedObject.show(getContextProvider());
            } catch (Throwable t) {
                Logger.log(t);
                processCallback.processShowFail(BMError.internal(
                        "Exception when showing fullscreen object"));
            }
        }
    }

    @Override
    public boolean canShow() {
        return super.canShow() && Utils.isNetworkAvailable(getContext());
    }

}
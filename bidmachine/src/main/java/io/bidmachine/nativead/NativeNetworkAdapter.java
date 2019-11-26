package io.bidmachine.nativead;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import java.util.Set;

import io.bidmachine.nativead.view.NativeMediaView;

public abstract class NativeNetworkAdapter implements NativeData {

    /**
     * Method to configure {@link NativeMediaView} to display media view from the AdNetwork.
     * Initially {@link NativeMediaView} is empty.
     *
     * @param nativeMediaView {@link NativeMediaView} that should be filled by image or video assets
     * @return {@code true} if configureMediaView was overriden, otherwise false and
     * {@link NativeMediaView} will be configured by default.
     */
    public boolean configureMediaView(@NonNull NativeMediaView nativeMediaView) {
        return false;
    }

    /**
     * @return {@link View} that provides DAA(Digital Advertising Alliances) from the AdNetwork
     */
    @Nullable
    public View configureProviderView() {
        return null;
    }

    /**
     * Method to register user interaction to native ad
     *
     * @param container       container that contains all necessary views to show native ad
     * @param iconView        {@link android.widget.ImageView} which is filled with an icon asset
     * @param nativeMediaView {@link NativeMediaView} which is filled with an image or video asset
     * @param clickableViews  set of views that are clickable
     */
    public void registerNative(@NonNull View container,
                               @Nullable ImageView iconView,
                               @Nullable NativeMediaView nativeMediaView,
                               @Nullable Set<View> clickableViews) {

    }

    /**
     * Method to unregister user interaction from native ad
     */
    public void unregisterNative() {

    }

    /**
     * Method to destroy AdNetwork components
     */
    public void destroy() {

    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public float getRating() {
        return -1;
    }

    @Nullable
    @Override
    public String getIconUrl() {
        return null;
    }

    @Nullable
    @Override
    public String getImageUrl() {
        return null;
    }

    @Nullable
    @Override
    public String getClickUrl() {
        return null;
    }

    @Nullable
    @Override
    public String getVideoUrl() {
        return null;
    }

    @Nullable
    @Override
    public String getVideoAdm() {
        return null;
    }

    @Override
    public boolean hasVideo() {
        return false;
    }

}
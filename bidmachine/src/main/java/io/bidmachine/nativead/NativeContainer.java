package io.bidmachine.nativead;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import java.util.Set;

import io.bidmachine.nativead.view.NativeMediaView;

public interface NativeContainer {

    /**
     * Gets provider view, that should be shown with ad
     *
     * @return provider view
     */
    @Nullable
    View getProviderView(Context context);

    /**
     * Registers native ad for interaction
     *
     * @param nativeAdView    container that contains all necessary views to show native ad
     * @param iconView        {@link ViewGroup} or {@link android.widget.ImageView} to fill in with an icon asset
     * @param nativeMediaView {@link NativeMediaView} to fill in with an image asset or video asset
     * @param clickableViews  set of views that should be clickable
     */
    void registerView(@Nullable ViewGroup nativeAdView,
                      @Nullable View iconView,
                      @Nullable NativeMediaView nativeMediaView,
                      @Nullable Set<View> clickableViews);

    /**
     * Unregisters registered view from receiving Ad interactions
     * (see {@link NativeContainer#registerView(ViewGroup, View, NativeMediaView, Set)}
     */
    void unregisterView();

    /**
     * @return {@code true} if Ad has registered view to handle interactions
     */
    boolean isViewRegistered();

}

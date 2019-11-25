package io.bidmachine.nativead;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import java.util.Set;

import io.bidmachine.nativead.view.NativeMediaView;

public interface NativeContainer {

    /**
     * Get provider view, that must be shown with ad
     *
     * @return provider view
     */
    @Nullable
    View getProviderView(Context context);

    /**
     * Register native ad for interaction
     *
     * @param nativeAdView    container that contains all views necessary for show native ad
     * @param iconView        {@link ViewGroup} or {@link android.widget.ImageView} to fill in with a icon asset
     * @param nativeMediaView {@link NativeMediaView} to fill in with a image or video assets
     * @param clickableViews  set of views that must be clickable
     */
    void registerView(@Nullable ViewGroup nativeAdView,
                      @Nullable View iconView,
                      @Nullable NativeMediaView nativeMediaView,
                      @Nullable Set<View> clickableViews);

    /**
     * Unregister registered view from receive Ad interactions
     * (see {@link NativeContainer#registerView(ViewGroup, View, NativeMediaView, Set)}
     */
    void unregisterView();

    /**
     * @return {@code true} if Ad has registered view for handle interactions
     */
    boolean isViewRegistered();

}

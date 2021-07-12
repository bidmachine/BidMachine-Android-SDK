package io.bidmachine.nativead;

import androidx.annotation.Nullable;

public interface NativeData extends NativePublicData {

    /**
     * @return URL of icon that should be cached and displayed in icon view.
     */
    @Nullable
    String getIconUrl();

    /**
     * @return URL of image that should be cached and displayed in {@link io.bidmachine.nativead.view.NativeMediaView}.
     */
    @Nullable
    String getImageUrl();

    /**
     * @return URL that should be opened by browser after click.
     */
    @Nullable
    String getClickUrl();

    /**
     * @return URL of video that should be cached and displayed in {@link io.bidmachine.nativead.view.NativeMediaView}.
     */
    @Nullable
    String getVideoUrl();

    /**
     * @return Video creative that should be cached and displayed in {@link io.bidmachine.nativead.view.NativeMediaView}.
     */
    @Nullable
    String getVideoAdm();

}
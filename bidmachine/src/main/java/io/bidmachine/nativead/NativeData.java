package io.bidmachine.nativead;

import android.support.annotation.Nullable;

public interface NativeData extends NativePublicData {

    /**
     * @return url of icon that should be cached and displayed in icon view
     */
    @Nullable
    String getIconUrl();

    /**
     * @return url of image that should be cached and displayed in {@link io.bidmachine.nativead.view.NativeMediaView}
     */
    @Nullable
    String getImageUrl();

    /**
     * @return url that should be opened by browser after click
     */
    @Nullable
    String getClickUrl();

    /**
     * @return url of video that should be cached and displayed in {@link io.bidmachine.nativead.view.NativeMediaView}
     */
    @Nullable
    String getVideoUrl();

    /**
     * @return video creative that should be cached and displayed in {@link io.bidmachine.nativead.view.NativeMediaView}
     */
    @Nullable
    String getVideoAdm();

}
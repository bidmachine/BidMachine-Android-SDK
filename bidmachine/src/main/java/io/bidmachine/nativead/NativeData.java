package io.bidmachine.nativead;

import android.support.annotation.Nullable;

public interface NativeData extends NativePublicData {

    /**
     * @return icon url that should be cached and display in icon view
     */
    @Nullable
    String getIconUrl();

    /**
     * @return image url that should be cached and display in {@link io.bidmachine.nativead.view.NativeMediaView}
     */
    @Nullable
    String getImageUrl();

    /**
     * @return url with which the browser should open after clicking
     */
    @Nullable
    String getClickUrl();

    /**
     * @return video url that should be cached and display in {@link io.bidmachine.nativead.view.NativeMediaView}
     */
    @Nullable
    String getVideoUrl();

    /**
     * @return video adm that should be cached and display in {@link io.bidmachine.nativead.view.NativeMediaView}
     */
    @Nullable
    String getVideoAdm();

}
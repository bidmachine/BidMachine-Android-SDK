package io.bidmachine.nativead;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;

public interface NativeMediaPublicData {

    @Nullable
    Uri getIconUri();

    @Nullable
    Bitmap getIconBitmap();

    @Nullable
    Uri getImageUri();

    @Nullable
    Bitmap getImageBitmap();

    @Nullable
    Uri getVideoUri();

}
package io.bidmachine.nativead;

import android.graphics.Bitmap;
import android.net.Uri;

import com.explorestack.iab.vast.VastRequest;

public interface NativeMediaPrivateData extends NativeMediaPublicData {

    void setIconUri(Uri uri);

    void setIconBitmap(Bitmap bitmap);

    void setImageUri(Uri uri);

    void setImageBitmap(Bitmap bitmap);

    void setVideoUri(Uri uri);

    void setVastRequest(VastRequest vastModel);

    VastRequest getVastRequest();

}
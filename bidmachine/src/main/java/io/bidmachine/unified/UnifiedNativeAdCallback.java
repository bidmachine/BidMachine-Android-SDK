package io.bidmachine.unified;

import android.support.annotation.NonNull;

import io.bidmachine.nativead.NativeNetworkAdapter;

public interface UnifiedNativeAdCallback extends UnifiedAdCallback {

    void onAdLoaded(@NonNull NativeNetworkAdapter nativeNetworkAdapter);

}

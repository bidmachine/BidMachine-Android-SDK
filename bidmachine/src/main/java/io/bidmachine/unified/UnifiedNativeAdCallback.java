package io.bidmachine.unified;

import androidx.annotation.NonNull;

import io.bidmachine.nativead.NativeNetworkAdapter;

public interface UnifiedNativeAdCallback extends UnifiedAdCallback {

    void onAdLoaded(@NonNull NativeNetworkAdapter nativeNetworkAdapter);

}
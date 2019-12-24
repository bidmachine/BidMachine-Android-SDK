package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

import io.bidmachine.unified.UnifiedAdRequestParams;
import io.bidmachine.unified.UnifiedNativeAdRequestParams;

class NetworkServiceRegistry {

    private static Set<NetworkService> networkServiceSet;

    static void registerService(NetworkService networkService) {
        if (networkServiceSet == null) {
            networkServiceSet = new HashSet<>();
        }
        networkServiceSet.add(networkService);
    }

    static void notifyInitSuccess(@NonNull String data,
                                  @NonNull UnifiedAdRequestParams adRequestParams) {
        if (networkServiceSet == null) {
            return;
        }
        for (NetworkService networkService : networkServiceSet) {
            networkService.initialize(data, adRequestParams);
        }
    }

}
package io.bidmachine.test.app;

import androidx.annotation.NonNull;

import java.util.UUID;

import io.bidmachine.NetworkConfig;

class OptionalNetwork {

    @NonNull
    final String displayName;
    @NonNull
    final NetworkConfig networkConfig;
    @NonNull
    final String assetFile;

    private final int hashCode;

    OptionalNetwork(@NonNull String displayName,
                    @NonNull NetworkConfig networkConfig,
                    @NonNull String assetFile) {
        this.displayName = displayName;
        this.networkConfig = networkConfig;
        this.assetFile = assetFile;
        this.hashCode = UUID.randomUUID().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OptionalNetwork network = (OptionalNetwork) o;
        return hashCode == network.hashCode;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

}
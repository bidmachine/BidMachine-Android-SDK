package io.bidmachine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

public class TestNetworkConfig extends NetworkConfig {

    private final String key;

    TestNetworkConfig() {
        this(null, null);
    }

    TestNetworkConfig(@Nullable Map<String, String> networkParams) {
        this(null, networkParams);
    }

    TestNetworkConfig(String key, @Nullable Map<String, String> networkParams) {
        super(networkParams);
        this.key = key;
    }

    @NonNull
    @Override
    protected NetworkAdapter createNetworkAdapter() {
        return new TestNetworkAdapter();
    }

    @NonNull
    @Override
    public String getKey() {
        return key;
    }

}
package io.bidmachine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

public class TestNetworkConfig extends NetworkConfig {

    private static final String DEFAULT_KEY = "test_network";

    private final String key;

    public TestNetworkConfig() {
        this(DEFAULT_KEY, null);
    }

    public TestNetworkConfig(@Nullable Map<String, String> networkParams) {
        this(DEFAULT_KEY, networkParams);
    }

    public TestNetworkConfig(String key, @Nullable Map<String, String> networkParams) {
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
package io.bidmachine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

public class TestNetworkConfig extends NetworkConfig {

    TestNetworkConfig() {
        super(null);
    }

    TestNetworkConfig(@Nullable Map<String, String> networkParams) {
        super(networkParams);
    }

    @NonNull
    @Override
    protected NetworkAdapter createNetworkAdapter() {
        return new TestNetworkAdapter();
    }

}
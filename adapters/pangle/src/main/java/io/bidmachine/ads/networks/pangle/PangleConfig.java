package io.bidmachine.ads.networks.pangle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.AdsFormat;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfig;

public class PangleConfig extends NetworkConfig {

    static final String KEY_APP_ID = "app_id";
    static final String KEY_SLOT_ID = "slot_id";
    static final String KEY_BID_TOKEN = "network_bid_token";
    static final String KEY_BID_PAYLOAD = "bid_payload";

    public PangleConfig(@NonNull final String appId) {
        super(new HashMap<String, String>() {{
            put(KEY_APP_ID, appId);
        }});
    }

    @SuppressWarnings("unused")
    public PangleConfig(@Nullable Map<String, String> networkConfig) {
        super(networkConfig);
    }

    @NonNull
    @Override
    protected NetworkAdapter createNetworkAdapter() {
        return new PangleAdapter();
    }

    @SuppressWarnings("WeakerAccess")
    public PangleConfig withMediationConfig(@NonNull AdsFormat format,
                                            @NonNull final String slotId) {
        return withMediationConfig(format, new HashMap<String, String>() {{
            put(KEY_SLOT_ID, slotId);
        }});
    }

}
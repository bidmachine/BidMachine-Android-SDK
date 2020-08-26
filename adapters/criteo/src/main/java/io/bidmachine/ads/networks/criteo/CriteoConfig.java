package io.bidmachine.ads.networks.criteo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.AdsFormat;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.NetworkConfig;
import io.bidmachine.Orientation;

public class CriteoConfig extends NetworkConfig {

    static final String PUBLISHER_ID = "publisher_id";
    static final String AD_UNIT_ID = "ad_unit_id";
    static final String PRICE = "price";

    public CriteoConfig(final String publisherId) {
        super(new HashMap<String, String>() {{
            put(PUBLISHER_ID, publisherId);
        }});
    }

    public CriteoConfig(@Nullable Map<String, String> networkParams) {
        super(networkParams);
    }

    @NonNull
    @Override
    protected NetworkAdapter createNetworkAdapter() {
        return new CriteoAdapter();
    }

    public CriteoConfig withMediationConfig(@NonNull AdsFormat adsFormat,
                                            @NonNull final String adUnitId) {
        return withMediationConfig(adsFormat, adUnitId, null);
    }

    public CriteoConfig withMediationConfig(@NonNull AdsFormat adsFormat,
                                            @NonNull final String adUnitId,
                                            @Nullable Orientation orientation) {
        return withMediationConfig(
                adsFormat,
                new HashMap<String, String>() {{
                    put(AD_UNIT_ID, adUnitId);
                }},
                orientation);
    }

}

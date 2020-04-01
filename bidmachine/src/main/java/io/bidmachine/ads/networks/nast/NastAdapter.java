package io.bidmachine.ads.networks.nast;

import io.bidmachine.AdsType;
import io.bidmachine.BuildConfig;
import io.bidmachine.NetworkAdapter;
import io.bidmachine.unified.UnifiedNativeAd;

public class NastAdapter extends NetworkAdapter {

    public static final String KEY = "nast";

    public NastAdapter() {
        super(KEY,
                "1.0",
                BuildConfig.VERSION_NAME + ".1",
                new AdsType[]{AdsType.Native});
    }

    @Override
    public UnifiedNativeAd createNativeAd() {
        return new NastNative();
    }

}
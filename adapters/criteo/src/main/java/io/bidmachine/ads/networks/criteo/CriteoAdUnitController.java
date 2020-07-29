package io.bidmachine.ads.networks.criteo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.criteo.publisher.model.AdSize;
import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;
import com.criteo.publisher.model.InterstitialAdUnit;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.bidmachine.AdsFormat;
import io.bidmachine.NetworkConfigParams;

class CriteoAdUnitController {

    private static final Map<String, AdUnit> adUnitMap = new HashMap<>();

    @Nullable
    static List<AdUnit> extractAdUnits(@NonNull NetworkConfigParams networkConfigParams) {
        EnumMap<AdsFormat, List<Map<String, String>>> networkMediationConfigs =
                networkConfigParams.obtainNetworkMediationConfigs(AdsFormat.values());
        if (networkMediationConfigs == null) {
            return null;
        }
        List<AdUnit> adUnitList = new ArrayList<>();
        for (Map.Entry<AdsFormat, List<Map<String, String>>> entry : networkMediationConfigs.entrySet()) {
            AdsFormat adsFormat = entry.getKey();
            if (adsFormat == null) {
                continue;
            }
            List<Map<String, String>> configList = entry.getValue();
            if (configList == null) {
                continue;
            }
            for (Map<String, String> config : configList) {
                String adUnitId = config.get(CriteoConfig.AD_UNIT_ID);
                if (TextUtils.isEmpty(adUnitId)) {
                    continue;
                }
                assert adUnitId != null;
                AdUnit adUnit = null;
                switch (adsFormat) {
                    case Banner:
                    case Banner_320x50:
                        adUnit = new BannerAdUnit(adUnitId, new AdSize(320, 50));
                        break;
                    case Banner_300x250:
                        adUnit = new BannerAdUnit(adUnitId, new AdSize(300, 250));
                        break;
                    case Banner_728x90:
                        adUnit = new BannerAdUnit(adUnitId, new AdSize(728, 90));
                        break;
                    case Interstitial:
                    case InterstitialStatic:
                    case InterstitialVideo:
                        adUnit = new InterstitialAdUnit(adUnitId);
                        break;
                }
                if (adUnit != null) {
                    adUnitMap.put(adUnitId, adUnit);
                    adUnitList.add(adUnit);
                }
            }
        }
        return adUnitList;
    }

    @Nullable
    static AdUnit getAdUnit(@Nullable String adUnitId) {
        if (TextUtils.isEmpty(adUnitId)) {
            return null;
        }
        return adUnitMap.get(adUnitId);
    }

    @VisibleForTesting
    static Map<String, AdUnit> getAdUnitMap() {
        return adUnitMap;
    }

}
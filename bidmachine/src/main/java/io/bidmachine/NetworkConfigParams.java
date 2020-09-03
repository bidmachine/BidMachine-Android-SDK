package io.bidmachine;

import androidx.annotation.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public interface NetworkConfigParams {

    @Nullable
    Map<String, String> obtainNetworkParams();

    @Nullable
    EnumMap<AdsFormat, List<Map<String, String>>> obtainNetworkMediationConfigs(@Nullable AdsFormat... adsFormats);

}

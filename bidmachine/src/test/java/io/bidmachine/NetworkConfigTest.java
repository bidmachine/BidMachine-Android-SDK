package io.bidmachine;

import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.bidmachine.banner.BannerSize;
import io.bidmachine.unified.UnifiedBannerAdRequestParams;
import io.bidmachine.unified.UnifiedFullscreenAdRequestParams;
import io.bidmachine.unified.UnifiedNativeAdRequestParams;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class NetworkConfigTest {

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
    }

    @Test
    public void constructor_params() {
        HashMap<String, String> params = new HashMap<>();
        params.put("TestKey", "TestValue");
        NetworkConfig networkConfig = new TestNetworkConfig(params);

        Map<String, String> networkParams = networkConfig.getNetworkConfigParams()
                .obtainNetworkParams();
        assertNotNull(networkParams);
        assertEquals("TestValue", networkParams.get("TestKey"));
    }

    @Test
    public void constructor_mediationParamsFromNetworkParams() {
        HashMap<String, String> params = new HashMap<>();
        params.put("TestKey", "TestValue");
        NetworkConfig networkConfig = new TestNetworkConfig(params);
        networkConfig.withMediationConfig(AdsFormat.Banner, new HashMap<String, String>());

        EnumMap<AdsFormat, List<Map<String, String>>> mediationConfig = networkConfig.getNetworkConfigParams()
                .obtainNetworkMediationConfigs(AdsFormat.Banner);
        assertNotNull(mediationConfig);

        List<Map<String, String>> configList = mediationConfig.get(AdsFormat.Banner);
        assertNotNull(configList);
        assertEquals(1, configList.size());

        Map<String, String> config = configList.get(0);
        assertNotNull(config);
        assertEquals("TestValue", config.get("TestKey"));
    }

    @Test
    public void constructor_mediationParamsNetworkParamsOverriding() {
        HashMap<String, String> params = new HashMap<>();
        params.put("TestKey", "TestValue");
        NetworkConfig networkConfig = new TestNetworkConfig(params);
        networkConfig.withMediationConfig(AdsFormat.Banner, new HashMap<String, String>() {{
            put("TestKey", "TestValue2");
        }});

        EnumMap<AdsFormat, List<Map<String, String>>> mediationConfig = networkConfig.getNetworkConfigParams()
                .obtainNetworkMediationConfigs(AdsFormat.Banner);
        assertNotNull(mediationConfig);

        List<Map<String, String>> configList = mediationConfig.get(AdsFormat.Banner);
        assertNotNull(configList);
        assertEquals(1, configList.size());

        Map<String, String> config = configList.get(0);
        assertNotNull(config);
        assertEquals("TestValue2", config.get("TestKey"));
    }

    @Test
    public void constructor_mediationParamsBaseMediationParamsOverriding() {
        NetworkConfig networkConfig = new TestNetworkConfig(null);
        networkConfig.withBaseMediationConfig(new HashMap<String, String>() {{
            put("TestKey", "TestValue");
        }});
        networkConfig.withMediationConfig(AdsFormat.Banner, new HashMap<String, String>() {{
            put("TestKey", "TestValue2");
        }});

        EnumMap<AdsFormat, List<Map<String, String>>> mediationConfig = networkConfig.getNetworkConfigParams()
                .obtainNetworkMediationConfigs(AdsFormat.Banner);
        assertNotNull(mediationConfig);

        List<Map<String, String>> configList = mediationConfig.get(AdsFormat.Banner);
        assertNotNull(configList);
        assertEquals(1, configList.size());

        Map<String, String> config = configList.get(0);
        assertNotNull(config);
        assertEquals("TestValue2", config.get("TestKey"));
    }

    @Test
    public void constructor_mediationParamsBaseMediationParamsNetworkParamsOverriding() {
        HashMap<String, String> params = new HashMap<>();
        params.put("TestKey", "TestValue");
        NetworkConfig networkConfig = new TestNetworkConfig(params);
        networkConfig.withBaseMediationConfig(new HashMap<String, String>() {{
            put("TestKey", "TestValue2");
        }});
        networkConfig.withMediationConfig(AdsFormat.Banner, new HashMap<String, String>() {{
            put("TestKey", "TestValue3");
        }});

        EnumMap<AdsFormat, List<Map<String, String>>> mediationConfig = networkConfig.getNetworkConfigParams()
                .obtainNetworkMediationConfigs(AdsFormat.Banner);
        assertNotNull(mediationConfig);

        List<Map<String, String>> configList = mediationConfig.get(AdsFormat.Banner);
        assertNotNull(configList);
        assertEquals(1, configList.size());

        Map<String, String> config = configList.get(0);
        assertNotNull(config);
        assertEquals("TestValue3", config.get("TestKey"));
    }

    @Test
    public void baseMediationParamsMerging() {
        NetworkConfig networkConfig = new TestNetworkConfig(null);
        networkConfig.withBaseMediationConfig(new HashMap<String, String>() {{
            put("TestKey1", "TestValue1");
        }});
        networkConfig.withMediationConfig(AdsFormat.Banner, new HashMap<String, String>() {{
            put("TestKey2", "TestValue2");
        }});

        EnumMap<AdsFormat, List<Map<String, String>>> mediationConfig = networkConfig.getNetworkConfigParams()
                .obtainNetworkMediationConfigs(AdsFormat.Banner);
        assertNotNull(mediationConfig);

        List<Map<String, String>> configList = mediationConfig.get(AdsFormat.Banner);
        assertNotNull(configList);
        assertEquals(1, configList.size());

        Map<String, String> config = configList.get(0);
        assertNotNull(config);
        assertEquals("TestValue1", config.get("TestKey1"));
        assertEquals("TestValue2", config.get("TestKey2"));
    }

    @Test
    public void mediationConfigsContainsOnlyRequiredTypes() {
        NetworkConfig networkConfig = new TestNetworkConfig(null);
        networkConfig.withMediationConfig(AdsFormat.Banner_320x50, new HashMap<String, String>() {{
            put("TestKey", "TestValueBanner");
        }});
        networkConfig.withMediationConfig(AdsFormat.InterstitialStatic,
                                          new HashMap<String, String>() {{
                                              put("TestKey", "TestValueInterstitialStatic");
                                          }});
        networkConfig.withMediationConfig(AdsFormat.RewardedStatic, new HashMap<String, String>() {{
            put("TestKey", "TestValueRewardedStatic");
        }});


        EnumMap<AdsFormat, List<Map<String, String>>> mediationConfig = networkConfig.getNetworkConfigParams()
                .obtainNetworkMediationConfigs(AdsFormat.values());
        assertNotNull(mediationConfig);
        assertEquals(3, mediationConfig.size());

        List<Map<String, String>> configList = mediationConfig.get(AdsFormat.Banner_320x50);
        assertNotNull(configList);
        assertEquals(1, configList.size());

        Map<String, String> config = configList.get(0);
        assertNotNull(config);
        assertEquals("TestValueBanner", config.get("TestKey"));

        configList = mediationConfig.get(AdsFormat.InterstitialStatic);
        assertNotNull(configList);
        assertEquals(1, configList.size());

        config = configList.get(0);
        assertNotNull(config);
        assertEquals("TestValueInterstitialStatic", config.get("TestKey"));

        configList = mediationConfig.get(AdsFormat.RewardedStatic);
        assertNotNull(configList);
        assertEquals(1, configList.size());

        config = configList.get(0);
        assertNotNull(config);
        assertEquals("TestValueRewardedStatic", config.get("TestKey"));
    }

    @Test
    public void mediationConfigsMergingDuringPeek() {
        NetworkConfig networkConfig = new TestNetworkConfig(null);
        networkConfig.withMediationConfig(AdsFormat.Banner, new HashMap<String, String>() {{
            put("TestKey", "TestValueBanner");
        }});
        networkConfig.withMediationConfig(AdsFormat.Interstitial, new HashMap<String, String>() {{
            put("TestKey", "TestValueInterstitialStatic");
        }});
        networkConfig.withMediationConfig(AdsFormat.Rewarded, new HashMap<String, String>() {{
            put("TestKey", "TestValueRewardedStatic");
        }});
        networkConfig.withMediationConfig(AdsFormat.Native, new HashMap<String, String>() {{
            put("TestKey", "TestValueNative");
        }});
        UnifiedBannerAdRequestParams bannerRequestParams = mock(UnifiedBannerAdRequestParams.class);
        doReturn(BannerSize.Size_320x50).when(bannerRequestParams).getBannerSize();
        assertEquals("TestValueBanner",
                     networkConfig.peekMediationConfig(AdsType.Banner,
                                                       bannerRequestParams,
                                                       AdContentType.All)
                             .get("TestKey"));
        doReturn(BannerSize.Size_300x250).when(bannerRequestParams).getBannerSize();
        assertEquals("TestValueBanner",
                     networkConfig.peekMediationConfig(AdsType.Banner,
                                                       bannerRequestParams,
                                                       AdContentType.All)
                             .get("TestKey"));
        doReturn(BannerSize.Size_728x90).when(bannerRequestParams).getBannerSize();
        assertEquals("TestValueBanner",
                     networkConfig.peekMediationConfig(AdsType.Banner,
                                                       bannerRequestParams,
                                                       AdContentType.All)
                             .get("TestKey"));
        UnifiedFullscreenAdRequestParams fullscreenAdRequestParams = mock(
                UnifiedFullscreenAdRequestParams.class);
        assertEquals("TestValueInterstitialStatic",
                     networkConfig.peekMediationConfig(AdsType.Interstitial,
                                                       fullscreenAdRequestParams,
                                                       AdContentType.All)
                             .get("TestKey"));
        assertEquals("TestValueInterstitialStatic",
                     networkConfig.peekMediationConfig(AdsType.Interstitial,
                                                       fullscreenAdRequestParams,
                                                       AdContentType.Video)
                             .get("TestKey"));
        assertEquals("TestValueInterstitialStatic",
                     networkConfig.peekMediationConfig(AdsType.Interstitial,
                                                       fullscreenAdRequestParams,
                                                       AdContentType.Static)
                             .get("TestKey"));
        assertEquals("TestValueRewardedStatic",
                     networkConfig.peekMediationConfig(AdsType.Rewarded,
                                                       fullscreenAdRequestParams,
                                                       AdContentType.All)
                             .get("TestKey"));
        assertEquals("TestValueRewardedStatic",
                     networkConfig.peekMediationConfig(AdsType.Rewarded,
                                                       fullscreenAdRequestParams,
                                                       AdContentType.Video)
                             .get("TestKey"));
        assertEquals("TestValueRewardedStatic",
                     networkConfig.peekMediationConfig(AdsType.Rewarded,
                                                       fullscreenAdRequestParams,
                                                       AdContentType.Static)
                             .get("TestKey"));
        UnifiedNativeAdRequestParams nativeAdRequestParams = mock(UnifiedNativeAdRequestParams.class);
        assertEquals("TestValueNative",
                     networkConfig.peekMediationConfig(AdsType.Native,
                                                       nativeAdRequestParams,
                                                       AdContentType.All)
                             .get("TestKey"));
    }

    @Test
    public void mediationConfigsNotMergingDuringPeekForSpecifiedFormats() {
        NetworkConfig networkConfig = new TestNetworkConfig(null);
        networkConfig.withMediationConfig(AdsFormat.Banner_320x50, new HashMap<String, String>() {{
            put("TestKey", "TestValueBanner320");
        }});
        networkConfig.withMediationConfig(AdsFormat.Banner_300x250, new HashMap<String, String>() {{
            put("TestKey", "TestValueBanner300");
        }});
        networkConfig.withMediationConfig(AdsFormat.Banner_728x90, new HashMap<String, String>() {{
            put("TestKey", "TestValueBanner728");
        }});
        networkConfig.withMediationConfig(AdsFormat.InterstitialVideo,
                                          new HashMap<String, String>() {{
                                              put("TestKey", "TestValueInterstitialVideo");
                                          }});
        networkConfig.withMediationConfig(AdsFormat.InterstitialStatic,
                                          new HashMap<String, String>() {{
                                              put("TestKey", "TestValueInterstitialStatic");
                                          }});
        networkConfig.withMediationConfig(AdsFormat.RewardedVideo, new HashMap<String, String>() {{
            put("TestKey", "TestValueRewardedVideo");
        }});
        networkConfig.withMediationConfig(AdsFormat.RewardedStatic, new HashMap<String, String>() {{
            put("TestKey", "TestValueRewardedStatic");
        }});
        UnifiedBannerAdRequestParams bannerRequestParams = mock(UnifiedBannerAdRequestParams.class);
        doReturn(BannerSize.Size_320x50).when(bannerRequestParams).getBannerSize();
        assertEquals("TestValueBanner320",
                     networkConfig.peekMediationConfig(AdsType.Banner,
                                                       bannerRequestParams,
                                                       AdContentType.All)
                             .get("TestKey"));
        doReturn(BannerSize.Size_300x250).when(bannerRequestParams).getBannerSize();
        assertEquals("TestValueBanner300",
                     networkConfig.peekMediationConfig(AdsType.Banner,
                                                       bannerRequestParams,
                                                       AdContentType.All)
                             .get("TestKey"));
        doReturn(BannerSize.Size_728x90).when(bannerRequestParams).getBannerSize();
        assertEquals("TestValueBanner728",
                     networkConfig.peekMediationConfig(AdsType.Banner,
                                                       bannerRequestParams,
                                                       AdContentType.All)
                             .get("TestKey"));
        UnifiedFullscreenAdRequestParams fullscreenAdRequestParams = mock(
                UnifiedFullscreenAdRequestParams.class);
        assertNull(networkConfig.peekMediationConfig(AdsType.Interstitial,
                                                     fullscreenAdRequestParams,
                                                     AdContentType.All));
        assertEquals("TestValueInterstitialVideo",
                     networkConfig.peekMediationConfig(AdsType.Interstitial,
                                                       fullscreenAdRequestParams,
                                                       AdContentType.Video)
                             .get("TestKey"));
        assertEquals("TestValueInterstitialStatic",
                     networkConfig.peekMediationConfig(AdsType.Interstitial,
                                                       fullscreenAdRequestParams,
                                                       AdContentType.Static)
                             .get("TestKey"));
        assertNull(networkConfig.peekMediationConfig(AdsType.Rewarded,
                                                     fullscreenAdRequestParams,
                                                     AdContentType.All));
        assertEquals("TestValueRewardedVideo",
                     networkConfig.peekMediationConfig(AdsType.Rewarded,
                                                       fullscreenAdRequestParams,
                                                       AdContentType.Video)
                             .get("TestKey"));
        assertEquals("TestValueRewardedStatic",
                     networkConfig.peekMediationConfig(AdsType.Rewarded,
                                                       fullscreenAdRequestParams,
                                                       AdContentType.Static)
                             .get("TestKey"));
    }

    @Test
    public void withMediationConfig_obtainNetworkMediationConfigs() {
        TestNetworkConfig networkConfig = new TestNetworkConfig(null);
        networkConfig.withMediationConfig(
                AdsFormat.Interstitial,
                new HashMap<String, String>() {{
                    put("test_key1", "test_value1");
                }},
                Orientation.Portrait);
        networkConfig.withMediationConfig(
                AdsFormat.Interstitial,
                new HashMap<String, String>() {{
                    put("test_key2", "test_value2");
                }},
                Orientation.Landscape);
        networkConfig.withMediationConfig(
                AdsFormat.Interstitial,
                new HashMap<String, String>() {{
                    put("test_key3", "test_value3");
                }},
                Orientation.Landscape);

        EnumMap<AdsFormat, List<Map<String, String>>> mediationConfig = networkConfig.getNetworkConfigParams()
                .obtainNetworkMediationConfigs(AdsFormat.values());
        assertNotNull(mediationConfig);
        assertEquals(1, mediationConfig.size());

        List<Map<String, String>> configList = mediationConfig.get(AdsFormat.Interstitial);
        assertNotNull(configList);
        assertEquals(3, configList.size());

        Map<String, String> config = configList.get(0);
        assertEquals("test_value1", config.get("test_key1"));
        assertEquals("portrait", config.get(NetworkConfig.CONFIG_ORIENTATION));

        config = configList.get(1);
        assertEquals("test_value2", config.get("test_key2"));
        assertEquals("landscape", config.get(NetworkConfig.CONFIG_ORIENTATION));

        config = configList.get(2);
        assertEquals("test_value3", config.get("test_key3"));
        assertEquals("landscape", config.get(NetworkConfig.CONFIG_ORIENTATION));
    }

    @Test
    public void withMediationConfig_peekMediationConfig() {
        TestNetworkConfig networkConfig = new TestNetworkConfig(null);
        networkConfig.withMediationConfig(
                AdsFormat.Interstitial,
                new HashMap<String, String>() {{
                    put("test_key1", "test_value1");
                }});
        networkConfig.withMediationConfig(
                AdsFormat.Interstitial,
                new HashMap<String, String>() {{
                    put("test_key2", "test_value2");
                }},
                Orientation.Portrait);
        networkConfig.withMediationConfig(
                AdsFormat.Interstitial,
                new HashMap<String, String>() {{
                    put("test_key3", "test_value3");
                }},
                Orientation.Portrait);
        networkConfig.withMediationConfig(
                AdsFormat.Interstitial,
                new HashMap<String, String>() {{
                    put("test_key4", "test_value4");
                }},
                Orientation.Landscape);
        networkConfig.withMediationConfig(
                AdsFormat.Interstitial,
                new HashMap<String, String>() {{
                    put("test_key5", "test_value5");
                }},
                Orientation.Landscape);
        BidMachineImpl.get().appContext = context;

        context.getResources().getConfiguration().orientation = Configuration.ORIENTATION_UNDEFINED;
        Map<String, String> mediationConfig = networkConfig.peekMediationConfig(
                AdsType.Interstitial,
                mock(UnifiedFullscreenAdRequestParams.class),
                AdContentType.All);
        assertNotNull(mediationConfig);
        assertEquals("test_value1", mediationConfig.get("test_key1"));
        assertNull(mediationConfig.get(NetworkConfig.CONFIG_ORIENTATION));

        context.getResources().getConfiguration().orientation = Configuration.ORIENTATION_PORTRAIT;
        mediationConfig = networkConfig.peekMediationConfig(
                AdsType.Interstitial,
                mock(UnifiedFullscreenAdRequestParams.class),
                AdContentType.All);
        assertNotNull(mediationConfig);
        assertEquals("test_value3", mediationConfig.get("test_key3"));
        assertEquals("portrait", mediationConfig.get(NetworkConfig.CONFIG_ORIENTATION));

        context.getResources().getConfiguration().orientation = Configuration.ORIENTATION_LANDSCAPE;
        mediationConfig = networkConfig.peekMediationConfig(
                AdsType.Interstitial,
                mock(UnifiedFullscreenAdRequestParams.class),
                AdContentType.All);
        assertNotNull(mediationConfig);
        assertEquals("test_value5", mediationConfig.get("test_key5"));
        assertEquals("landscape", mediationConfig.get(NetworkConfig.CONFIG_ORIENTATION));
    }

    @Test
    public void isOrientationMatched() {
        NetworkConfig networkConfig = new TestNetworkConfig(null);

        boolean result = networkConfig.isOrientationMatched(null);
        assertTrue(result);

        result = networkConfig.isOrientationMatched(new HashMap<String, String>());
        assertTrue(result);

        result = networkConfig.isOrientationMatched(new HashMap<String, String>() {{
            put(NetworkConfig.CONFIG_ORIENTATION, "test_value");
        }});
        assertTrue(result);

        result = networkConfig.isOrientationMatched(new HashMap<String, String>() {{
            put(NetworkConfig.CONFIG_ORIENTATION, Orientation.Undefined.toString());
        }});
        assertTrue(result);

        BidMachineImpl.get().appContext = context;
        context.getResources().getConfiguration().orientation = Configuration.ORIENTATION_PORTRAIT;
        result = networkConfig.isOrientationMatched(new HashMap<String, String>() {{
            put(NetworkConfig.CONFIG_ORIENTATION, Orientation.Portrait.toString());
        }});
        assertTrue(result);

        context.getResources().getConfiguration().orientation = Configuration.ORIENTATION_LANDSCAPE;
        result = networkConfig.isOrientationMatched(new HashMap<String, String>() {{
            put(NetworkConfig.CONFIG_ORIENTATION, Orientation.Landscape.toString());
        }});
        assertTrue(result);

        context.getResources().getConfiguration().orientation = Configuration.ORIENTATION_LANDSCAPE;
        result = networkConfig.isOrientationMatched(new HashMap<String, String>() {{
            put(NetworkConfig.CONFIG_ORIENTATION, Orientation.Portrait.toString());
        }});
        assertFalse(result);

        context.getResources().getConfiguration().orientation = Configuration.ORIENTATION_PORTRAIT;
        result = networkConfig.isOrientationMatched(new HashMap<String, String>() {{
            put(NetworkConfig.CONFIG_ORIENTATION, Orientation.Landscape.toString());
        }});
        assertFalse(result);
    }


    static class TestNetworkConfig extends NetworkConfig {

        TestNetworkConfig(@Nullable Map<String, String> networkParams) {
            super(networkParams);
        }

        @NonNull
        @Override
        protected NetworkAdapter createNetworkAdapter() {
            return new TestNetworkAdapter();
        }
    }

    static class TestNetworkAdapter extends NetworkAdapter {

        TestNetworkAdapter() {
            super("TestAdapter", "1", "1", AdsType.values());
        }
    }

}

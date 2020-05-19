package io.bidmachine.ads.networks.criteo;

import android.support.annotation.Nullable;

import com.criteo.publisher.model.AdUnit;
import com.criteo.publisher.model.BannerAdUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.bidmachine.AdsFormat;
import io.bidmachine.NetworkConfigParams;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CriteoAdUnitControllerTest {

    @Before
    public void setUp() throws Exception {
        CriteoAdUnitController.getAdUnitMap().clear();
    }

    @Test
    public void extractAdUnits_obtainedMediationConfigIsNull_resultListIsNull() {
        BidMachineNetworkConfigParams networkConfigParams = new BidMachineNetworkConfigParams(null);
        List<AdUnit> adUnitList = CriteoAdUnitController.extractAdUnits(networkConfigParams);
        assertNull(adUnitList);
        assertNotNull(CriteoAdUnitController.getAdUnitMap());
        assertEquals(0, CriteoAdUnitController.getAdUnitMap().size());
    }

    @Test
    public void extractAdUnits_obtainedMediationConfigIsEmpty_resultListIsEmpty() {
        BidMachineNetworkConfigParams networkConfigParams = new BidMachineNetworkConfigParams();
        networkConfigParams.addMap(AdsFormat.Banner, new HashMap<String, String>() {{
            put("key_1", "ad_unit_id_1");
            put("key_2", "ad_unit_id_2");
            put("key_3", "ad_unit_id_3");
        }});
        List<AdUnit> adUnitList = CriteoAdUnitController.extractAdUnits(networkConfigParams);
        assertNotNull(adUnitList);
        assertEquals(0, adUnitList.size());
        assertNotNull(CriteoAdUnitController.getAdUnitMap());
        assertEquals(0, CriteoAdUnitController.getAdUnitMap().size());
    }

    @Test
    public void extractAdUnits_obtainedMediationConfigContainsAdUnits_resultListIsFull() {
        BidMachineNetworkConfigParams networkConfigParams = new BidMachineNetworkConfigParams();
        networkConfigParams.addMap(AdsFormat.Banner, new HashMap<String, String>() {{
            put(CriteoConfig.AD_UNIT_ID, "ad_unit_banner_id");
        }});
        networkConfigParams.addMap(AdsFormat.Banner_320x50, new HashMap<String, String>() {{
            put(CriteoConfig.AD_UNIT_ID, "ad_unit_banner_320_id");
        }});
        networkConfigParams.addMap(AdsFormat.Banner_300x250, new HashMap<String, String>() {{
            put(CriteoConfig.AD_UNIT_ID, "ad_unit_banner_300_id");
        }});
        networkConfigParams.addMap(AdsFormat.Banner_728x90, new HashMap<String, String>() {{
            put(CriteoConfig.AD_UNIT_ID, "ad_unit_banner_728_id");
        }});
        networkConfigParams.addMap(AdsFormat.Interstitial, new HashMap<String, String>() {{
            put(CriteoConfig.AD_UNIT_ID, "ad_unit_interstitial_id");
        }});
        networkConfigParams.addMap(AdsFormat.InterstitialStatic, new HashMap<String, String>() {{
            put(CriteoConfig.AD_UNIT_ID, "ad_unit_interstitial_static_id");
        }});
        networkConfigParams.addMap(AdsFormat.InterstitialVideo, new HashMap<String, String>() {{
            put(CriteoConfig.AD_UNIT_ID, "ad_unit_interstitial_video_id");
        }});
        List<AdUnit> adUnitList = CriteoAdUnitController.extractAdUnits(networkConfigParams);
        assertNotNull(adUnitList);
        assertEquals(7, adUnitList.size());

        BannerAdUnit bannerAdUnit = findAdUnitFromList(adUnitList, "ad_unit_banner_id");
        assertNotNull(bannerAdUnit);
        assertEquals("ad_unit_banner_id", bannerAdUnit.getAdUnitId());
        assertEquals(320, bannerAdUnit.getSize().getWidth());
        assertEquals(50, bannerAdUnit.getSize().getHeight());

        BannerAdUnit banner320AdUnit = findAdUnitFromList(adUnitList, "ad_unit_banner_320_id");
        assertNotNull(banner320AdUnit);
        assertEquals("ad_unit_banner_320_id", banner320AdUnit.getAdUnitId());
        assertEquals(320, banner320AdUnit.getSize().getWidth());
        assertEquals(50, banner320AdUnit.getSize().getHeight());

        BannerAdUnit banner300AdUnit = findAdUnitFromList(adUnitList, "ad_unit_banner_300_id");
        assertNotNull(banner300AdUnit);
        assertEquals("ad_unit_banner_300_id", banner300AdUnit.getAdUnitId());
        assertEquals(300, banner300AdUnit.getSize().getWidth());
        assertEquals(250, banner300AdUnit.getSize().getHeight());

        BannerAdUnit banner728AdUnit = findAdUnitFromList(adUnitList, "ad_unit_banner_728_id");
        assertNotNull(banner728AdUnit);
        assertEquals("ad_unit_banner_728_id", banner728AdUnit.getAdUnitId());
        assertEquals(728, banner728AdUnit.getSize().getWidth());
        assertEquals(90, banner728AdUnit.getSize().getHeight());

        AdUnit interstitialAdUnit = findAdUnitFromList(adUnitList, "ad_unit_interstitial_id");
        assertNotNull(interstitialAdUnit);
        assertEquals("ad_unit_interstitial_id", interstitialAdUnit.getAdUnitId());

        AdUnit interstitialStaticAdUnit = findAdUnitFromList(adUnitList,
                                                             "ad_unit_interstitial_static_id");
        assertNotNull(interstitialStaticAdUnit);
        assertEquals("ad_unit_interstitial_static_id", interstitialStaticAdUnit.getAdUnitId());

        AdUnit interstitialVideoAdUnit = findAdUnitFromList(adUnitList,
                                                            "ad_unit_interstitial_video_id");
        assertNotNull(interstitialVideoAdUnit);
        assertEquals("ad_unit_interstitial_video_id", interstitialVideoAdUnit.getAdUnitId());
    }

    @Test
    public void getAdUnit_negativeTests() {
        AdUnit adUnit1 = CriteoAdUnitController.getAdUnit(null);
        AdUnit adUnit2 = CriteoAdUnitController.getAdUnit("");
        AdUnit adUnit3 = CriteoAdUnitController.getAdUnit("ad_unit_id");
        assertNull(adUnit1);
        assertNull(adUnit2);
        assertNull(adUnit3);
    }

    @Test
    public void getAdUnit() {
        AdUnit adUnit1 = mock(AdUnit.class);
        AdUnit adUnit2 = mock(AdUnit.class);
        CriteoAdUnitController.getAdUnitMap().put("ad_unit_1", adUnit1);
        CriteoAdUnitController.getAdUnitMap().put("ad_unit_2", adUnit2);
        assertEquals(adUnit1, CriteoAdUnitController.getAdUnit("ad_unit_1"));
        assertEquals(adUnit2, CriteoAdUnitController.getAdUnit("ad_unit_2"));
    }

    @SuppressWarnings("unchecked")
    private <T extends AdUnit> T findAdUnitFromList(List<AdUnit> adUnitList, String adUnitId) {
        for (AdUnit adUnit : adUnitList) {
            if (adUnit.getAdUnitId().equals(adUnitId)) {
                return (T) adUnit;
            }
        }
        return null;
    }

    private static class BidMachineNetworkConfigParams implements NetworkConfigParams {

        private EnumMap<AdsFormat, Map<String, String>> mapEnumMap;

        BidMachineNetworkConfigParams() {
            this(new EnumMap<AdsFormat, Map<String, String>>(AdsFormat.class));
        }

        BidMachineNetworkConfigParams(EnumMap<AdsFormat, Map<String, String>> mapEnumMap) {
            this.mapEnumMap = mapEnumMap;
        }

        void addMap(AdsFormat adsFormat, Map<String, String> map) {
            mapEnumMap.put(adsFormat, map);
        }

        @Nullable
        @Override
        public Map<String, String> obtainNetworkParams() {
            return null;
        }

        @Nullable
        @Override
        public EnumMap<AdsFormat, Map<String, String>> obtainNetworkMediationConfigs(@Nullable AdsFormat... adsFormats) {
            return mapEnumMap;
        }

    }

}
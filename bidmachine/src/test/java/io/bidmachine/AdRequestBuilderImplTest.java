package io.bidmachine;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AdRequestBuilderImplTest {

    private TestAdRequestBuilder adRequestBuilder;

    @Before
    public void setUp() throws Exception {
        adRequestBuilder = new TestAdRequestBuilder();
        BidMachine.initialize(RuntimeEnvironment.application, "1");
        BidMachineImpl.get().getInitNetworkConfigList().clear();
    }

    @Test
    public void setNetworksJson_invalidParams_nothingAdded() {
        adRequestBuilder.setNetworks((String) null);
        assertNull(adRequestBuilder.params);
        adRequestBuilder.setNetworks("");
        assertNull(adRequestBuilder.params);
        adRequestBuilder.setNetworks("test_string");
        assertNull(adRequestBuilder.params);
        adRequestBuilder.setNetworks("{}");
        assertNull(adRequestBuilder.params);
        adRequestBuilder.setNetworks("{test_string}");
        assertNull(adRequestBuilder.params);
        adRequestBuilder.setNetworks("[test_string]");
        assertNull(adRequestBuilder.params);
        adRequestBuilder.setNetworks("[]");
        assertNull(adRequestBuilder.params);
    }

    @Test
    @Config(manifest = "src/test/AndroidManifest.xml", sdk = 21)
    public void setNetworksJson_validParams_configAdded() throws Exception {
        JSONArray configArray = new JSONArray("[{"
                                                      + "\"network\": \"test_network\","
                                                      + "\"required_parameter_1\": \"required_value_1\","
                                                      + "\"required_parameter_2\": \"required_value_2\","
                                                      + "\"ad_units\": [{"
                                                      + "        \"ad_unit_id\": \"test_value_1\","
                                                      + "        \"format\": \"banner_320x50\""
                                                      + "    }, {"
                                                      + "        \"ad_unit_id\": \"test_value_2\","
                                                      + "        \"format\": \"interstitial_static\""
                                                      + "    }]"
                                                      + "}]");
        NetworkRegistry.cache.put("test_network", new TestNetworkConfig());
        adRequestBuilder.setNetworks(configArray.toString());

        assertNotNull(adRequestBuilder.params);
        assertEquals(1, adRequestBuilder.params.networkConfigMap.size());
    }

    @Test
    @Config(manifest = "src/test/AndroidManifest.xml", sdk = 21)
    public void setNetworksJson_validParamsWithNotRegisteredNetwork_nothingAdded() throws Exception {
        JSONArray configArray = new JSONArray("[{"
                                                      + "\"network\": \"test_network_1\","
                                                      + "\"required_parameter_1\": \"required_value_1\","
                                                      + "\"required_parameter_2\": \"required_value_2\","
                                                      + "\"ad_units\": [{"
                                                      + "        \"ad_unit_id\": \"test_value_1\","
                                                      + "        \"format\": \"banner_320x50\""
                                                      + "    }, {"
                                                      + "        \"ad_unit_id\": \"test_value_2\","
                                                      + "        \"format\": \"interstitial_static\""
                                                      + "    }]"
                                                      + "}]");
        NetworkRegistry.cache.put("test_network", new TestNetworkConfig());
        adRequestBuilder.setNetworks(configArray.toString());

        assertNull(adRequestBuilder.params);
    }

    @Test
    public void fillNetworkConfigs() {
        adRequestBuilder.fillNetworkConfigs(null);
        assertNull(adRequestBuilder.params);
        adRequestBuilder.fillNetworkConfigs(new ArrayList<NetworkConfig>());
        assertNull(adRequestBuilder.params);

        String networkKey = "TestAdapter";
        final TestNetworkConfig testNetworkConfig = new TestNetworkConfig(networkKey, null);
        NetworkRegistry.cache.put(networkKey, testNetworkConfig);
        adRequestBuilder.fillNetworkConfigs(new ArrayList<NetworkConfig>() {{
            add(testNetworkConfig);
        }});
        assertNotNull(adRequestBuilder.params);
        assertNotNull(adRequestBuilder.params.networkConfigMap);
        assertEquals(testNetworkConfig, adRequestBuilder.params.networkConfigMap.get(networkKey));
    }

    @Test
    public void fillNetworkConfigs_networkNotInitialized() {
        String networkKey = "TestAdapter";
        final TestNetworkConfig testNetworkConfig = new TestNetworkConfig(networkKey, null);
        adRequestBuilder.fillNetworkConfigs(new ArrayList<NetworkConfig>() {{
            add(testNetworkConfig);
        }});
        assertNotNull(adRequestBuilder.params);
        assertNotNull(adRequestBuilder.params.networkConfigMap);
        assertNull(adRequestBuilder.params.networkConfigMap.get(networkKey));
        assertNull(adRequestBuilder.params.networkConfigMap.get("TestAdapter_not_found"));
    }

    @Test
    public void fillNetworkConfigs_withInitNetworkConfig() {
        String networkKey1 = "TestAdapter1";
        final TestNetworkConfig testNetworkConfig1 = new TestNetworkConfig(networkKey1, null);
        String networkKey2 = "TestAdapter2";
        final TestNetworkConfig testNetworkConfig2 = new TestNetworkConfig(networkKey2, null);

        List<NetworkConfig> networkConfigList = BidMachineImpl.get().getInitNetworkConfigList();
        networkConfigList.clear();
        networkConfigList.add(testNetworkConfig1);

        NetworkRegistry.cache.put(networkKey2, testNetworkConfig2);
        adRequestBuilder.fillNetworkConfigs(new ArrayList<NetworkConfig>() {{
            add(testNetworkConfig2);
        }});
        assertNotNull(adRequestBuilder.params);
        assertNotNull(adRequestBuilder.params.networkConfigMap);
        assertEquals(testNetworkConfig1, adRequestBuilder.params.networkConfigMap.get(networkKey1));
        assertEquals(testNetworkConfig2, adRequestBuilder.params.networkConfigMap.get(networkKey2));
    }

    @Test
    public void fillNetworkConfigs_twoNetworkWithSameKeyFromDifferentSource_returnFromAdRequest() {
        String networkKey = "TestAdapter";
        final TestNetworkConfig testNetworkConfig1 = new TestNetworkConfig(networkKey, null);
        final TestNetworkConfig testNetworkConfig2 = new TestNetworkConfig(networkKey, null);

        List<NetworkConfig> networkConfigList = BidMachineImpl.get().getInitNetworkConfigList();
        networkConfigList.clear();
        networkConfigList.add(testNetworkConfig1);

        adRequestBuilder.fillNetworkConfigs(new ArrayList<NetworkConfig>() {{
            add(testNetworkConfig2);
        }});
        assertNotNull(adRequestBuilder.params);
        assertNotNull(adRequestBuilder.params.networkConfigMap);
        assertEquals(1, adRequestBuilder.params.networkConfigMap.size());
        assertEquals(testNetworkConfig2, adRequestBuilder.params.networkConfigMap.get(networkKey));
    }

}
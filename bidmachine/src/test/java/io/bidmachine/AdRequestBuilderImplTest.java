package io.bidmachine;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AdRequestBuilderImplTest {

    private static final String DEFAULT_KEY = "test_network_key";
    private TestAdRequestBuilder adRequestBuilder;

    @Before
    public void setUp() throws Exception {
        adRequestBuilder = new TestAdRequestBuilder();
        BidMachine.initialize(RuntimeEnvironment.application, "1");
        BidMachineImpl.get().getInitNetworkConfigList().clear();
        BidMachineImpl.get()
                .getInitNetworkConfigList()
                .add(new TestNetworkConfig(DEFAULT_KEY, null));
    }

    @Test
    public void setNetworksJson_invalidParams_addedOnlyInitParams() {
        adRequestBuilder.setNetworks((String) null);
        assertEquals(1, adRequestBuilder.params.networkConfigMap.size());
        assertNotNull(adRequestBuilder.params.networkConfigMap.get(DEFAULT_KEY));

        adRequestBuilder.setNetworks("");
        assertEquals(1, adRequestBuilder.params.networkConfigMap.size());
        assertNotNull(adRequestBuilder.params.networkConfigMap.get(DEFAULT_KEY));

        adRequestBuilder.setNetworks("test_string");
        assertEquals(1, adRequestBuilder.params.networkConfigMap.size());
        assertNotNull(adRequestBuilder.params.networkConfigMap.get(DEFAULT_KEY));

        adRequestBuilder.setNetworks("{}");
        assertEquals(1, adRequestBuilder.params.networkConfigMap.size());
        assertNotNull(adRequestBuilder.params.networkConfigMap.get(DEFAULT_KEY));

        adRequestBuilder.setNetworks("{test_string}");
        assertEquals(1, adRequestBuilder.params.networkConfigMap.size());
        assertNotNull(adRequestBuilder.params.networkConfigMap.get(DEFAULT_KEY));

        adRequestBuilder.setNetworks("[]");
        assertEquals(1, adRequestBuilder.params.networkConfigMap.size());
        assertNotNull(adRequestBuilder.params.networkConfigMap.get(DEFAULT_KEY));

        adRequestBuilder.setNetworks("[test_string]");
        assertEquals(1, adRequestBuilder.params.networkConfigMap.size());
        assertNotNull(adRequestBuilder.params.networkConfigMap.get(DEFAULT_KEY));
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
        assertEquals(2, adRequestBuilder.params.networkConfigMap.size());
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

        assertNotNull(adRequestBuilder.params);
        assertEquals(1, adRequestBuilder.params.networkConfigMap.size());
    }

    @Test
    public void fillNetworkConfigs() {
        adRequestBuilder.fillNetworkConfigs(null);
        assertNotNull(adRequestBuilder.params);
        assertEquals(1, adRequestBuilder.params.networkConfigMap.size());

        adRequestBuilder.fillNetworkConfigs(new ArrayList<NetworkConfig>());
        assertNotNull(adRequestBuilder.params);
        assertEquals(1, adRequestBuilder.params.networkConfigMap.size());

        String networkKey = "TestAdapter";
        final TestNetworkConfig testNetworkConfig = new TestNetworkConfig(networkKey, null);
        NetworkRegistry.cache.put(networkKey, testNetworkConfig);
        adRequestBuilder.fillNetworkConfigs(new ArrayList<NetworkConfig>() {{
            add(testNetworkConfig);
        }});
        assertNotNull(adRequestBuilder.params);
        assertNotNull(adRequestBuilder.params.networkConfigMap);
        assertEquals(2, adRequestBuilder.params.networkConfigMap.size());
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
        assertEquals(1, adRequestBuilder.params.networkConfigMap.size());
        assertNull(adRequestBuilder.params.networkConfigMap.get(networkKey));
    }

    @Test
    public void fillNetworkConfigs_twoNetworkWithSameKeyFromDifferentSource_returnFromAdRequest() {
        final TestNetworkConfig testNetworkConfig = new TestNetworkConfig(DEFAULT_KEY, null);
        adRequestBuilder.fillNetworkConfigs(new ArrayList<NetworkConfig>() {{
            add(testNetworkConfig);
        }});
        assertNotNull(adRequestBuilder.params);
        assertNotNull(adRequestBuilder.params.networkConfigMap);
        assertEquals(1, adRequestBuilder.params.networkConfigMap.size());
        assertEquals(testNetworkConfig,
                     adRequestBuilder.params.networkConfigMap.get(DEFAULT_KEY));
    }

}
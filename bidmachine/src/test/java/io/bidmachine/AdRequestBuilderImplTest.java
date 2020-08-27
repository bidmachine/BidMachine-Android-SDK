package io.bidmachine;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

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
    }

    @Test
    public void setNetworksJson() {
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
    public void fillNetworkConfigs() {
        adRequestBuilder.fillNetworkConfigs(null);
        assertNull(adRequestBuilder.params);
        adRequestBuilder.fillNetworkConfigs(new ArrayList<NetworkConfig>());
        assertNull(adRequestBuilder.params);
        final NetworkConfigTest.TestNetworkConfig testNetworkConfig =
                new NetworkConfigTest.TestNetworkConfig(null);
        adRequestBuilder.fillNetworkConfigs(new ArrayList<NetworkConfig>() {{
            add(testNetworkConfig);
        }});
        assertNotNull(adRequestBuilder.params);
        assertNotNull(adRequestBuilder.params.networkConfigMap);
        assertEquals(testNetworkConfig,
                     adRequestBuilder.params.networkConfigMap.get("TestAdapter"));
        assertNull(adRequestBuilder.params.networkConfigMap.get("TestAdapter_not_found"));
    }

}
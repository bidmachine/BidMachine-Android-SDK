package io.bidmachine;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NetworkRegistryTest {

    @Before
    public void setUp() throws Exception {
        NetworkRegistry.pendingNetworks = null;
        NetworkRegistry.pendingNetworksJson = null;
    }

    @Test
    public void isNetworkRegistered_networksNotRegistered_returnFalse() {
        boolean result = NetworkRegistry.isNetworkRegistered("test_key");
        assertFalse(result);
    }

    @Test
    public void isNetworkRegistered_networksNotRegisteredAndNetworkListsIsEmpty_returnFalse() {
        NetworkRegistry.pendingNetworks = new HashSet<>();
        NetworkRegistry.pendingNetworksJson = new HashSet<>();

        boolean result = NetworkRegistry.isNetworkRegistered("test_key");
        assertFalse(result);
    }

    @Test
    public void isNetworkRegistered_registeredNotThatNetworks_returnFalse() throws Exception {
        NetworkRegistry.pendingNetworks = new HashSet<>();
        NetworkRegistry.pendingNetworksJson = new HashSet<>();
        NetworkConfig testNetwork1 = new TestNetworkConfig("test_network_key_1", null);
        JSONObject testNetwork2 = new JSONObject().put(NetworkConfig.KEY_NETWORK,
                                                       "test_network_key_2");

        NetworkRegistry.pendingNetworks.add(testNetwork1);
        NetworkRegistry.pendingNetworksJson.add(testNetwork2);

        boolean result = NetworkRegistry.isNetworkRegistered("test_key");
        assertFalse(result);
    }

    @Test
    public void isNetworkRegistered_pendingNetworksIsRegistered_returnTrue() {
        NetworkConfig testNetwork = new TestNetworkConfig("test_key", null);
        NetworkRegistry.pendingNetworks = new HashSet<>();
        NetworkRegistry.pendingNetworks.add(testNetwork);

        boolean result = NetworkRegistry.isNetworkRegistered("test_key");
        assertTrue(result);
    }

    @Test
    public void isNetworkRegistered_pendingNetworksJsonIsRegistered_returnTrue() throws Exception {
        JSONObject testNetwork = new JSONObject().put(NetworkConfig.KEY_NETWORK, "test_key");
        NetworkRegistry.pendingNetworksJson = new HashSet<>();
        NetworkRegistry.pendingNetworksJson.add(testNetwork);

        boolean result = NetworkRegistry.isNetworkRegistered("test_key");
        assertTrue(result);
    }

    @Test
    public void registerNetwork_networkConfigIsNull_pendingNetworksIsNull() {
        NetworkRegistry.registerNetwork((NetworkConfig) null);

        assertNull(NetworkRegistry.pendingNetworks);
    }

    @Test
    public void registerNetwork_networkConfigNotNull_pendingNetworkNotNull() {
        NetworkConfig testNetwork = new TestNetworkConfig("test_key", null);
        NetworkRegistry.registerNetwork(testNetwork);

        assertNotNull(NetworkRegistry.pendingNetworks);
        assertEquals(1, NetworkRegistry.pendingNetworks.size());
        assertEquals("test_key", NetworkRegistry.pendingNetworks.iterator().next().getKey());
    }

    @Test
    public void registerNetwork_networkJsonIsNull_pendingNetworksIsNull() {
        NetworkRegistry.registerNetwork((JSONObject) null);

        assertNull(NetworkRegistry.pendingNetworksJson);
    }

    @Test
    public void registerNetwork_networkJsonNotNull_pendingNetworkNotNull() throws Exception {
        JSONObject testNetwork = new JSONObject().put(NetworkConfig.KEY_NETWORK, "test_key");
        NetworkRegistry.registerNetwork(testNetwork);

        assertNotNull(NetworkRegistry.pendingNetworksJson);
        assertEquals(1, NetworkRegistry.pendingNetworksJson.size());
        assertEquals("test_key",
                     NetworkRegistry.pendingNetworksJson.iterator()
                             .next()
                             .getString(NetworkConfig.KEY_NETWORK));
    }

}
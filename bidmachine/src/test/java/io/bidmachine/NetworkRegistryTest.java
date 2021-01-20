package io.bidmachine;

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
        NetworkRegistry.pendingNetworks = new HashSet<>();
    }

    @Test
    public void isNetworkRegistered_pendingNetworksIsNull_returnFalse() {
        NetworkRegistry.pendingNetworks = null;
        boolean result = NetworkRegistry.isNetworkRegistered("test_key");

        assertFalse(result);
    }

    @Test
    public void isNetworkRegistered_networksNotRegistered_returnFalse() {
        boolean result = NetworkRegistry.isNetworkRegistered("test_key");

        assertFalse(result);
    }

    @Test
    public void isNetworkRegistered_registeredNotThatNetworks_returnFalse() {
        NetworkConfig testNetwork1 = new TestNetworkConfig("test_network_key_1", null);
        NetworkRegistry.pendingNetworks.add(testNetwork1);
        boolean result = NetworkRegistry.isNetworkRegistered("test_key");

        assertFalse(result);
    }

    @Test
    public void isNetworkRegistered_pendingNetworksIsRegistered_returnTrue() {
        NetworkConfig testNetwork = new TestNetworkConfig("test_key", null);
        NetworkRegistry.pendingNetworks.add(testNetwork);
        boolean result = NetworkRegistry.isNetworkRegistered("test_key");

        assertTrue(result);
    }

    @Test
    public void registerNetwork_networkConfigIsNull_pendingNetworksIsNull() {
        NetworkRegistry.pendingNetworks = null;
        NetworkRegistry.registerNetwork(null);

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

}
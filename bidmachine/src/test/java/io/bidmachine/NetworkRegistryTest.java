package io.bidmachine;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NetworkRegistryTest {

    @Before
    public void setUp() throws Exception {
        NetworkRegistry.cache.clear();
        NetworkRegistry.pendingNetworks.clear();
    }

    @Test
    public void isNetworkRegistered_networksNotRegistered_returnFalse() {
        boolean result = NetworkRegistry.isNetworkRegistered("test_key");

        assertFalse(result);
    }

    @Test
    public void isNetworkRegistered_registeredNotThatNetworks_returnFalse() {
        NetworkConfig testNetwork = new TestNetworkConfig("test_network_key_1", null);
        NetworkRegistry.registerNetwork(testNetwork);
        boolean result = NetworkRegistry.isNetworkRegistered("test_key");

        assertFalse(result);
    }

    @Test
    public void isNetworkRegistered_pendingNetworksIsRegistered_returnTrue() {
        NetworkConfig testNetwork = new TestNetworkConfig("test_key", null);
        NetworkRegistry.registerNetwork(testNetwork);
        boolean result = NetworkRegistry.isNetworkRegistered("test_key");

        assertTrue(result);
    }

}
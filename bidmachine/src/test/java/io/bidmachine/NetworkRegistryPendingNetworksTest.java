package io.bidmachine;

import org.junit.Before;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;

import java.util.Collection;

public class NetworkRegistryPendingNetworksTest extends MultiThreadingTest {

    @Parameters(name = "Count of actions: {0}, Count of read thread: {1}")
    public static Collection<Object[]> data() {
        return createDefaultParameters();
    }

    public NetworkRegistryPendingNetworksTest(int actionCount, int readThreadCount) {
        super(actionCount, readThreadCount);
    }

    @Before
    public void setUp() throws Exception {
        NetworkRegistry.pendingNetworks.clear();
    }

    @Override
    void writeAction(int actionNumber, int totalActions) {
        NetworkConfig networkConfig = new TestNetworkConfig("key" + actionNumber, null);
        NetworkRegistry.pendingNetworks.put(networkConfig.getKey(), networkConfig);
    }

    @Override
    void readAction(int actionNumber, int totalActions) {
        for (NetworkConfig networkConfig : NetworkRegistry.pendingNetworks.values()) {
            System.out.println(networkConfig.getKey());
        }
    }

}
package io.bidmachine;

import org.json.JSONObject;
import org.junit.Before;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;

import java.util.Collection;

public class NetworkRegistryIsNetworkRegistered extends MultiThreadingTest {

    @Parameters(name = "Count of actions: {0}, Count of read thread: {1}")
    public static Collection<Object[]> data() {
        return createDefaultParameters();
    }

    public NetworkRegistryIsNetworkRegistered(int actionCount, int readThreadCount) {
        super(actionCount, readThreadCount);
    }

    @Before
    public void setUp() throws Exception {
        NetworkRegistry.cache.clear();
    }

    @Override
    void writeAction(int actionNumber, int totalActions) throws Exception {
        String key1 = "key" + actionNumber;
        String key2 = "key" + (actionNumber + 1);
        String key3 = "key" + (actionNumber + 2);

        NetworkRegistry.registerNetwork(new TestNetworkConfig(key1, null));
        NetworkRegistry.registerNetwork(new JSONObject().put(NetworkConfig.KEY_NETWORK, key2));
        NetworkRegistry.cache.put("key" + actionNumber,
                                  new TestNetworkConfig(key3, null));
    }

    @Override
    void readAction(int actionNumber, int totalActions) {
        String key1 = "key" + actionNumber;
        String key2 = "key" + (actionNumber + 1);
        String key3 = "key" + (actionNumber + 2);

        NetworkRegistry.isNetworkRegistered(key1);
        NetworkRegistry.isNetworkRegistered(key2);
        NetworkRegistry.isNetworkRegistered(key3);
    }

}
package io.bidmachine;

import org.junit.Before;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;

import java.util.Collection;

public class NetworkRegistrySetLoggingEnabledTest extends MultiThreadingTest {

    @Parameters(name = "Count of actions: {0}, Count of read thread: {1}")
    public static Collection<Object[]> data() {
        return createDefaultParameters();
    }

    public NetworkRegistrySetLoggingEnabledTest(int actionCount, int readThreadCount) {
        super(actionCount, readThreadCount);
    }

    @Before
    public void setUp() throws Exception {
        NetworkRegistry.cache.clear();
    }

    @Override
    void writeAction(int actionNumber, int totalActions) {
        NetworkRegistry.cache.put("key" + actionNumber, new TestNetworkConfig());
    }

    @Override
    void readAction(int actionNumber, int totalActions) {
        NetworkRegistry.setLoggingEnabled(true);
    }

}
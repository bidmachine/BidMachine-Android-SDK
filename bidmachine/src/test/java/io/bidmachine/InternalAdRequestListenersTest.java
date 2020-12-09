package io.bidmachine;

import androidx.annotation.NonNull;

import org.junit.Before;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;

import java.util.Collection;

public class InternalAdRequestListenersTest extends MultiThreadingTest {

    @Parameters(name = "Count of actions: {0}, Count of read thread: {1}")
    public static Collection<Object[]> data() {
        return createDefaultParameters();
    }

    public InternalAdRequestListenersTest(int actionCount, int readThreadCount) {
        super(actionCount, readThreadCount);
    }

    private TestAdRequest testAdRequest;

    @Before
    public void setUp() throws Exception {
        testAdRequest = new TestAdRequest.Builder(AdsType.Banner).build();
    }

    @Override
    void writeAction(int actionNumber, int totalActions) {
        testAdRequest.addInternalListener(new AdRequest.InternalAdRequestListener<TestAdRequest>() {
            @Override
            public void onRequestDestroyed(@NonNull TestAdRequest request) {

            }
        });
    }

    @Override
    void readAction(int actionNumber, int totalActions) {
        testAdRequest.notifyRequestDestroyed();
    }

}
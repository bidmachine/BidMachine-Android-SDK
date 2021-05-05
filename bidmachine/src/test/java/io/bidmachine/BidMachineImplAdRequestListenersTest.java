package io.bidmachine;

import org.junit.Before;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;

import java.util.Collection;

public class BidMachineImplAdRequestListenersTest extends MultiThreadingTest {

    @Parameters(name = "Count of actions: {0}, Count of read thread: {1}")
    public static Collection<Object[]> data() {
        return createDefaultParameters();
    }

    public BidMachineImplAdRequestListenersTest(int actionCount, int readThreadCount) {
        super(actionCount, readThreadCount);
    }

    private BidMachineImpl bidMachine;

    @Before
    public void setUp() throws Exception {
        bidMachine = BidMachineImpl.get();
    }

    @Override
    void writeAction(int actionNumber, int totalActions) {
        bidMachine.registerAdRequestListener(new SimpleAdRequestListener());
    }

    @Override
    void readAction(int actionNumber, int totalActions) {
        for (AdRequest.AdRequestListener<?> adRequest : bidMachine.getAdRequestListeners()) {
            System.out.println(adRequest.toString());
        }
    }

}
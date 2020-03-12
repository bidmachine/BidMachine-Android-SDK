package io.bidmachine;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BidMachineImplTest {

    private BidMachineImpl bidMachine;

    @Before
    public void setUp() throws Exception {
        bidMachine = BidMachineImpl.get();
    }

    @Test
    public void beforeInit() {
        assertNotNull(bidMachine.getTargetingParams());
        assertNotNull(bidMachine.getExtraParams());
        assertNotNull(bidMachine.getUserRestrictionParams());
        assertNotNull(bidMachine.getPriceFloorParams());
        assertNotNull(bidMachine.getDeviceParams());
        assertNotNull(bidMachine.getIabSharedPreference());
    }

}
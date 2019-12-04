package io.bidmachine;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BidMachineTest {

    @Test
    public void setPublisher() {
        assertNull(BidMachineImpl.get().getPublisher());

        Publisher publisher = new Publisher.Builder().build();
        BidMachine.setPublisher(publisher);
        assertNotNull(BidMachineImpl.get().getPublisher());
        assertEquals(publisher, BidMachineImpl.get().getPublisher());

        BidMachine.setPublisher(null);
        assertNull(BidMachineImpl.get().getPublisher());
    }

}
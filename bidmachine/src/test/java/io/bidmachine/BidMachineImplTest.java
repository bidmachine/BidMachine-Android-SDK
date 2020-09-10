package io.bidmachine;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BidMachineImplTest {

    private Context context;
    private BidMachineImpl bidMachine;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
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

    @Test
    public void obtainIFV() {
        String ifv1 = bidMachine.obtainIFV(context);
        String ifv2 = bidMachine.obtainIFV(context);
        assertNotNull(ifv1);
        assertNotNull(ifv2);
        assertEquals(ifv1, ifv2);

        bidMachine.ifv = null;
        String ifv3 = bidMachine.obtainIFV(context);
        assertNotNull(ifv3);
        assertEquals(ifv2, ifv3);
    }

    @Test
    public void getSessionAdParams() {
        SessionAdParams bannerSessionAdParams1 = bidMachine.getSessionAdParams(AdsType.Banner);
        assertNotNull(bannerSessionAdParams1);
        SessionAdParams bannerSessionAdParams2 = bidMachine.getSessionAdParams(AdsType.Banner);
        assertNotNull(bannerSessionAdParams1);
        assertEquals(bannerSessionAdParams1, bannerSessionAdParams2);
        SessionAdParams interstitialSessionAdParams1 = bidMachine.getSessionAdParams(AdsType.Interstitial);
        assertNotNull(bannerSessionAdParams1);
        assertNotEquals(interstitialSessionAdParams1, bannerSessionAdParams1);
    }

}
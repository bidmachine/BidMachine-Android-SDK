package io.bidmachine.ads.networks.criteo;

import com.criteo.publisher.Bid;
import com.criteo.publisher.CriteoUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Map;

import io.bidmachine.AdRequest;
import io.bidmachine.banner.BannerRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CriteoBidTokenControllerTest {

    @Before
    public void setUp() throws Exception {
        CriteoBidTokenController.bidMap.clear();
    }

    @Test
    public void storeBidToken() {
        Bid bid1 = CriteoUtils.createBidToken();
        Bid bid2 = CriteoUtils.createBidToken();
        Bid bid3 = CriteoUtils.createBidToken();
        AdRequest<?, ?> adRequest1 = new BannerRequest.Builder().build();
        AdRequest<?, ?> adRequest2 = new BannerRequest.Builder().build();
        CriteoBidTokenController.storeBid(null, bid1);
        CriteoBidTokenController.storeBid(adRequest1, bid2);
        CriteoBidTokenController.storeBid(adRequest2, bid3);

        Map<AdRequest, Bid> bidMap = CriteoBidTokenController.bidMap;
        assertEquals(2, bidMap.size());
        assertEquals(bid2, bidMap.get(adRequest1));
        assertEquals(bid3, bidMap.get(adRequest2));
    }

    @Test
    public void takeBidToken() {
        Bid bid1 = CriteoBidTokenController.takeBid(null);
        Bid bid2 = CriteoBidTokenController.takeBid(new BannerRequest.Builder().build());
        assertNull(bid1);
        assertNull(bid2);

        Bid bid3 = CriteoUtils.createBidToken();
        AdRequest<?, ?> adRequest1 = new BannerRequest.Builder().build();
        CriteoBidTokenController.storeBid(adRequest1, bid3);
        assertEquals(bid3, CriteoBidTokenController.takeBid(adRequest1));
    }

}
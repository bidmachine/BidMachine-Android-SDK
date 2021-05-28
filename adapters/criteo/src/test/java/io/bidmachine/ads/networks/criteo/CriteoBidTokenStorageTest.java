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
public class CriteoBidTokenStorageTest {

    @Before
    public void setUp() throws Exception {
        CriteoBidTokenStorage.bidMap.clear();
    }

    @Test
    public void storeBidToken() {
        Bid bid1 = CriteoUtils.createBidToken();
        Bid bid2 = CriteoUtils.createBidToken();
        Bid bid3 = CriteoUtils.createBidToken();
        AdRequest<?, ?> adRequest1 = new BannerRequest.Builder().build();
        AdRequest<?, ?> adRequest2 = new BannerRequest.Builder().build();
        CriteoBidTokenStorage.storeBid(null, bid1);
        CriteoBidTokenStorage.storeBid(adRequest1, bid2);
        CriteoBidTokenStorage.storeBid(adRequest2, bid3);

        Map<AdRequest, Bid> bidMap = CriteoBidTokenStorage.bidMap;
        assertEquals(2, bidMap.size());
        assertEquals(bid2, bidMap.get(adRequest1));
        assertEquals(bid3, bidMap.get(adRequest2));
    }

    @Test
    public void takeBidToken() {
        Bid bid1 = CriteoBidTokenStorage.takeBid(null);
        Bid bid2 = CriteoBidTokenStorage.takeBid(new BannerRequest.Builder().build());
        assertNull(bid1);
        assertNull(bid2);

        Bid bid3 = CriteoUtils.createBidToken();
        AdRequest<?, ?> adRequest1 = new BannerRequest.Builder().build();
        CriteoBidTokenStorage.storeBid(adRequest1, bid3);
        assertEquals(bid3, CriteoBidTokenStorage.takeBid(adRequest1));
    }

}
package io.bidmachine.ads.networks.criteo;

import androidx.annotation.NonNull;

import com.criteo.publisher.Bid;
import com.criteo.publisher.CriteoUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Map;

import io.bidmachine.AdRequest;
import io.bidmachine.AdsType;
import io.bidmachine.TargetingParams;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.unified.UnifiedAdRequestParams;

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
        AdRequest adRequest1 = createAdRequest();
        AdRequest adRequest2 = createAdRequest();
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
        Bid bid2 = CriteoBidTokenController.takeBid(createAdRequest());
        assertNull(bid1);
        assertNull(bid2);

        Bid bid3 = CriteoUtils.createBidToken();
        AdRequest adRequest1 = createAdRequest();
        CriteoBidTokenController.storeBid(adRequest1, bid3);
        assertEquals(bid3, CriteoBidTokenController.takeBid(adRequest1));
    }

    private AdRequest createAdRequest() {
        return new AdRequest(AdsType.Banner) {
            @NonNull
            @Override
            protected UnifiedAdRequestParams createUnifiedAdRequestParams(@NonNull TargetingParams targetingParams,
                                                                          @NonNull DataRestrictions dataRestrictions) {
                return null;
            }
        };
    }

}
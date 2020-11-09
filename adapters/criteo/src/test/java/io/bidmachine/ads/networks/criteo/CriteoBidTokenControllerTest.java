package io.bidmachine.ads.networks.criteo;

import androidx.annotation.NonNull;

import com.criteo.publisher.BidToken;
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
        CriteoBidTokenController.bidTokenMap.clear();
    }

    @Test
    public void storeBidToken() {
        BidToken bidToken1 = CriteoUtils.createBidToken();
        BidToken bidToken2 = CriteoUtils.createBidToken();
        BidToken bidToken3 = CriteoUtils.createBidToken();
        AdRequest adRequest1 = createAdRequest();
        AdRequest adRequest2 = createAdRequest();
        CriteoBidTokenController.storeBid(null, bidToken1);
        CriteoBidTokenController.storeBid(adRequest1, bidToken2);
        CriteoBidTokenController.storeBid(adRequest2, bidToken3);

        Map<AdRequest, BidToken> bidTokenMap = CriteoBidTokenController.bidTokenMap;
        assertEquals(2, bidTokenMap.size());
        assertEquals(bidToken2, bidTokenMap.get(adRequest1));
        assertEquals(bidToken3, bidTokenMap.get(adRequest2));
    }

    @Test
    public void takeBidToken() {
        BidToken bidToken1 = CriteoBidTokenController.takeBid(null);
        BidToken bidToken2 = CriteoBidTokenController.takeBid(createAdRequest());
        assertNull(bidToken1);
        assertNull(bidToken2);

        BidToken bidToken3 = CriteoUtils.createBidToken();
        AdRequest adRequest1 = createAdRequest();
        CriteoBidTokenController.storeBid(adRequest1, bidToken3);
        assertEquals(bidToken3, CriteoBidTokenController.takeBid(adRequest1));
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
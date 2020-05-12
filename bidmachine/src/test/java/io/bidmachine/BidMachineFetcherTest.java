package io.bidmachine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import io.bidmachine.models.AuctionResult;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.unified.UnifiedAdRequestParams;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BidMachineFetcherTest {

    @Before
    public void setUp() throws Exception {
        BidMachineFetcher.cachedRequests = new EnumMap<>(AdsType.class);
        BidMachineFetcher.resetPriceRounding();
    }

    @Test
    public void setPriceRounding() {
        BidMachineFetcher.setPriceRounding(0.01);
        assertEquals("0.01", BidMachineFetcher.priceRounding.toString());
        assertEquals(RoundingMode.CEILING, BidMachineFetcher.priceRoundingMode);

        BidMachineFetcher.setPriceRounding(1.001);
        assertEquals("1.001", BidMachineFetcher.priceRounding.toString());
        assertEquals(RoundingMode.CEILING, BidMachineFetcher.priceRoundingMode);

        BidMachineFetcher.setPriceRounding(0.01, RoundingMode.FLOOR);
        assertEquals("0.01", BidMachineFetcher.priceRounding.toString());
        assertEquals(RoundingMode.FLOOR, BidMachineFetcher.priceRoundingMode);

        BidMachineFetcher.setPriceRounding(1.001, RoundingMode.FLOOR);
        assertEquals("1.001", BidMachineFetcher.priceRounding.toString());
        assertEquals(RoundingMode.FLOOR, BidMachineFetcher.priceRoundingMode);
    }

    @Test
    public void fetch() {
        AdRequest adRequest = createAdRequest(AdsType.Banner, "test_banner_id_1", 0.001);
        Map<String, String> params = BidMachineFetcher.fetch(adRequest);
        assertNotNull(params);
        assertEquals("test_banner_id_1", params.get(BidMachineFetcher.KEY_ID));
        assertEquals(BidMachineFetcher.roundPrice(0.001), params.get(BidMachineFetcher.KEY_PRICE));
        assertEquals(1, BidMachineFetcher.cachedRequests.size());
        Map<String, AdRequest> requestMap = BidMachineFetcher.cachedRequests.get(AdsType.Banner);
        assertNotNull(requestMap);
        assertEquals(1, requestMap.size());

        adRequest = createAdRequest(AdsType.Banner, "test_banner_id_2", 0.002);
        params = BidMachineFetcher.fetch(adRequest);
        assertNotNull(params);
        assertEquals("test_banner_id_2", params.get(BidMachineFetcher.KEY_ID));
        assertEquals(BidMachineFetcher.roundPrice(0.002), params.get(BidMachineFetcher.KEY_PRICE));
        assertEquals(1, BidMachineFetcher.cachedRequests.size());
        requestMap = BidMachineFetcher.cachedRequests.get(AdsType.Banner);
        assertNotNull(requestMap);
        assertEquals(2, requestMap.size());

        adRequest = createAdRequest(AdsType.Interstitial, "test_interstitial_id_1", 0.003);
        params = BidMachineFetcher.fetch(adRequest);
        assertNotNull(params);
        assertEquals("test_interstitial_id_1", params.get(BidMachineFetcher.KEY_ID));
        assertEquals(BidMachineFetcher.roundPrice(0.003), params.get(BidMachineFetcher.KEY_PRICE));
        assertEquals(2, BidMachineFetcher.cachedRequests.size());
        requestMap = BidMachineFetcher.cachedRequests.get(AdsType.Interstitial);
        assertNotNull(requestMap);
        assertEquals(1, requestMap.size());
    }

    @Test
    public void fetch_expired() throws Exception {
        AdRequest adRequest1 = createAdRequest(AdsType.Banner, "test_banner_id", 0.01);
        AdRequest adRequest2 = createAdRequest(AdsType.Banner, "test_banner_id", 0.01);
        BidMachineFetcher.fetch(adRequest1);
        Map<String, AdRequest> requestMap = BidMachineFetcher.cachedRequests.get(AdsType.Banner);
        assertNotNull(requestMap);
        assertEquals(1, requestMap.size());

        adRequest2.processExpired();
        requestMap = BidMachineFetcher.cachedRequests.get(AdsType.Banner);
        assertNotNull(requestMap);
        assertEquals(1, requestMap.size());

        adRequest1.processExpired();
        requestMap = BidMachineFetcher.cachedRequests.get(AdsType.Banner);
        assertNotNull(requestMap);
        assertEquals(0, requestMap.size());
    }

    @Test
    public void release1() {
        AdRequest adRequest = createAdRequest(AdsType.Banner, "test_banner_id_1", 0.001);
        BidMachineFetcher.fetch(adRequest);
        adRequest = createAdRequest(AdsType.Banner, "test_banner_id_2", 0.002);
        BidMachineFetcher.fetch(adRequest);
        adRequest = createAdRequest(AdsType.Interstitial, "test_interstitial_id_1", 0.003);
        BidMachineFetcher.fetch(adRequest);

        adRequest = BidMachineFetcher.release(AdsType.Banner, (String) null);
        assertNull(adRequest);

        adRequest = BidMachineFetcher.release(AdsType.Banner, "");
        assertNull(adRequest);

        adRequest = BidMachineFetcher.release(AdsType.Banner, "test_banner_id_0");
        assertNull(adRequest);

        adRequest = BidMachineFetcher.release(AdsType.Native, "test_banner_id_1");
        assertNull(adRequest);

        adRequest = BidMachineFetcher.release(AdsType.Banner, "test_banner_id_1");
        assertNotNull(adRequest);
        AuctionResult auctionResult = adRequest.getAuctionResult();
        assertNotNull(auctionResult);
        assertEquals("test_banner_id_1", auctionResult.getId());
        assertEquals(0.001, auctionResult.getPrice(), 0D);

        assertEquals(2, BidMachineFetcher.cachedRequests.size());
        Map<String, AdRequest> requestMap = BidMachineFetcher.cachedRequests.get(AdsType.Banner);
        assertNotNull(requestMap);
        assertEquals(1, requestMap.size());
    }

    @Test
    public void release2() {
        AdRequest adRequest1 = createAdRequest(AdsType.Banner, "test_banner_id_1", 0.001);
        BidMachineFetcher.fetch(adRequest1);
        AdRequest adRequest2 = createAdRequest(AdsType.Banner, "test_banner_id_2", 0.002);
        BidMachineFetcher.fetch(adRequest2);
        AdRequest adRequest3 = createAdRequest(AdsType.Interstitial,
                                               "test_interstitial_id_1",
                                               0.003);
        BidMachineFetcher.fetch(adRequest3);

        AdRequest adRequest = createAdRequest(AdsType.Banner, null, 0.001);
        AdRequest adRequestReleased = BidMachineFetcher.release(adRequest);
        assertNull(adRequestReleased);

        adRequest = createAdRequest(AdsType.Banner, "", 0.001);
        adRequestReleased = BidMachineFetcher.release(adRequest);
        assertNull(adRequestReleased);

        adRequest = createAdRequest(AdsType.Banner, "test_id_0", 0.001);
        adRequestReleased = BidMachineFetcher.release(adRequest);
        assertNull(adRequestReleased);

        adRequest = createAdRequest(AdsType.Native, "test_banner_id_1", 0.001);
        adRequestReleased = BidMachineFetcher.release(adRequest);
        assertNull(adRequestReleased);

        adRequest = BidMachineFetcher.release(adRequest1);
        assertNotNull(adRequest);
        AuctionResult auctionResult = adRequest.getAuctionResult();
        assertNotNull(auctionResult);
        assertEquals("test_banner_id_1", auctionResult.getId());
        assertEquals(0.001, auctionResult.getPrice(), 0D);

        assertEquals(2, BidMachineFetcher.cachedRequests.size());
        Map<String, AdRequest> requestMap = BidMachineFetcher.cachedRequests.get(AdsType.Banner);
        assertNotNull(requestMap);
        assertEquals(1, requestMap.size());
    }

    @Test
    public void release3() {
        AdRequest adRequest = createAdRequest(AdsType.Banner, "test_banner_id_1", 0.001);
        BidMachineFetcher.fetch(adRequest);
        adRequest = createAdRequest(AdsType.Banner, "test_banner_id_2", 0.002);
        BidMachineFetcher.fetch(adRequest);
        adRequest = createAdRequest(AdsType.Interstitial, "test_interstitial_id_1", 0.003);
        BidMachineFetcher.fetch(adRequest);

        adRequest = BidMachineFetcher.release(AdsType.Banner, createMapWithId(null));
        assertNull(adRequest);

        adRequest = BidMachineFetcher.release(AdsType.Banner, createMapWithId(""));
        assertNull(adRequest);

        adRequest = BidMachineFetcher.release(AdsType.Banner, createMapWithId("test_banner_id_0"));
        assertNull(adRequest);

        adRequest = BidMachineFetcher.release(AdsType.Native, createMapWithId("test_banner_id_1"));
        assertNull(adRequest);

        adRequest = BidMachineFetcher.release(AdsType.Banner, createMapWithId("test_banner_id_1"));
        assertNotNull(adRequest);
        AuctionResult auctionResult = adRequest.getAuctionResult();
        assertNotNull(auctionResult);
        assertEquals("test_banner_id_1", auctionResult.getId());
        assertEquals(0.001, auctionResult.getPrice(), 0D);

        assertEquals(2, BidMachineFetcher.cachedRequests.size());
        Map<String, AdRequest> requestMap = BidMachineFetcher.cachedRequests.get(AdsType.Banner);
        assertNotNull(requestMap);
        assertEquals(1, requestMap.size());
    }

    @Test
    public void roundPrice1() {
        BidMachineFetcher.setPriceRounding(0.01);

        String result = BidMachineFetcher.roundPrice(0.01);
        assertEquals("0.01", result);
        result = BidMachineFetcher.roundPrice(0.99);
        assertEquals("0.99", result);
        result = BidMachineFetcher.roundPrice(1.212323);
        assertEquals("1.22", result);
        result = BidMachineFetcher.roundPrice(1.34538483);
        assertEquals("1.35", result);
        result = BidMachineFetcher.roundPrice(1.4);
        assertEquals("1.40", result);
        result = BidMachineFetcher.roundPrice(1.58538483);
        assertEquals("1.59", result);
    }

    @Test
    public void roundPrice2() {
        BidMachineFetcher.setPriceRounding(0.1);

        String result = BidMachineFetcher.roundPrice(0.01);
        assertEquals("0.1", result);
        result = BidMachineFetcher.roundPrice(0.99);
        assertEquals("1.0", result);
        result = BidMachineFetcher.roundPrice(1.212323);
        assertEquals("1.3", result);
        result = BidMachineFetcher.roundPrice(1.34538483);
        assertEquals("1.4", result);
        result = BidMachineFetcher.roundPrice(1.4);
        assertEquals("1.4", result);
        result = BidMachineFetcher.roundPrice(1.58538483);
        assertEquals("1.6", result);
    }

    @Test
    public void roundPrice3() {
        BidMachineFetcher.setPriceRounding(0.01, RoundingMode.FLOOR);

        String result = BidMachineFetcher.roundPrice(0.01);
        assertEquals("0.01", result);
        result = BidMachineFetcher.roundPrice(0.99);
        assertEquals("0.99", result);
        result = BidMachineFetcher.roundPrice(1.212323);
        assertEquals("1.21", result);
        result = BidMachineFetcher.roundPrice(1.34538483);
        assertEquals("1.34", result);
        result = BidMachineFetcher.roundPrice(1.4);
        assertEquals("1.40", result);
        result = BidMachineFetcher.roundPrice(1.58538483);
        assertEquals("1.58", result);
    }


    private AdRequest createAdRequest(AdsType adsType, final String id, final double price) {
        AdRequest adRequest = new AdRequest(adsType) {
            @NonNull
            @Override
            protected UnifiedAdRequestParams createUnifiedAdRequestParams(@NonNull TargetingParams targetingParams,
                                                                          @NonNull DataRestrictions dataRestrictions) {
                return null;
            }
        };
        adRequest.auctionResult = new AuctionResult() {
            @NonNull
            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getNetworkName() {
                return "TestNetwork";
            }

            @Nullable
            @Override
            public String getDemandSource() {
                return null;
            }

            @Override
            public double getPrice() {
                return price;
            }

            @Override
            public String getSeat() {
                return null;
            }

            @NonNull
            @Override
            public String getCreativeId() {
                return null;
            }

            @Nullable
            @Override
            public String getCid() {
                return null;
            }

            @Nullable
            @Override
            public String[] getAdDomains() {
                return new String[0];
            }
        };
        return adRequest;
    }

    private Map<String, String> createMapWithId(final String id) {
        return new HashMap<String, String>() {{
            put(BidMachineFetcher.KEY_ID, id);
        }};
    }

}
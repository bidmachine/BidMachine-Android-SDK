package io.bidmachine;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import io.bidmachine.models.AuctionResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BidMachineFetcherTest {

    @Before
    public void setUp() throws Exception {
        BidMachineFetcher.cachedRequests = new EnumMap<>(AdsType.class);
    }

    @Test
    public void fetch() {
        TestAdRequest adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionId("test_banner_id_1")
                .setAuctionPrice(0.001000001)
                .build();
        Map<String, String> params = BidMachineFetcher.fetch(adRequest);
        assertNotNull(params);
        assertEquals("test_banner_id_1", params.get(BidMachineFetcher.KEY_ID));
        assertEquals("0.01", params.get(BidMachineFetcher.KEY_PRICE));
        assertEquals(1, BidMachineFetcher.cachedRequests.size());
        Map<String, AdRequest> requestMap = BidMachineFetcher.cachedRequests.get(AdsType.Banner);
        assertNotNull(requestMap);
        assertEquals(1, requestMap.size());

        adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionId("test_banner_id_2")
                .setAuctionPrice(120.002)
                .build();
        params = BidMachineFetcher.fetch(adRequest);
        assertNotNull(params);
        assertEquals("test_banner_id_2", params.get(BidMachineFetcher.KEY_ID));
        assertEquals("120.01", params.get(BidMachineFetcher.KEY_PRICE));
        assertEquals(1, BidMachineFetcher.cachedRequests.size());
        requestMap = BidMachineFetcher.cachedRequests.get(AdsType.Banner);
        assertNotNull(requestMap);
        assertEquals(2, requestMap.size());

        adRequest = new TestAdRequest.Builder(AdsType.Interstitial)
                .setAuctionId("test_interstitial_id_1")
                .setAuctionPrice(100000)
                .build();
        params = BidMachineFetcher.fetch(adRequest);
        assertNotNull(params);
        assertEquals("test_interstitial_id_1", params.get(BidMachineFetcher.KEY_ID));
        assertEquals("100000.00", params.get(BidMachineFetcher.KEY_PRICE));
        assertEquals(2, BidMachineFetcher.cachedRequests.size());
        requestMap = BidMachineFetcher.cachedRequests.get(AdsType.Interstitial);
        assertNotNull(requestMap);
        assertEquals(1, requestMap.size());
    }

    @Test
    public void fetch_adRequestExpired_removedFromStorage() throws Exception {
        TestAdRequest adRequest1 = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionId("test_banner_id")
                .setAuctionPrice(0.01)
                .build();
        TestAdRequest adRequest2 = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionId("test_banner_id")
                .setAuctionPrice(0.01)
                .build();
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
    public void fetch_adRequestDestroyed_removedFromStorage() throws Exception {
        TestAdRequest adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionId("test_banner_id")
                .setAuctionPrice(0.01)
                .build();
        BidMachineFetcher.fetch(adRequest);
        Map<String, AdRequest> requestMap = BidMachineFetcher.cachedRequests.get(AdsType.Banner);
        assertNotNull(requestMap);
        assertEquals(1, requestMap.size());

        adRequest.destroy();
        requestMap = BidMachineFetcher.cachedRequests.get(AdsType.Banner);
        assertNotNull(requestMap);
        assertEquals(0, requestMap.size());
    }

    @Test
    public void release1() {
        TestAdRequest adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionId("test_banner_id_1")
                .setAuctionPrice(0.001)
                .build();
        BidMachineFetcher.fetch(adRequest);
        adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionId("test_banner_id_2")
                .setAuctionPrice(0.002)
                .build();
        BidMachineFetcher.fetch(adRequest);
        adRequest = new TestAdRequest.Builder(AdsType.Interstitial)
                .setAuctionId("test_interstitial_id_1")
                .setAuctionPrice(0.003)
                .build();
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
        TestAdRequest adRequest1 = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionId("test_banner_id_1")
                .setAuctionPrice(0.001)
                .build();
        BidMachineFetcher.fetch(adRequest1);
        TestAdRequest adRequest2 = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionId("test_banner_id_2")
                .setAuctionPrice(0.002)
                .build();
        BidMachineFetcher.fetch(adRequest2);
        TestAdRequest adRequest3 = new TestAdRequest.Builder(AdsType.Interstitial)
                .setAuctionId("test_interstitial_id_1")
                .setAuctionPrice(0.003)
                .build();
        BidMachineFetcher.fetch(adRequest3);

        TestAdRequest adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionPrice(0.001)
                .build();
        TestAdRequest adRequestReleased = BidMachineFetcher.release(adRequest);
        assertNull(adRequestReleased);

        adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionId("")
                .setAuctionPrice(0.001)
                .build();
        adRequestReleased = BidMachineFetcher.release(adRequest);
        assertNull(adRequestReleased);

        adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionId("test_id_0")
                .setAuctionPrice(0.001)
                .build();
        adRequestReleased = BidMachineFetcher.release(adRequest);
        assertNull(adRequestReleased);

        adRequest = new TestAdRequest.Builder(AdsType.Native)
                .setAuctionId("test_banner_id_1")
                .setAuctionPrice(0.001)
                .build();
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
        TestAdRequest adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionId("test_banner_id_1")
                .setAuctionPrice(0.001)
                .build();
        BidMachineFetcher.fetch(adRequest);
        adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionId("test_banner_id_2")
                .setAuctionPrice(0.002)
                .build();
        BidMachineFetcher.fetch(adRequest);
        adRequest = new TestAdRequest.Builder(AdsType.Interstitial)
                .setAuctionId("test_interstitial_id_1")
                .setAuctionPrice(0.003)
                .build();
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
    public void identifyAdType() {
        String adType = BidMachineFetcher.identifyAdType(null);
        assertNull(adType);
        adType = BidMachineFetcher.identifyAdType(CreativeFormat.Banner);
        assertEquals("display", adType);
        adType = BidMachineFetcher.identifyAdType(CreativeFormat.Video);
        assertEquals("video", adType);
        adType = BidMachineFetcher.identifyAdType(CreativeFormat.Native);
        assertEquals("native", adType);
    }

    @Test
    public void toMap_minimumParameters() {
        TestAdRequest adRequest = spy(new TestAdRequest.Builder(AdsType.Banner)
                                              .setAuctionId("test_id")
                                              .setAuctionPrice(0.25)
                                              .build());
        Map<String, String> result = BidMachineFetcher.toMap(adRequest);
        assertEquals(3, result.size());
        assertEquals("test_id", result.get(BidMachineFetcher.KEY_ID));
        assertEquals("0.25", result.get(BidMachineFetcher.KEY_PRICE));
        assertEquals("test_network", result.get(BidMachineFetcher.KEY_NETWORK_KEY));
        assertFalse(result.containsKey(BidMachineFetcher.KEY_AD_TYPE));
    }

    @Test
    public void toMap_maximumParameters() {
        Map<String, String> customParams = new HashMap<>();
        customParams.put("custom_key_1", "custom_value_1");
        customParams.put("custom_key_2", "custom_value_2");
        customParams.put("custom_key_3", "custom_value_3");
        TestAdRequest adRequest = spy(new TestAdRequest.Builder(AdsType.Banner)
                                              .setAuctionId("test_id")
                                              .setAuctionPrice(0.25)
                                              .setAuctionNetworkName("test_network_name")
                                              .setAuctionCreativeFormat(CreativeFormat.Banner)
                                              .setAuctionCustomParams(customParams)
                                              .build());
        Map<String, String> result = BidMachineFetcher.toMap(adRequest);
        assertEquals(7, result.size());
        assertEquals("test_id", result.get(BidMachineFetcher.KEY_ID));
        assertEquals("0.25", result.get(BidMachineFetcher.KEY_PRICE));
        assertEquals("test_network_name", result.get(BidMachineFetcher.KEY_NETWORK_KEY));
        assertEquals("display", result.get(BidMachineFetcher.KEY_AD_TYPE));
        assertEquals("custom_value_1", result.get("custom_key_1"));
        assertEquals("custom_value_2", result.get("custom_key_2"));
        assertEquals("custom_value_3", result.get("custom_key_3"));
    }

    @Test
    public void toMap_variousPrice_convertedToStringPrice() {
        TestAdRequest adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionPrice(1000000)
                .build();
        Map<String, String> result = BidMachineFetcher.toMap(adRequest);
        assertEquals("1000000.00", result.get(BidMachineFetcher.KEY_PRICE));

        adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionPrice(0)
                .build();
        result = BidMachineFetcher.toMap(adRequest);
        assertEquals("0.00", result.get(BidMachineFetcher.KEY_PRICE));

        adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionPrice(00.00)
                .build();
        result = BidMachineFetcher.toMap(adRequest);
        assertEquals("0.00", result.get(BidMachineFetcher.KEY_PRICE));

        adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionPrice(0.01)
                .build();
        result = BidMachineFetcher.toMap(adRequest);
        assertEquals("0.01", result.get(BidMachineFetcher.KEY_PRICE));

        adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionPrice(0.001)
                .build();
        result = BidMachineFetcher.toMap(adRequest);
        assertEquals("0.01", result.get(BidMachineFetcher.KEY_PRICE));

        adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionPrice(0.050000001)
                .build();
        result = BidMachineFetcher.toMap(adRequest);
        assertEquals("0.06", result.get(BidMachineFetcher.KEY_PRICE));

        adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionPrice(1000000.050000001)
                .build();
        result = BidMachineFetcher.toMap(adRequest);
        assertEquals("1000000.06", result.get(BidMachineFetcher.KEY_PRICE));

        adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionPrice(0001000000.050000001)
                .build();
        result = BidMachineFetcher.toMap(adRequest);
        assertEquals("1000000.06", result.get(BidMachineFetcher.KEY_PRICE));
    }


    private Map<String, String> createMapWithId(final String id) {
        return new HashMap<String, String>() {{
            put(BidMachineFetcher.KEY_ID, id);
        }};
    }

}
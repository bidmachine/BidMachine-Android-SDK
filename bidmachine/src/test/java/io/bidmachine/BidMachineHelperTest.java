package io.bidmachine;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

public class BidMachineHelperTest {

    @Test
    public void identifyAdType() {
        String adType = BidMachineHelper.identifyAdType(null);
        assertNull(adType);
        adType = BidMachineHelper.identifyAdType(CreativeFormat.Banner);
        assertEquals("display", adType);
        adType = BidMachineHelper.identifyAdType(CreativeFormat.Video);
        assertEquals("video", adType);
        adType = BidMachineHelper.identifyAdType(CreativeFormat.Native);
        assertEquals("native", adType);
    }

    @Test
    public void toMap_minimumParameters() {
        AdRequest adRequest = spy(new TestAdRequest.Builder(AdsType.Banner)
                                          .setAuctionId("test_id")
                                          .setAuctionPrice(0.25)
                                          .build());
        Map<String, String> map = BidMachineHelper.toMap(adRequest);
        assertEquals(3, map.size());
        assertEquals("test_id", map.get(BidMachineFetcher.KEY_ID));
        assertEquals("0.25", map.get(BidMachineFetcher.KEY_PRICE));
        assertEquals("test_network", map.get(BidMachineFetcher.KEY_NETWORK_KEY));
        assertFalse(map.containsKey(BidMachineFetcher.KEY_AD_TYPE));
    }

    @Test
    public void toMap_maximumParameters() {
        Map<String, String> customParams = new HashMap<>();
        customParams.put("custom_key_1", "custom_value_1");
        customParams.put("custom_key_2", "custom_value_2");
        customParams.put("custom_key_3", "custom_value_3");
        AdRequest adRequest = spy(new TestAdRequest.Builder(AdsType.Banner)
                                          .setAuctionId("test_id")
                                          .setAuctionPrice(0.25)
                                          .setAuctionNetworkName("test_network_name")
                                          .setAuctionCreativeFormat(CreativeFormat.Banner)
                                          .setAuctionCustomParams(customParams)
                                          .build());
        Map<String, String> map = BidMachineHelper.toMap(adRequest);
        assertEquals(7, map.size());
        assertEquals("test_id", map.get(BidMachineFetcher.KEY_ID));
        assertEquals("0.25", map.get(BidMachineFetcher.KEY_PRICE));
        assertEquals("test_network_name", map.get(BidMachineFetcher.KEY_NETWORK_KEY));
        assertEquals("display", map.get(BidMachineFetcher.KEY_AD_TYPE));
        assertEquals("custom_value_1", map.get("custom_key_1"));
        assertEquals("custom_value_2", map.get("custom_key_2"));
        assertEquals("custom_value_3", map.get("custom_key_3"));
    }

    @Test
    public void moPub_toKeywordsWithAdRequestMinimumParameters() {
        AdRequest adRequest = spy(new TestAdRequest.Builder(AdsType.Banner)
                                          .setAuctionId("test_id")
                                          .setAuctionPrice(0.25)
                                          .build());
        String moPubKeywords = BidMachineHelper.MoPub.toKeywords(adRequest);
        assertEquals(3, moPubKeywords.split(",").length);
        assertTrue(moPubKeywords.contains("bm_id:test_id"));
        assertTrue(moPubKeywords.contains("bm_pf:0.25"));
        assertTrue(moPubKeywords.contains("bm_network_key:test_network"));
    }

    @Test
    public void moPub_toKeywordsWithAdRequestMaximumParameters() {
        Map<String, String> customParams = new HashMap<>();
        customParams.put("custom_key_1", "custom_value_1");
        customParams.put("custom_key_2", "custom_value_2");
        customParams.put("custom_key_3", "custom_value_3");
        AdRequest adRequest = spy(new TestAdRequest.Builder(AdsType.Banner)
                                          .setAuctionId("test_id")
                                          .setAuctionPrice(0.25)
                                          .setAuctionNetworkName("test_network_name")
                                          .setAuctionCreativeFormat(CreativeFormat.Banner)
                                          .setAuctionCustomParams(customParams)
                                          .build());
        String moPubKeywords = BidMachineHelper.MoPub.toKeywords(adRequest);
        assertEquals(7, moPubKeywords.split(",").length);
        assertTrue(moPubKeywords.contains("bm_id:test_id"));
        assertTrue(moPubKeywords.contains("bm_pf:0.25"));
        assertTrue(moPubKeywords.contains("bm_network_key:test_network_name"));
        assertTrue(moPubKeywords.contains("bm_ad_type:display"));
        assertTrue(moPubKeywords.contains("custom_key_1:custom_value_1"));
        assertTrue(moPubKeywords.contains("custom_key_2:custom_value_2"));
        assertTrue(moPubKeywords.contains("custom_key_3:custom_value_3"));
    }

}
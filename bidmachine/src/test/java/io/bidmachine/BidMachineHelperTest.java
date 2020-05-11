package io.bidmachine;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.ads.networks.mraid.MraidAdapter;
import io.bidmachine.ads.networks.vast.VastAdapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

public class BidMachineHelperTest {

    @Test
    public void identifyAdType_withoutAdmFormat() {
        TestAdRequest adRequest = spy(new TestAdRequest.Builder(AdsType.Banner)
                                              .build());
        String networkName = adRequest.getAuctionResult().getNetworkKey();
        String adType = BidMachineHelper.identifyAdType(networkName);
        assertNull(adType);
    }

    @Test
    public void identifyAdType_admFormatIsMraid() {
        TestAdRequest adRequest = spy(new TestAdRequest.Builder(AdsType.Banner)
                                              .setAuctionNetworkName(MraidAdapter.KEY)
                                              .build());
        String networkName = adRequest.getAuctionResult().getNetworkKey();
        String adType = BidMachineHelper.identifyAdType(networkName);
        assertEquals("display", adType);
    }

    @Test
    public void identifyAdType_admFormatIsVast() {
        TestAdRequest adRequest = spy(new TestAdRequest.Builder(AdsType.Banner)
                                              .setAuctionNetworkName(VastAdapter.KEY)
                                              .build());
        String networkName = adRequest.getAuctionResult().getNetworkKey();
        String adType = BidMachineHelper.identifyAdType(networkName);
        assertEquals("video", adType);
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
                                          .setAuctionNetworkName(MraidAdapter.KEY)
                                          .setAuctionCustomParams(customParams)
                                          .build());
        Map<String, String> map = BidMachineHelper.toMap(adRequest);
        assertEquals(7, map.size());
        assertEquals("test_id", map.get(BidMachineFetcher.KEY_ID));
        assertEquals("0.25", map.get(BidMachineFetcher.KEY_PRICE));
        assertEquals(MraidAdapter.KEY, map.get(BidMachineFetcher.KEY_NETWORK_KEY));
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
                                          .setAuctionNetworkName(MraidAdapter.KEY)
                                          .setAuctionCustomParams(customParams)
                                          .build());
        String moPubKeywords = BidMachineHelper.MoPub.toKeywords(adRequest);
        assertEquals(7, moPubKeywords.split(",").length);
        assertTrue(moPubKeywords.contains("bm_id:test_id"));
        assertTrue(moPubKeywords.contains("bm_pf:0.25"));
        assertTrue(moPubKeywords.contains("bm_network_key:mraid"));
        assertTrue(moPubKeywords.contains("bm_ad_type:display"));
        assertTrue(moPubKeywords.contains("custom_key_1:custom_value_1"));
        assertTrue(moPubKeywords.contains("custom_key_2:custom_value_2"));
        assertTrue(moPubKeywords.contains("custom_key_3:custom_value_3"));
    }

}
package io.bidmachine;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import io.bidmachine.banner.BannerBridge;
import io.bidmachine.interstitial.InterstitialAd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BidMachineAdTest {

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        BidMachine.initialize(context, "5");
        for (AdsType adsType : AdsType.values()) {
            BidMachineImpl.get().getSessionAdParams(adsType).clear();
        }
    }

    @Test
    public void impressionCountByAdsType_oneImpressionPerAdInstance() {
        assertNull(BidMachineImpl.get()
                           .getSessionAdParams(AdsType.Banner)
                           .getImpressionCount());
        assertNull(BidMachineImpl.get()
                           .getSessionAdParams(AdsType.Interstitial)
                           .getImpressionCount());
        assertNull(BidMachineImpl.get()
                           .getSessionAdParams(AdsType.Rewarded)
                           .getImpressionCount());

        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        BidMachineAd interstitialAd = new InterstitialAd(context);
        bannerAd.processCallback.processImpression();
        bannerAd.processCallback.processImpression();
        interstitialAd.processCallback.processImpression();
        interstitialAd.processCallback.processImpression();

        SessionAdParams sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getImpressionCount());
        assertEquals(1, sessionAdParams.getImpressionCount().intValue());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getImpressionCount());
        assertEquals(1, sessionAdParams.getImpressionCount().intValue());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getImpressionCount());

        bannerAd = BannerBridge.createBannerAd(context);
        interstitialAd = new InterstitialAd(context);
        bannerAd.processCallback.processImpression();
        bannerAd.processCallback.processImpression();
        interstitialAd.processCallback.processImpression();
        interstitialAd.processCallback.processImpression();

        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getImpressionCount());
        assertEquals(2, sessionAdParams.getImpressionCount().intValue());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getImpressionCount());
        assertEquals(2, sessionAdParams.getImpressionCount().intValue());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getImpressionCount());
    }

    @Test
    public void clickCountByAdsType_oneClickPerAdInstance() {
        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Banner)
                .getClickCount());
        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Interstitial)
                .getClickCount());
        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Rewarded)
                .getClickCount());

        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        BidMachineAd interstitialAd = new InterstitialAd(context);
        bannerAd.processCallback.processClicked();
        bannerAd.processCallback.processClicked();
        interstitialAd.processCallback.processClicked();
        interstitialAd.processCallback.processClicked();

        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Banner)
                .getClickCount());
        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Interstitial)
                .getClickCount());
        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Rewarded)
                .getClickCount());

        bannerAd.processCallback.processImpression();
        interstitialAd.processCallback.processImpression();

        assertEquals(1, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Banner)
                .getClickCount());
        assertEquals(1, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Interstitial)
                .getClickCount());
        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Rewarded)
                .getClickCount());

        bannerAd = BannerBridge.createBannerAd(context);
        interstitialAd = new InterstitialAd(context);
        bannerAd.processCallback.processClicked();
        bannerAd.processCallback.processClicked();
        bannerAd.processCallback.processImpression();
        interstitialAd.processCallback.processClicked();
        interstitialAd.processCallback.processClicked();
        interstitialAd.processCallback.processImpression();

        assertEquals(2, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Banner)
                .getClickCount());
        assertEquals(2, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Interstitial)
                .getClickCount());
        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Rewarded)
                .getClickCount());
    }

    @Test
    public void clickRateByAdsType() {
        assertNull(BidMachineImpl.get()
                           .getSessionAdParams(AdsType.Banner)
                           .getClickRate());
        assertNull(BidMachineImpl.get()
                           .getSessionAdParams(AdsType.Interstitial)
                           .getClickRate());
        assertNull(BidMachineImpl.get()
                           .getSessionAdParams(AdsType.Rewarded)
                           .getClickRate());

        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        BidMachineAd interstitialAd = new InterstitialAd(context);
        bannerAd.processCallback.processImpression();
        interstitialAd.processCallback.processImpression();

        SessionAdParams sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getClickRate());
        assertEquals(0.0F, sessionAdParams.getClickRate(), 0);
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getClickRate());
        assertEquals(0.0F, sessionAdParams.getClickRate(), 0);
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getClickRate());

        bannerAd.processCallback.processClicked();
        interstitialAd.processCallback.processClicked();

        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getClickRate());
        assertEquals(100.0F, sessionAdParams.getClickRate(), 0);
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getClickRate());
        assertEquals(100.0F, sessionAdParams.getClickRate(), 0);
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getClickRate());

        bannerAd.processCallback.processClicked();
        interstitialAd.processCallback.processClicked();

        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getClickRate());
        assertEquals(100.0F, sessionAdParams.getClickRate(), 0);
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getClickRate());
        assertEquals(100.0F, sessionAdParams.getClickRate(), 0);
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getClickRate());

        bannerAd = BannerBridge.createBannerAd(context);
        interstitialAd = new InterstitialAd(context);
        bannerAd.processCallback.processImpression();
        interstitialAd.processCallback.processImpression();

        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getClickRate());
        assertEquals(50.0F, sessionAdParams.getClickRate(), 0);
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getClickRate());
        assertEquals(50.0F, sessionAdParams.getClickRate(), 0);
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getClickRate());
    }

    @Test
    public void isUserClickedOnLastAd_impressionBeforeClick() {
        assertNull(BidMachineImpl.get()
                           .getSessionAdParams(AdsType.Banner)
                           .getUserClickedOnLastAd());
        assertNull(BidMachineImpl.get()
                           .getSessionAdParams(AdsType.Interstitial)
                           .getUserClickedOnLastAd());
        assertNull(BidMachineImpl.get()
                           .getSessionAdParams(AdsType.Rewarded)
                           .getUserClickedOnLastAd());

        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        BidMachineAd interstitialAd = new InterstitialAd(context);
        bannerAd.processCallback.processImpression();
        interstitialAd.processCallback.processImpression();

        SessionAdParams sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Banner);
        assertNull(sessionAdParams.getUserClickedOnLastAd());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Interstitial);
        assertNull(sessionAdParams.getUserClickedOnLastAd());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getUserClickedOnLastAd());

        bannerAd.processCallback.processClicked();
        interstitialAd.processCallback.processClicked();

        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getUserClickedOnLastAd());
        assertTrue(sessionAdParams.getUserClickedOnLastAd());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getUserClickedOnLastAd());
        assertTrue(sessionAdParams.getUserClickedOnLastAd());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getUserClickedOnLastAd());
    }

    @Test
    public void isUserClickedOnLastAd_clickBeforeImpression() {
        assertNull(BidMachineImpl.get()
                           .getSessionAdParams(AdsType.Banner)
                           .getUserClickedOnLastAd());
        assertNull(BidMachineImpl.get()
                           .getSessionAdParams(AdsType.Interstitial)
                           .getUserClickedOnLastAd());
        assertNull(BidMachineImpl.get()
                           .getSessionAdParams(AdsType.Rewarded)
                           .getUserClickedOnLastAd());

        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        BidMachineAd interstitialAd = new InterstitialAd(context);
        bannerAd.processCallback.processClicked();
        interstitialAd.processCallback.processClicked();

        SessionAdParams sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Banner);
        assertNull(sessionAdParams.getUserClickedOnLastAd());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Interstitial);
        assertNull(sessionAdParams.getUserClickedOnLastAd());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getUserClickedOnLastAd());

        bannerAd.processCallback.processImpression();
        interstitialAd.processCallback.processImpression();

        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getUserClickedOnLastAd());
        assertTrue(sessionAdParams.getUserClickedOnLastAd());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getUserClickedOnLastAd());
        assertTrue(sessionAdParams.getUserClickedOnLastAd());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getUserClickedOnLastAd());
    }

    @Test
    public void videoImpressionCountByAdsType_oneImpressionPerAdInstance() {
        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Banner)
                .getVideoImpressionCount());
        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Interstitial)
                .getVideoImpressionCount());
        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Rewarded)
                .getVideoImpressionCount());

        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionCreativeFormat(CreativeFormat.Banner)
                .build();
        BidMachineAd interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Interstitial)
                .setAuctionCreativeFormat(CreativeFormat.Video)
                .build();
        bannerAd.processCallback.processImpression();
        bannerAd.processCallback.processImpression();
        interstitialAd.processCallback.processImpression();
        interstitialAd.processCallback.processImpression();

        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Banner)
                .getVideoImpressionCount());
        assertEquals(1, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Interstitial)
                .getVideoImpressionCount());
        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Rewarded)
                .getVideoImpressionCount());

        bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionCreativeFormat(CreativeFormat.Banner)
                .build();
        interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Interstitial)
                .setAuctionCreativeFormat(CreativeFormat.Video)
                .build();
        bannerAd.processCallback.processImpression();
        bannerAd.processCallback.processImpression();
        interstitialAd.processCallback.processImpression();
        interstitialAd.processCallback.processImpression();

        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Banner)
                .getVideoImpressionCount());
        assertEquals(2, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Interstitial)
                .getVideoImpressionCount());
        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Rewarded)
                .getVideoImpressionCount());

        bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionCreativeFormat(CreativeFormat.Banner)
                .build();
        interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Interstitial)
                .setAuctionCreativeFormat(CreativeFormat.Banner)
                .build();
        bannerAd.processCallback.processImpression();
        bannerAd.processCallback.processImpression();
        interstitialAd.processCallback.processImpression();
        interstitialAd.processCallback.processImpression();

        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Banner)
                .getVideoImpressionCount());
        assertEquals(2, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Interstitial)
                .getVideoImpressionCount());
        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Rewarded)
                .getVideoImpressionCount());
    }

    @Test
    public void completedVideosCountByAdsType_oneCompletedVideoPerAdInstance() {
        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Banner)
                .getCompletedVideosCount());
        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Interstitial)
                .getCompletedVideosCount());
        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Rewarded)
                .getCompletedVideosCount());

        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionCreativeFormat(CreativeFormat.Banner)
                .build();
        BidMachineAd interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Interstitial)
                .setAuctionCreativeFormat(CreativeFormat.Video)
                .build();
        bannerAd.processCallback.processFinished();
        bannerAd.processCallback.processFinished();
        interstitialAd.processCallback.processFinished();
        interstitialAd.processCallback.processFinished();

        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Banner)
                .getCompletedVideosCount());
        assertEquals(1, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Interstitial)
                .getCompletedVideosCount());
        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Rewarded)
                .getCompletedVideosCount());

        bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionCreativeFormat(CreativeFormat.Banner)
                .build();
        interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Interstitial)
                .setAuctionCreativeFormat(CreativeFormat.Video)
                .build();
        bannerAd.processCallback.processFinished();
        bannerAd.processCallback.processFinished();
        interstitialAd.processCallback.processFinished();
        interstitialAd.processCallback.processFinished();

        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Banner)
                .getCompletedVideosCount());
        assertEquals(2, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Interstitial)
                .getCompletedVideosCount());
        assertEquals(0, BidMachineImpl.get()
                .getSessionAdParams(AdsType.Rewarded)
                .getCompletedVideosCount());
    }

    @Test
    public void completionRateByAdsType() {
        assertNull(BidMachineImpl.get()
                           .getSessionAdParams(AdsType.Banner)
                           .getCompletionRate());
        assertNull(BidMachineImpl.get()
                           .getSessionAdParams(AdsType.Interstitial)
                           .getCompletionRate());
        assertNull(BidMachineImpl.get()
                           .getSessionAdParams(AdsType.Rewarded)
                           .getCompletionRate());

        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionCreativeFormat(CreativeFormat.Banner)
                .build();
        BidMachineAd interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Interstitial)
                .setAuctionCreativeFormat(CreativeFormat.Video)
                .build();
        bannerAd.processCallback.processImpression();
        interstitialAd.processCallback.processImpression();

        SessionAdParams sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Banner);
        assertNull(sessionAdParams.getCompletionRate());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getCompletionRate());
        assertEquals(0.0F, sessionAdParams.getCompletionRate(), 0);
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getCompletionRate());

        bannerAd.processCallback.processFinished();
        interstitialAd.processCallback.processFinished();

        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Banner);
        assertNull(sessionAdParams.getCompletionRate());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getCompletionRate());
        assertEquals(100.0F, sessionAdParams.getCompletionRate(), 0);
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getCompletionRate());

        bannerAd.processCallback.processFinished();
        interstitialAd.processCallback.processFinished();

        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Banner);
        assertNull(sessionAdParams.getCompletionRate());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getCompletionRate());
        assertEquals(100.0F, sessionAdParams.getCompletionRate(), 0);
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getCompletionRate());

        bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionCreativeFormat(CreativeFormat.Banner)
                .build();
        interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Interstitial)
                .setAuctionCreativeFormat(CreativeFormat.Video)
                .build();
        bannerAd.processCallback.processImpression();
        interstitialAd.processCallback.processImpression();

        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Banner);
        assertNull(sessionAdParams.getCompletionRate());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getCompletionRate());
        assertEquals(50.0F, sessionAdParams.getCompletionRate(), 0);
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getCompletionRate());
    }

    @Test
    public void lastAdDomain_adNotContainsAdDomain_returnNull() {
        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner).build();
        BidMachineAd interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Interstitial).build();
        bannerAd.processCallback.processImpression();
        interstitialAd.processCallback.processImpression();

        SessionAdParams sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Banner);
        assertNull(sessionAdParams.getLastAdDomain());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Interstitial);
        assertNull(sessionAdParams.getLastAdDomain());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getLastAdDomain());
    }

    @Test
    public void lastAdDomain_adContainsAdDomain_returnFirstAdDomain() {
        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAdDomain(new ArrayList<String>() {{
                    add("test_banner_ad_domain_1");
                    add("test_banner_ad_domain_2");
                    add("test_banner_ad_domain_3");
                }})
                .build();
        BidMachineAd interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAdDomain(new ArrayList<String>() {{
                    add("test_interstitial_ad_domain_1");
                    add("test_interstitial_ad_domain_2");
                    add("test_interstitial_ad_domain_3");
                }})
                .build();
        bannerAd.processCallback.processImpression();
        interstitialAd.processCallback.processImpression();

        SessionAdParams sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getLastAdDomain());
        assertEquals("test_banner_ad_domain_1", sessionAdParams.getLastAdDomain());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getLastAdDomain());
        assertEquals("test_interstitial_ad_domain_1", sessionAdParams.getLastAdDomain());
        sessionAdParams = BidMachineImpl.get().getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getLastAdDomain());
    }

}
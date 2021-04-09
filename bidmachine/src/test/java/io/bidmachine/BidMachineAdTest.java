package io.bidmachine;

import android.content.Context;

import androidx.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.bidmachine.banner.BannerBridge;
import io.bidmachine.interstitial.InterstitialAd;
import io.bidmachine.interstitial.InterstitialRequest;
import io.bidmachine.utils.BMError;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BidMachineAdTest {

    private Context context;
    private SessionManager sessionManager;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        BidMachine.initialize(context, "5");

        sessionManager = SessionManager.get();
        sessionManager.startNewSession();
    }

    @Test
    public void load_requestIsNull_onAdLoadFailedWithNoRequest() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        InterstitialAd interstitialAd = new InterstitialAd(context);
        interstitialAd.setListener(new SimpleInterstitialListener() {
            @Override
            public void onAdLoadFailed(@NonNull InterstitialAd ad, @NonNull BMError error) {
                String message = error.getMessage();
                if (message != null && message.contains("No Request")) {
                    countDownLatch.countDown();
                }
            }
        });
        interstitialAd.load(null);

        assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void load_requestWillDestroyed_onAdLoadFailedWithDestroyed() {
        InterstitialRequest interstitialRequest = new InterstitialRequest.Builder().build();
        InterstitialAd interstitialAd = new InterstitialAd(context);
        interstitialAd.load(interstitialRequest);
        interstitialRequest.destroy();

        assertTrue(interstitialAd.isDestroyed());
    }

    @Test
    public void load_requestWasShown_onAdLoadFailedWithAlreadyShown() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        TestAdRequest testAdRequest = new TestAdRequest.Builder(AdsType.Interstitial).build();
        testAdRequest.onShown();
        BidMachineAd bidMachineAd = new InterstitialAd(context);
        bidMachineAd.setListener(new SimpleInterstitialListener() {
            @Override
            public void onAdLoadFailed(@NonNull InterstitialAd ad, @NonNull BMError error) {
                if ("AdRequest already shown".equals(error.getBrief())) {
                    countDownLatch.countDown();
                }
            }
        });
        bidMachineAd.load(testAdRequest);
        assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void impressionCountByAdsType_oneImpressionPerAdInstance() {
        assertNull(sessionManager
                           .getSessionAdParams(AdsType.Banner)
                           .getImpressionCount());
        assertNull(sessionManager
                           .getSessionAdParams(AdsType.Interstitial)
                           .getImpressionCount());
        assertNull(sessionManager
                           .getSessionAdParams(AdsType.Rewarded)
                           .getImpressionCount());

        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        BidMachineAd interstitialAd = new InterstitialAd(context);
        bannerAd.processCallback.processShown();
        bannerAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();

        SessionAdParams sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getImpressionCount());
        assertEquals(1, sessionAdParams.getImpressionCount().intValue());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getImpressionCount());
        assertEquals(1, sessionAdParams.getImpressionCount().intValue());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getImpressionCount());

        bannerAd = BannerBridge.createBannerAd(context);
        interstitialAd = new InterstitialAd(context);
        bannerAd.processCallback.processShown();
        bannerAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();

        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getImpressionCount());
        assertEquals(2, sessionAdParams.getImpressionCount().intValue());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getImpressionCount());
        assertEquals(2, sessionAdParams.getImpressionCount().intValue());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getImpressionCount());
    }

    @Test
    public void clickCountByAdsType_oneClickPerAdInstance() {
        assertEquals(0, sessionManager
                .getSessionAdParams(AdsType.Banner)
                .getClickCount());
        assertEquals(0, sessionManager
                .getSessionAdParams(AdsType.Interstitial)
                .getClickCount());
        assertEquals(0, sessionManager
                .getSessionAdParams(AdsType.Rewarded)
                .getClickCount());

        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        BidMachineAd interstitialAd = new InterstitialAd(context);
        bannerAd.processCallback.processClicked();
        bannerAd.processCallback.processClicked();
        interstitialAd.processCallback.processClicked();
        interstitialAd.processCallback.processClicked();

        assertEquals(1, sessionManager
                .getSessionAdParams(AdsType.Banner)
                .getClickCount());
        assertEquals(1, sessionManager
                .getSessionAdParams(AdsType.Interstitial)
                .getClickCount());
        assertEquals(0, sessionManager
                .getSessionAdParams(AdsType.Rewarded)
                .getClickCount());

        bannerAd = BannerBridge.createBannerAd(context);
        interstitialAd = new InterstitialAd(context);
        bannerAd.processCallback.processClicked();
        bannerAd.processCallback.processClicked();
        interstitialAd.processCallback.processClicked();
        interstitialAd.processCallback.processClicked();

        assertEquals(2, sessionManager
                .getSessionAdParams(AdsType.Banner)
                .getClickCount());
        assertEquals(2, sessionManager
                .getSessionAdParams(AdsType.Interstitial)
                .getClickCount());
        assertEquals(0, sessionManager
                .getSessionAdParams(AdsType.Rewarded)
                .getClickCount());
    }

    @Test
    public void clickRateByAdsType() {
        assertNull(sessionManager
                           .getSessionAdParams(AdsType.Banner)
                           .getClickRate());
        assertNull(sessionManager
                           .getSessionAdParams(AdsType.Interstitial)
                           .getClickRate());
        assertNull(sessionManager
                           .getSessionAdParams(AdsType.Rewarded)
                           .getClickRate());

        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        BidMachineAd interstitialAd = new InterstitialAd(context);
        bannerAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();

        SessionAdParams sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getClickRate());
        assertEquals(0.0F, sessionAdParams.getClickRate(), 0);
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getClickRate());
        assertEquals(0.0F, sessionAdParams.getClickRate(), 0);
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getClickRate());

        bannerAd.processCallback.processClicked();
        interstitialAd.processCallback.processClicked();

        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getClickRate());
        assertEquals(100.0F, sessionAdParams.getClickRate(), 0);
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getClickRate());
        assertEquals(100.0F, sessionAdParams.getClickRate(), 0);
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getClickRate());

        bannerAd.processCallback.processClicked();
        interstitialAd.processCallback.processClicked();

        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getClickRate());
        assertEquals(100.0F, sessionAdParams.getClickRate(), 0);
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getClickRate());
        assertEquals(100.0F, sessionAdParams.getClickRate(), 0);
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getClickRate());

        bannerAd = BannerBridge.createBannerAd(context);
        interstitialAd = new InterstitialAd(context);
        bannerAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();

        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getClickRate());
        assertEquals(50.0F, sessionAdParams.getClickRate(), 0);
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getClickRate());
        assertEquals(50.0F, sessionAdParams.getClickRate(), 0);
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getClickRate());
    }

    @Test
    public void isUserClickedOnLastAd() {
        assertNull(sessionManager
                           .getSessionAdParams(AdsType.Banner)
                           .getUserClickedOnLastAd());
        assertNull(sessionManager
                           .getSessionAdParams(AdsType.Interstitial)
                           .getUserClickedOnLastAd());
        assertNull(sessionManager
                           .getSessionAdParams(AdsType.Rewarded)
                           .getUserClickedOnLastAd());

        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        BidMachineAd interstitialAd = new InterstitialAd(context);
        bannerAd.processCallback.processClicked();
        interstitialAd.processCallback.processClicked();

        SessionAdParams sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getUserClickedOnLastAd());
        assertTrue(sessionAdParams.getUserClickedOnLastAd());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getUserClickedOnLastAd());
        assertTrue(sessionAdParams.getUserClickedOnLastAd());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getUserClickedOnLastAd());
    }

    @Test
    public void videoImpressionCountByAdsType_oneImpressionPerAdInstance() {
        assertEquals(0, sessionManager
                .getSessionAdParams(AdsType.Banner)
                .getVideoImpressionCount());
        assertEquals(0, sessionManager
                .getSessionAdParams(AdsType.Interstitial)
                .getVideoImpressionCount());
        assertEquals(0, sessionManager
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
        bannerAd.processCallback.processShown();
        bannerAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();

        assertEquals(0, sessionManager
                .getSessionAdParams(AdsType.Banner)
                .getVideoImpressionCount());
        assertEquals(1, sessionManager
                .getSessionAdParams(AdsType.Interstitial)
                .getVideoImpressionCount());
        assertEquals(0, sessionManager
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
        bannerAd.processCallback.processShown();
        bannerAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();

        assertEquals(0, sessionManager
                .getSessionAdParams(AdsType.Banner)
                .getVideoImpressionCount());
        assertEquals(2, sessionManager
                .getSessionAdParams(AdsType.Interstitial)
                .getVideoImpressionCount());
        assertEquals(0, sessionManager
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
        bannerAd.processCallback.processShown();
        bannerAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();

        assertEquals(0, sessionManager
                .getSessionAdParams(AdsType.Banner)
                .getVideoImpressionCount());
        assertEquals(2, sessionManager
                .getSessionAdParams(AdsType.Interstitial)
                .getVideoImpressionCount());
        assertEquals(0, sessionManager
                .getSessionAdParams(AdsType.Rewarded)
                .getVideoImpressionCount());
    }

    @Test
    public void completedVideosCountByAdsType_oneCompletedVideoPerAdInstance() {
        assertEquals(0, sessionManager
                .getSessionAdParams(AdsType.Banner)
                .getCompletedVideosCount());
        assertEquals(0, sessionManager
                .getSessionAdParams(AdsType.Interstitial)
                .getCompletedVideosCount());
        assertEquals(0, sessionManager
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

        assertEquals(0, sessionManager
                .getSessionAdParams(AdsType.Banner)
                .getCompletedVideosCount());
        assertEquals(1, sessionManager
                .getSessionAdParams(AdsType.Interstitial)
                .getCompletedVideosCount());
        assertEquals(0, sessionManager
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

        assertEquals(0, sessionManager
                .getSessionAdParams(AdsType.Banner)
                .getCompletedVideosCount());
        assertEquals(2, sessionManager
                .getSessionAdParams(AdsType.Interstitial)
                .getCompletedVideosCount());
        assertEquals(0, sessionManager
                .getSessionAdParams(AdsType.Rewarded)
                .getCompletedVideosCount());
    }

    @Test
    public void completionRateByAdsType() {
        assertNull(sessionManager
                           .getSessionAdParams(AdsType.Banner)
                           .getCompletionRate());
        assertNull(sessionManager
                           .getSessionAdParams(AdsType.Interstitial)
                           .getCompletionRate());
        assertNull(sessionManager
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
        bannerAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();

        SessionAdParams sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNull(sessionAdParams.getCompletionRate());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getCompletionRate());
        assertEquals(0.0F, sessionAdParams.getCompletionRate(), 0);
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getCompletionRate());

        bannerAd.processCallback.processFinished();
        interstitialAd.processCallback.processFinished();

        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNull(sessionAdParams.getCompletionRate());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getCompletionRate());
        assertEquals(100.0F, sessionAdParams.getCompletionRate(), 0);
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getCompletionRate());

        bannerAd.processCallback.processFinished();
        interstitialAd.processCallback.processFinished();

        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNull(sessionAdParams.getCompletionRate());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getCompletionRate());
        assertEquals(100.0F, sessionAdParams.getCompletionRate(), 0);
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getCompletionRate());

        bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAuctionCreativeFormat(CreativeFormat.Banner)
                .build();
        interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Interstitial)
                .setAuctionCreativeFormat(CreativeFormat.Video)
                .build();
        bannerAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();

        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNull(sessionAdParams.getCompletionRate());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getCompletionRate());
        assertEquals(50.0F, sessionAdParams.getCompletionRate(), 0);
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getCompletionRate());
    }

    @Test
    public void lastBundle_adNotContainsBundle_returnNull() {
        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner).build();
        BidMachineAd interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Interstitial).build();
        bannerAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();

        SessionAdParams sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNull(sessionAdParams.getLastBundle());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNull(sessionAdParams.getLastBundle());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getLastBundle());
    }

    @Test
    public void lastBundle_adContainsBundle_returnFirstBundle() {
        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setBundleList(new ArrayList<String>() {{
                    add("test_banner_bundle_1");
                    add("test_banner_bundle_2");
                    add("test_banner_bundle_3");
                }})
                .build();
        BidMachineAd interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setBundleList(new ArrayList<String>() {{
                    add("test_interstitial_bundle_1");
                    add("test_interstitial_bundle_2");
                    add("test_interstitial_bundle_3");
                }})
                .build();
        bannerAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();

        SessionAdParams sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getLastBundle());
        assertEquals("test_banner_bundle_1", sessionAdParams.getLastBundle());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getLastBundle());
        assertEquals("test_interstitial_bundle_1", sessionAdParams.getLastBundle());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getLastBundle());
    }

    @Test
    public void lastBundle_adContainsBundle_returnFirstNotEmptyBundle() {
        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setBundleList(new ArrayList<String>() {{
                    add("");
                    add("test_banner_bundle_2");
                    add("test_banner_bundle_3");
                }})
                .build();
        BidMachineAd interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setBundleList(new ArrayList<String>() {{
                    add("");
                    add("test_interstitial_bundle_2");
                    add("test_interstitial_bundle_3");
                }})
                .build();
        bannerAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();

        SessionAdParams sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getLastBundle());
        assertEquals("test_banner_bundle_2", sessionAdParams.getLastBundle());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getLastBundle());
        assertEquals("test_interstitial_bundle_2", sessionAdParams.getLastBundle());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getLastBundle());
    }

    @Test
    public void lastBundle_secondAdNotContainsBundle_returnNull() {
        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setBundleList(new ArrayList<String>() {{
                    add("test_banner_bundle_1");
                }})
                .build();
        BidMachineAd interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setBundleList(new ArrayList<String>() {{
                    add("test_interstitial_bundle_1");
                }})
                .build();
        bannerAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();

        SessionAdParams sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getLastBundle());
        assertEquals("test_banner_bundle_1", sessionAdParams.getLastBundle());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getLastBundle());
        assertEquals("test_interstitial_bundle_1", sessionAdParams.getLastBundle());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getLastBundle());

        bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner).build();
        interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Interstitial).build();
        bannerAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();

        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNull(sessionAdParams.getLastBundle());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNull(sessionAdParams.getLastBundle());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getLastBundle());
    }

    @Test
    public void lastAdDomain_adNotContainsAdDomain_returnNull() {
        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner).build();
        BidMachineAd interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Interstitial).build();
        bannerAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();

        SessionAdParams sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNull(sessionAdParams.getLastAdDomain());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNull(sessionAdParams.getLastAdDomain());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getLastAdDomain());
    }

    @Test
    public void lastAdDomain_adContainsAdDomain_returnFirstAdDomain() {
        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAdDomainList(new ArrayList<String>() {{
                    add("test_banner_ad_domain_1");
                    add("test_banner_ad_domain_2");
                    add("test_banner_ad_domain_3");
                }})
                .build();
        BidMachineAd interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAdDomainList(new ArrayList<String>() {{
                    add("test_interstitial_ad_domain_1");
                    add("test_interstitial_ad_domain_2");
                    add("test_interstitial_ad_domain_3");
                }})
                .build();
        bannerAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();

        SessionAdParams sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getLastAdDomain());
        assertEquals("test_banner_ad_domain_1", sessionAdParams.getLastAdDomain());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getLastAdDomain());
        assertEquals("test_interstitial_ad_domain_1", sessionAdParams.getLastAdDomain());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getLastAdDomain());
    }

    @Test
    public void lastAdDomain_adContainsAdDomain_returnFirstNotEmptyAdDomain() {
        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAdDomainList(new ArrayList<String>() {{
                    add("");
                    add("test_banner_ad_domain_2");
                    add("test_banner_ad_domain_3");
                }})
                .build();
        BidMachineAd interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAdDomainList(new ArrayList<String>() {{
                    add("");
                    add("test_interstitial_ad_domain_2");
                    add("test_interstitial_ad_domain_3");
                }})
                .build();
        bannerAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();

        SessionAdParams sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getLastAdDomain());
        assertEquals("test_banner_ad_domain_2", sessionAdParams.getLastAdDomain());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getLastAdDomain());
        assertEquals("test_interstitial_ad_domain_2", sessionAdParams.getLastAdDomain());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getLastAdDomain());
    }

    @Test
    public void lastAdDomain_secondAdNotContainsAdDomain_returnNull() {
        BidMachineAd bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAdDomainList(new ArrayList<String>() {{
                    add("test_banner_ad_domain_1");
                }})
                .build();
        BidMachineAd interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setAdDomainList(new ArrayList<String>() {{
                    add("test_interstitial_ad_domain_1");
                }})
                .build();
        bannerAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();

        SessionAdParams sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNotNull(sessionAdParams.getLastAdDomain());
        assertEquals("test_banner_ad_domain_1", sessionAdParams.getLastAdDomain());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNotNull(sessionAdParams.getLastAdDomain());
        assertEquals("test_interstitial_ad_domain_1", sessionAdParams.getLastAdDomain());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getLastAdDomain());

        bannerAd = BannerBridge.createBannerAd(context);
        bannerAd.adRequest = new TestAdRequest.Builder(AdsType.Banner).build();
        interstitialAd = new InterstitialAd(context);
        interstitialAd.adRequest = new TestAdRequest.Builder(AdsType.Interstitial).build();
        bannerAd.processCallback.processShown();
        interstitialAd.processCallback.processShown();

        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Banner);
        assertNull(sessionAdParams.getLastAdDomain());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Interstitial);
        assertNull(sessionAdParams.getLastAdDomain());
        sessionAdParams = sessionManager.getSessionAdParams(AdsType.Rewarded);
        assertNull(sessionAdParams.getLastAdDomain());
    }

}
package io.bidmachine;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import io.bidmachine.banner.BannerBridge;
import io.bidmachine.interstitial.InterstitialAd;
import io.bidmachine.nativead.NativeAd;
import io.bidmachine.rewarded.RewardedAd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class SessionTrackerTest {

    private SessionTrackerImpl tracker;

    @Before
    public void setup() {
        tracker = new SessionTrackerImpl();
    }

    @Test
    public void allTypeCountTracking() {
        BidMachineAd[] testRequests = new BidMachineAd[]{
                BannerBridge.createBannerAd(RuntimeEnvironment.application),
                new NativeAd(RuntimeEnvironment.application),
                new InterstitialAd(RuntimeEnvironment.application),
                new RewardedAd(RuntimeEnvironment.application)};

        for (BidMachineAd request : testRequests) {
            for (TrackEventType type : TrackEventType.values()) {
                tracker.trackEventFinish(request.trackingObject, type, request.getAdsType(), null);
            }
        }

        for (AdsType adsType : AdsType.values()) {
            for (TrackEventType type : TrackEventType.values()) {
                assertEquals(1, tracker.getEventCount(adsType, type));
            }
        }

        for (TrackEventType type : TrackEventType.values()) {
            assertEquals(4, tracker.getTotalEventCount(type));
        }
    }

    @Test
    public void notifyEvent_sameObject_clearInstanceWhenIntervalsEmptyAfterFinish() {
        TrackingObject trackingObject = new TestTrackingObject("test_key");
        tracker.trackEventStart(trackingObject, TrackEventType.AuctionRequest, null);
        assertEquals(1, tracker.intervalHolders.size());
        assertEquals(1, tracker.intervalHolders.get(trackingObject.getTrackingKey()).size());
        tracker.trackEventFinish(trackingObject, TrackEventType.AuctionRequest, null, null);
        assertEquals(0, tracker.intervalHolders.size());
    }

    @Test
    public void notifyEvent_sameObject_notEmptyInstanceAfterFinish() {
        TrackingObject trackingObject = new TestTrackingObject("test_key");
        tracker.trackEventStart(trackingObject, TrackEventType.AuctionRequest, null);
        tracker.trackEventStart(trackingObject, TrackEventType.Load, null);
        assertEquals(1, tracker.intervalHolders.size());
        assertEquals(2, tracker.intervalHolders.get(trackingObject.getTrackingKey()).size());
        tracker.trackEventFinish(trackingObject, TrackEventType.Load, null, null);
        assertEquals(1, tracker.intervalHolders.size());
    }

    @Test
    public void notifyEvent_differentObject() {
        TrackingObject trackingObject1 = new TestTrackingObject("test_key_1");
        TrackingObject trackingObject2 = new TestTrackingObject("test_key_2");

        tracker.trackEventStart(trackingObject1, TrackEventType.AuctionRequest, null);
        tracker.trackEventStart(trackingObject2, TrackEventType.AuctionRequest, null);

        assertEquals(2, tracker.intervalHolders.size());
        assertEquals(1, tracker.intervalHolders.get(trackingObject1.getTrackingKey()).size());
        assertEquals(1, tracker.intervalHolders.get(trackingObject2.getTrackingKey()).size());

        tracker.trackEventFinish(trackingObject1, TrackEventType.AuctionRequest, null, null);

        assertEquals(1, tracker.intervalHolders.size());
        assertNull(tracker.intervalHolders.get(trackingObject1.getTrackingKey()));
        assertEquals(1, tracker.intervalHolders.get(trackingObject2.getTrackingKey()).size());
    }

    @Test
    public void replaceMacros_urlIsNullOrEmpty_returnNull() {
        String url = SessionTracker.replaceMacros(null, null, 100, 500);
        assertNull(url);
        url = SessionTracker.replaceMacros("", null, 100, 500);
        assertNull(url);
    }

    @Test
    public void replaceMacros_urlWithoutMacros_returnInputUrl() {
        String url = "http://test.com";
        assertEquals(url, SessionTracker.replaceMacros(url, new TrackEventInfo(), 100, 500));
    }

    @Test
    public void replaceMacros_urlContainsMacros_returnUrlWithoutMacros() {
        String urlWithMacros = "http://test.com?" +
                "BM_EVENT_CODE=${BM_EVENT_CODE}&" +
                "BM_EVENT_CODE=%24%7BBM_EVENT_CODE%7D&" +
                "BM_ACTION_CODE=${BM_ACTION_CODE}&" +
                "BM_ACTION_CODE=%24%7BBM_ACTION_CODE%7D&" +
                "BM_ERROR_REASON=${BM_ERROR_REASON}&" +
                "BM_ERROR_REASON=%24%7BBM_ERROR_REASON%7D&" +
                "BM_ACTION_START=${BM_ACTION_START}&" +
                "BM_ACTION_START=%24%7BBM_ACTION_START%7D&" +
                "BM_ACTION_FINISH=${BM_ACTION_FINISH}&" +
                "BM_ACTION_FINISH=%24%7BBM_ACTION_FINISH%7D";
        String urlWithoutMacros = "http://test.com?" +
                "BM_EVENT_CODE=100&" +
                "BM_EVENT_CODE=100&" +
                "BM_ACTION_CODE=100&" +
                "BM_ACTION_CODE=100&" +
                "BM_ERROR_REASON=500&" +
                "BM_ERROR_REASON=500&" +
                "BM_ACTION_START=${BM_ACTION_START}&" +
                "BM_ACTION_START=%24%7BBM_ACTION_START%7D&" +
                "BM_ACTION_FINISH=${BM_ACTION_FINISH}&" +
                "BM_ACTION_FINISH=%24%7BBM_ACTION_FINISH%7D";
        assertEquals(
                urlWithoutMacros,
                SessionTracker.replaceMacros(
                        urlWithMacros,
                        null,
                        100,
                        500));
    }

    @Test
    public void replaceMacros_urlContainsMacrosAndTrackingInfo_returnUrlWithoutMacros() {
        TrackEventInfo trackEventInfo = new TrackEventInfo();
        trackEventInfo.finishTimeMs = 2000;
        String urlWithMacros = "http://test.com?" +
                "BM_EVENT_CODE=${BM_EVENT_CODE}&" +
                "BM_EVENT_CODE=%24%7BBM_EVENT_CODE%7D&" +
                "BM_ACTION_CODE=${BM_ACTION_CODE}&" +
                "BM_ACTION_CODE=%24%7BBM_ACTION_CODE%7D&" +
                "BM_ERROR_REASON=${BM_ERROR_REASON}&" +
                "BM_ERROR_REASON=%24%7BBM_ERROR_REASON%7D&" +
                "BM_ACTION_START=${BM_ACTION_START}&" +
                "BM_ACTION_START=%24%7BBM_ACTION_START%7D&" +
                "BM_ACTION_FINISH=${BM_ACTION_FINISH}&" +
                "BM_ACTION_FINISH=%24%7BBM_ACTION_FINISH%7D";
        String urlWithoutMacros = "http://test.com?" +
                "BM_EVENT_CODE=100&" +
                "BM_EVENT_CODE=100&" +
                "BM_ACTION_CODE=100&" +
                "BM_ACTION_CODE=100&" +
                "BM_ERROR_REASON=500&" +
                "BM_ERROR_REASON=500&" +
                "BM_ACTION_START=" + trackEventInfo.startTimeMs + "&" +
                "BM_ACTION_START=" + trackEventInfo.startTimeMs + "&" +
                "BM_ACTION_FINISH=" + trackEventInfo.finishTimeMs + "&" +
                "BM_ACTION_FINISH=" + trackEventInfo.finishTimeMs;
        assertEquals(
                urlWithoutMacros,
                SessionTracker.replaceMacros(
                        urlWithMacros,
                        trackEventInfo,
                        100,
                        500));
    }

}

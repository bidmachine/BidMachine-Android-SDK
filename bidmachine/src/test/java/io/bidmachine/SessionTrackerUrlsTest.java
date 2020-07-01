package io.bidmachine;

import io.bidmachine.protobuf.EventTypeExtended;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import io.bidmachine.utils.BMError;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class SessionTrackerUrlsTest {

    private MockWebServer mockWebServer;
    private List<String> trackedUrlList;
    private CountDownLatch countDownLatch;
    private TrackingObject trackingObject;
    private TrackEventInfo trackEventInfo;
    private MockResponse mockResponse;

    @Before
    public void setUp() throws Exception {
        trackedUrlList = new ArrayList<>();
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                trackedUrlList.add(request.getPath());
                countDownLatch.countDown();
                return mockResponse;
            }
        });
        mockWebServer.start();
        final String host = mockWebServer.url("").toString();

        Map<TrackEventType, List<String>> trackUrlMap = new EnumMap<>(TrackEventType.class);
        trackUrlMap.put(TrackEventType.Load, new ArrayList<String>() {{
            add(createUrlWithMacros(host, "Load", "test_id_1"));
            add(createUrlWithMacros(host, "Load", "test_id_2"));
            add(createUrlWithMacros(host, "Load", "test_id_3"));
        }});
        trackUrlMap.put(TrackEventType.Error, new ArrayList<String>() {{
            add(createUrlWithMacros(host, "Error", "test_id_4"));
            add(createUrlWithMacros(host, "Error", "test_id_5"));
        }});
        trackUrlMap.put(TrackEventType.TrackingError, new ArrayList<String>() {{
            add(createUrlWithMacros(host, "TrackingError", "test_id_6"));
        }});

        trackingObject = mock(TrackingObject.class);
        doReturn(trackUrlMap.get(TrackEventType.Load))
                .when(trackingObject)
                .getTrackingUrls(TrackEventType.Load);
        doReturn(trackUrlMap.get(TrackEventType.Error))
                .when(trackingObject)
                .getTrackingUrls(TrackEventType.Error);
        doReturn(trackUrlMap.get(TrackEventType.TrackingError))
                .when(trackingObject)
                .getTrackingUrls(TrackEventType.TrackingError);
        trackEventInfo = new TrackEventInfo();
        trackEventInfo.finishTimeMs = 2000;
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void notifyTrack_loadWithoutError_sendOnlyLoad() throws Exception {
        mockResponse = new MockResponse().setResponseCode(200);
        countDownLatch = new CountDownLatch(3);
        SessionTracker.notifyTrack(trackingObject, TrackEventType.Load, trackEventInfo, null);
        countDownLatch.await();

        assertEquals(3, trackedUrlList.size());
        assertTrue(trackedUrlList.contains(createUrlWithoutMacros(
                "/Load",
                "test_id_1",
                EventTypeExtended.EVENT_TYPE_EXTENDED_AD_LOADED_VALUE,
                -1,
                trackEventInfo)));
        assertTrue(trackedUrlList.contains(createUrlWithoutMacros(
                "/Load",
                "test_id_2",
                EventTypeExtended.EVENT_TYPE_EXTENDED_AD_LOADED_VALUE,
                -1,
                trackEventInfo)));
        assertTrue(trackedUrlList.contains(createUrlWithoutMacros(
                "/Load",
                "test_id_3",
                EventTypeExtended.EVENT_TYPE_EXTENDED_AD_LOADED_VALUE,
                -1,
                trackEventInfo)));
    }

    @Test
    public void notifyTrack_loadWithoutError_sendLoadWithTrackingError() throws Exception {
        mockResponse = new MockResponse().setResponseCode(404);
        countDownLatch = new CountDownLatch(6);
        SessionTracker.notifyTrack(trackingObject, TrackEventType.Load, trackEventInfo, null);
        countDownLatch.await();

        assertEquals(6, trackedUrlList.size());
        assertTrue(trackedUrlList.contains(createUrlWithoutMacros(
                "/Load",
                "test_id_1",
                EventTypeExtended.EVENT_TYPE_EXTENDED_AD_LOADED_VALUE,
                -1,
                trackEventInfo)));
        assertTrue(trackedUrlList.contains(createUrlWithoutMacros(
                "/Load",
                "test_id_2",
                EventTypeExtended.EVENT_TYPE_EXTENDED_AD_LOADED_VALUE,
                -1,
                trackEventInfo)));
        assertTrue(trackedUrlList.contains(createUrlWithoutMacros(
                "/Load",
                "test_id_3",
                EventTypeExtended.EVENT_TYPE_EXTENDED_AD_LOADED_VALUE,
                -1,
                trackEventInfo)));
        assertEquals(3, countOf(
                trackedUrlList,
                createUrlWithoutMacros(
                        "/TrackingError",
                        "test_id_6",
                        EventTypeExtended.EVENT_TYPE_EXTENDED_AD_LOADED_VALUE,
                        110,
                        trackEventInfo)));
    }

    @Test
    public void notifyTrack_loadWithError_sendError() throws Exception {
        mockResponse = new MockResponse().setResponseCode(200);
        countDownLatch = new CountDownLatch(2);
        SessionTracker.notifyTrack(trackingObject, TrackEventType.Load, trackEventInfo, BMError.TimeoutError);
        countDownLatch.await();

        assertEquals(2, trackedUrlList.size());
        assertTrue(trackedUrlList.contains(createUrlWithoutMacros(
                "/Error",
                "test_id_4",
                EventTypeExtended.EVENT_TYPE_EXTENDED_AD_LOADED_VALUE,
                102,
                trackEventInfo)));
        assertTrue(trackedUrlList.contains(createUrlWithoutMacros(
                "/Error",
                "test_id_5",
                EventTypeExtended.EVENT_TYPE_EXTENDED_AD_LOADED_VALUE,
                102,
                trackEventInfo)));
    }

    @Test
    public void notifyTrack_loadWithError_sendErrorAndTrackingError() throws Exception {
        mockResponse = new MockResponse().setResponseCode(404);
        countDownLatch = new CountDownLatch(4);
        SessionTracker.notifyTrack(trackingObject, TrackEventType.Load, trackEventInfo, BMError.TimeoutError);
        countDownLatch.await();

        assertEquals(4, trackedUrlList.size());
        assertTrue(trackedUrlList.contains(createUrlWithoutMacros(
                "/Error",
                "test_id_4",
                EventTypeExtended.EVENT_TYPE_EXTENDED_AD_LOADED_VALUE,
                102,
                trackEventInfo)));
        assertTrue(trackedUrlList.contains(createUrlWithoutMacros(
                "/Error",
                "test_id_5",
                EventTypeExtended.EVENT_TYPE_EXTENDED_AD_LOADED_VALUE,
                102,
                trackEventInfo)));
        assertEquals(2, countOf(
                trackedUrlList,
                createUrlWithoutMacros(
                        "/TrackingError",
                        "test_id_6",
                        1000,
                        110,
                        trackEventInfo)));
    }

    private String createUrlWithMacros(String host, String path, String id) {
        return host + path + "?" +
                "id=" + id + "&" +
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
    }

    private String createUrlWithoutMacros(String path,
                                          String id,
                                          int processCode,
                                          int errorCode,
                                          TrackEventInfo trackEventInfo) {
        return path + "?" +
                "id=" + id + "&" +
                "BM_EVENT_CODE=" + processCode + "&" +
                "BM_EVENT_CODE=" + processCode + "&" +
                "BM_ACTION_CODE=" + processCode + "&" +
                "BM_ACTION_CODE=" + processCode + "&" +
                "BM_ERROR_REASON=" + errorCode + "&" +
                "BM_ERROR_REASON=" + errorCode + "&" +
                "BM_ACTION_START=" + trackEventInfo.startTimeMs + "&" +
                "BM_ACTION_START=" + trackEventInfo.startTimeMs + "&" +
                "BM_ACTION_FINISH=" + trackEventInfo.finishTimeMs + "&" +
                "BM_ACTION_FINISH=" + trackEventInfo.finishTimeMs;
    }

    private int countOf(List<String> stringList, String searchString) {
        int count = 0;
        for (String string : stringList) {
            if (string.equals(searchString)) {
                count++;
            }
        }
        return count;
    }

}
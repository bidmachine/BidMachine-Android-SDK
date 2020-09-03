package io.bidmachine;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.explorestack.protobuf.adcom.Ad;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.bidmachine.protobuf.ActionType;
import io.bidmachine.protobuf.ErrorReason;
import io.bidmachine.protobuf.EventTypeExtended;
import io.bidmachine.protobuf.InitResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;

import static io.bidmachine.TestUtils.changeInitUrl;
import static io.bidmachine.TestUtils.resetBidMachineInstance;
import static io.bidmachine.TestUtils.restoreInitUrl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class InitRequestTest {

    private static final String TRACKING_URL_FORMAT = "tracking" +
            "?event_code=${BM_EVENT_CODE}" +
            "&event_code2=%24%7BBM_EVENT_CODE%7D" +
            "&start=${BM_ACTION_START}" +
            "&start2=%24%7BBM_ACTION_START%7D" +
            "&finish=${BM_ACTION_FINISH}" +
            "&finish2=%24%7BBM_ACTION_FINISH%7D";

    private static final String ERROR_TRACKING_URL_FORMAT = TRACKING_URL_FORMAT +
            "&action_code=${BM_ACTION_CODE}" +
            "&action_code2=%24%7BBM_ACTION_CODE%7D" +
            "&error_reason=${BM_ERROR_REASON}" +
            "&error_reason2=%24%7BBM_ERROR_REASON%7D";

    private MockWebServer mockWebServer;

    @Before
    public void setup() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        resetBidMachineInstance();
        changeInitUrl(mockWebServer.url("").toString());
    }

    @Test
    public void initRequest_Success() throws Exception {
        int eventCode = EventTypeExtended.EVENT_TYPE_EXTENDED_SESSION_INITIALIZED_VALUE;

        Ad.Event loadEvent = Ad.Event.newBuilder()
                .setTypeValue(EventTypeExtended.EVENT_TYPE_EXTENDED_SESSION_INITIALIZED_VALUE)
                .setUrl(mockWebServer.url(TRACKING_URL_FORMAT).toString())
                .build();

        InitResponse initResponse = InitResponse.newBuilder()
                .setEndpoint(mockWebServer.url("").toString())
                .addEvent(loadEvent)
                .build();

        Buffer buffer = new Buffer();
        buffer.write(initResponse.toByteArray());

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody(buffer));

        BidMachineImpl.get().initialize(RuntimeEnvironment.application, "1", null);

        RecordedRequest recordedRequest =
                mockWebServer.takeRequest(3, TimeUnit.SECONDS);
        RecordedRequest eventRequest =
                mockWebServer.takeRequest(3, TimeUnit.SECONDS);

        String path = eventRequest.getPath();

        assertTrue(eventRequest.getPath().contains("event_code=" + eventCode)
                && eventRequest.getPath().contains("event_code2=" + eventCode));

        assertEquals(eventCode, getParamLongValue(path, "event_code"));
        assertEquals(eventCode, getParamLongValue(path, "event_code2"));
        assertNotEquals(0, getParamLongValue(path, "start"));
        assertNotEquals(0, getParamLongValue(path, "start2"));
        assertNotEquals(0, getParamLongValue(path, "finish"));
        assertNotEquals(0, getParamLongValue(path, "finish2"));
    }

    @Test
    public void initRequest_Fail() throws Exception {
        Ad.Event loadEvent = Ad.Event.newBuilder()
                .setTypeValue(EventTypeExtended.EVENT_TYPE_EXTENDED_ERROR_VALUE)
                .setUrl(mockWebServer.url(ERROR_TRACKING_URL_FORMAT).toString())
                .build();

        InitResponse initResponse = InitResponse.newBuilder()
                .setEndpoint(mockWebServer.url("").toString())
                .addEvent(loadEvent)
                .build();

        Buffer buffer = new Buffer();
        buffer.write(initResponse.toByteArray());

        SharedPreferences preferences = RuntimeEnvironment.application.getSharedPreferences("BidMachinePref", Context.MODE_PRIVATE);
        preferences.edit().putString("initData", Base64.encodeToString(initResponse.toByteArray(), Base64.DEFAULT)).apply();

        mockWebServer.enqueue(new MockResponse().setResponseCode(400)
                .setBody(buffer));

        BidMachineImpl.get().initialize(RuntimeEnvironment.application, "1", null);

        RecordedRequest recordedRequest =
                mockWebServer.takeRequest(3, TimeUnit.SECONDS);
        RecordedRequest eventRequest =
                mockWebServer.takeRequest(3, TimeUnit.SECONDS);

        String path = eventRequest.getPath();

        assertEquals(ActionType.ACTION_TYPE_SESSION_INITIALIZING_VALUE, getParamLongValue(path, "action_code"));
        assertEquals(ActionType.ACTION_TYPE_SESSION_INITIALIZING_VALUE, getParamLongValue(path, "action_code2"));
        assertEquals(ErrorReason.ERROR_REASON_HTTP_BAD_REQUEST_VALUE, getParamLongValue(path, "error_reason"));
        assertEquals(ErrorReason.ERROR_REASON_HTTP_BAD_REQUEST_VALUE, getParamLongValue(path, "error_reason2"));
        assertNotEquals(0, getParamLongValue(path, "start"));
        assertNotEquals(0, getParamLongValue(path, "start2"));
        assertNotEquals(0, getParamLongValue(path, "finish"));
        assertNotEquals(0, getParamLongValue(path, "finish2"));
    }

    private long getParamLongValue(String path, String param) throws Exception {
        Matcher matcher = Pattern.compile("(" + param + "=)(\\d*)").matcher(path);
        if (matcher.find()) {
            return Long.valueOf(matcher.group(2));
        }
        throw new Exception("Value not found");
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
        restoreInitUrl();
    }

}

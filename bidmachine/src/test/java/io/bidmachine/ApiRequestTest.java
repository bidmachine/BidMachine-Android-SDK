package io.bidmachine;

import android.support.annotation.Nullable;
import io.bidmachine.core.NetworkRequest;
import io.bidmachine.utils.BMError;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 16)
public class ApiRequestTest {

    private MockWebServer mockServer;

    @Before
    public void setup() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @After
    public void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    public void request_success() throws InterruptedException {
        mockServer.enqueue(new MockResponse().setBody("Success"));
        performTest("Request", "Success");
    }

    @Test
    public void request_fail_err204() throws InterruptedException {
        mockServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_NO_CONTENT));
        performTest("Request", BMError.NoContent);
    }

    @Test
    public void request_fail_err400() throws InterruptedException {
        mockServer.enqueue(new MockResponse()
                .setHeader("ad-exchange-error-message", "Test error message")
                .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST));
        performTest("Request", BMError.requestError("Test error message"));
    }

    @Test
    public void request_fail_with_message() throws InterruptedException {
        mockServer.enqueue(new MockResponse()
                .setHeader("ad-exchange-error-message", "Test error message")
                .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND));
        performTest("Request", BMError.requestError("Test error message"));
    }

    @Test
    public void request_fail_timeout() throws InterruptedException {
        mockServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
        performTest("Request", BMError.TimeoutError);
    }

    @Test
    public void request_fail_timeout_throttle() throws InterruptedException {
        ApiRequest.REQUEST_TIMEOUT = 2000;
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("Success")
                .throttleBody(1, 1, TimeUnit.SECONDS));
        performTest("Request", BMError.TimeoutError);
    }

    @Test
    public void setLoadingTimeOut_negativeNumber_defaultTimeOut() {
        int timeOut = -10;
        ApiRequest<String, String> apiRequest = new ApiRequest.Builder<String, String>()
                .setLoadingTimeOut(timeOut)
                .build();
        assertEquals(ApiRequest.REQUEST_TIMEOUT, apiRequest.timeOut);
    }

    @Test
    public void setLoadingTimeOut_zeroNumber_defaultTimeOut() {
        int timeOut = 0;
        ApiRequest<String, String> apiRequest = new ApiRequest.Builder<String, String>()
                .setLoadingTimeOut(timeOut)
                .build();
        assertEquals(ApiRequest.REQUEST_TIMEOUT, apiRequest.timeOut);
    }

    @Test
    public void setLoadingTimeOut_positiveNumber_transferredTimeOut() {
        int timeOut = 10;
        ApiRequest<String, String> apiRequest = new ApiRequest.Builder<String, String>()
                .setLoadingTimeOut(timeOut)
                .build();
        assertEquals(timeOut, apiRequest.timeOut);
    }

    private class ObjectContainer<ObjectType> {
        ObjectType referenceObject;
    }

    private void performTest(String requestData, final Object responseResult) throws InterruptedException {
        final CountDownLatch lock = new CountDownLatch(1);
        final AtomicBoolean requestResult = new AtomicBoolean();

        final ObjectContainer<Object> responseContainer = new ObjectContainer<>();

        ApiRequest<String, String> request = new ApiRequest.Builder<String, String>()
                .setRequestData(requestData)
                .setDataBinder(new ApiRequest.ApiDataBinder<String, String>() {
                    @Override
                    protected void prepareHeaders(NetworkRequest request, URLConnection connection) {
                    }

                    @Nullable
                    @Override
                    protected byte[] obtainData(NetworkRequest request, URLConnection connection,
                                                @Nullable String requestData) {
                        return requestData.getBytes();
                    }

                    @Override
                    protected String createSuccessResult(NetworkRequest request,
                                                         URLConnection connection,
                                                         byte[] resultData) {
                        return new String(resultData);
                    }
                })
                .setCallback(new NetworkRequest.Callback<String, BMError>() {
                    @Override
                    public void onSuccess(@Nullable String result) {
                        responseContainer.referenceObject = result;
                        requestResult.set(true);
                        lock.countDown();
                    }

                    @Override
                    public void onFail(@Nullable BMError result) {
                        responseContainer.referenceObject = result;
                        requestResult.set(false);
                        lock.countDown();
                    }
                }).build();

        request.requiredUrl = mockServer.url("/test").toString();
        request.request();
        lock.await();

        assertEquals(responseResult, responseContainer.referenceObject);
    }

}

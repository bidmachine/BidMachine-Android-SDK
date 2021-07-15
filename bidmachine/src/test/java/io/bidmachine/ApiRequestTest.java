package io.bidmachine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

import io.bidmachine.core.NetworkRequest;
import io.bidmachine.utils.BMError;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 16)
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
    public void request_success() throws Exception {
        mockServer.enqueue(new MockResponse().setBody("Success"));
        performSuccessTest("Request", "Success");
    }

    @Test
    public void request_fail_err204() throws Exception {
        mockServer.enqueue(new MockResponse().setResponseCode(HttpURLConnection.HTTP_NO_CONTENT));
        performFailTest("Request", BMError.noFill());
    }

    @Test
    public void request_fail_err400() throws Exception {
        mockServer.enqueue(new MockResponse()
                                   .setHeader("ad-exchange-error-message", "Test error message")
                                   .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST));
        performFailTest("Request", BMError.Request);
    }

    @Test
    public void request_fail_with_message() throws Exception {
        mockServer.enqueue(new MockResponse()
                                   .setHeader("ad-exchange-error-message", "Test error message")
                                   .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND));
        performFailTest("Request", BMError.Request);
    }

    @Test
    public void request_fail_timeout() throws Exception {
        mockServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
        performFailTest("Request", BMError.TimeoutError);
    }

    @Test
    public void request_fail_timeout_throttle() throws Exception {
        ApiRequest.REQUEST_TIMEOUT = 2000;
        mockServer.enqueue(new MockResponse()
                                   .setResponseCode(200)
                                   .setBody("Success")
                                   .throttleBody(1, 1, TimeUnit.SECONDS));
        performFailTest("Request", BMError.TimeoutError);
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


    static class Container<T> {

        T object;

    }


    @SuppressWarnings("SameParameterValue")
    private void performSuccessTest(@NonNull String requestData,
                                    @NonNull String expectedResult) throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final Container<String> container = new Container<>();

        ApiRequest<String, String> request = new ApiRequest.Builder<String, String>()
                .setRequestData(requestData)
                .setDataBinder(new ApiRequest.ApiDataBinder<String, String>() {
                    @Override
                    protected void prepareHeaders(NetworkRequest<String, String, BMError> request,
                                                  URLConnection connection) {

                    }

                    @Nullable
                    @Override
                    protected byte[] obtainData(NetworkRequest<String, String, BMError> request,
                                                URLConnection connection,
                                                @Nullable String requestData) {
                        return requestData.getBytes();
                    }

                    @Override
                    protected String createSuccessResult(NetworkRequest<String, String, BMError> request,
                                                         URLConnection connection,
                                                         byte[] resultData) {
                        return new String(resultData);
                    }
                })
                .setCallback(new NetworkRequest.Callback<String, BMError>() {
                    @Override
                    public void onSuccess(@Nullable String result) {
                        container.object = result;
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onFail(@Nullable BMError result) {
                        fail();
                    }
                }).build();

        request.requiredUrl = mockServer.url("/test").toString();
        request.request();
        countDownLatch.await();

        assertEquals(expectedResult, container.object);
    }

    @SuppressWarnings("SameParameterValue")
    private void performFailTest(@NonNull String requestData,
                                 @NonNull BMError expectedResult) throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final Container<BMError> container = new Container<>();

        ApiRequest<String, String> request = new ApiRequest.Builder<String, String>()
                .setRequestData(requestData)
                .setDataBinder(new ApiRequest.ApiDataBinder<String, String>() {
                    @Override
                    protected void prepareHeaders(NetworkRequest<String, String, BMError> request,
                                                  URLConnection connection) {

                    }

                    @Nullable
                    @Override
                    protected byte[] obtainData(NetworkRequest<String, String, BMError> request,
                                                URLConnection connection,
                                                @Nullable String requestData) {
                        return requestData.getBytes();
                    }

                    @Override
                    protected String createSuccessResult(NetworkRequest<String, String, BMError> request,
                                                         URLConnection connection,
                                                         byte[] resultData) {
                        return new String(resultData);
                    }
                })
                .setCallback(new NetworkRequest.Callback<String, BMError>() {
                    @Override
                    public void onSuccess(@Nullable String result) {
                        fail();
                    }

                    @Override
                    public void onFail(@Nullable BMError result) {
                        container.object = result;
                        countDownLatch.countDown();
                    }
                }).build();

        request.requiredUrl = mockServer.url("/test").toString();
        request.request();
        countDownLatch.await();

        assertEquals(expectedResult, container.object);
    }

}

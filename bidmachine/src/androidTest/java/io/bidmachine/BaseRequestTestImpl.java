package io.bidmachine;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.WifiManager;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.explorestack.protobuf.Any;
import com.explorestack.protobuf.MessageOrBuilder;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Openrtb;
import com.explorestack.protobuf.openrtb.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.bidmachine.protobuf.AdExtension;
import io.bidmachine.protobuf.ErrorReason;
import io.bidmachine.protobuf.InitRequest;
import io.bidmachine.protobuf.InitResponse;
import io.bidmachine.test_utils.TestHelper;
import io.bidmachine.test_utils.ViewAction;
import io.bidmachine.utils.BMError;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import okio.Buffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class BaseRequestTestImpl<AdType extends BidMachineAd, AdRequestType extends AdRequest>
        implements AdRequestTests, AdListener<AdType>, AdFullScreenListener<AdType>, AdRewardedListener<AdType> {

    protected MockWebServer mockWebServer;

    protected ResultState loadedState = new ResultState();
    protected ResultState failedState = new ResultState();
    protected ResultState shownState = new ResultState();
    protected ResultState shownFailState = new ResultState();
    protected ResultState impressionState = new ResultState();
    protected ResultState clickedState = new ResultState();
    protected ResultState closedState = new ResultState();
    protected ResultState expiredState = new ResultState();
    protected ResultState rewardedState = new ResultState();

    protected CountDownLatch countDownLatch;

    @Rule
    public ActivityTestRule<RequestTestActivity> activityTestRule = new ActivityTestRule<>(RequestTestActivity.class);

    @Before
    public void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(8080);

        setCurrentAuctionUrl(mockWebServer.url("mock_request").toString());

        ApiRequest.REQUEST_TIMEOUT = 1000;
        BidMachineImpl.get().currentInitRequest = new ApiRequest.Builder<InitRequest, InitResponse>().build();
        BidMachine.initialize(activityTestRule.getActivity(), "1");
    }

    private void setCurrentAuctionUrl(String url) {
        InitResponse initResponse = InitResponse.newBuilder()
                                                .setEndpoint(url)
                                                .build();

        SharedPreferences preferences = activityTestRule.getActivity()
                                                        .getSharedPreferences("BidMachinePref", Context.MODE_PRIVATE);
        preferences.edit()
                   .putString("initData", Base64.encodeToString(initResponse.toByteArray(), Base64.DEFAULT))
                   .apply();

        BidMachine.setTestMode(true);
        BidMachine.setLoggingEnabled(true);
        BidMachineImpl.get().currentAuctionUrl = url;
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    protected OrtbAdCreator adCreator = new DefaultOrtbAdCreator();
    protected OrtbSeatbidCreator seatbidCreator = new DefaultSeatbidCreator();
    protected OrtbBidCreator bidCreator = new DefaultBidCreator();

    protected abstract AdType createAd();

    protected abstract AdRequestType createAdRequest();

    protected abstract PlacementDisplayBuilder createPlacementDisplayBuilder();

    /*
    Tests
     */

    abstract class ResponseBuildCallback {
        void onCreatePlacementDisplayBuilder(PlacementDisplayBuilder builder) {
        }

        void onCreateAd(Ad.Builder builder) {
        }

        void onCreateSeatbid(Response.Seatbid.Builder builder) {
        }

        void onCreateBid(Response.Seatbid.Bid.Builder builder) {
        }
    }

    protected Response.Builder buildResponse() {
        return buildResponse(null);
    }

    protected Response.Builder buildResponse(ResponseBuildCallback callback) {
        final PlacementDisplayBuilder placementDisplayBuilder = createPlacementDisplayBuilder();
        if (callback != null) callback.onCreatePlacementDisplayBuilder(placementDisplayBuilder);

        final MessageOrBuilder placementDisplay = placementDisplayBuilder.createDisplayBuilder();

        final Ad.Builder adBuilder = adCreator.createAd();
        if (callback != null) callback.onCreateAd(adBuilder);
        placementDisplayBuilder.bind(adBuilder, placementDisplay);

        final Response.Seatbid.Bid.Builder bid = bidCreator.createBid();
        bid.setMedia(Any.pack(adBuilder.build()));
        if (callback != null) callback.onCreateBid(bid);

        final Response.Seatbid.Builder seatbid = seatbidCreator.createSeatbid();
        seatbid.addBid(bid);
        if (callback != null) callback.onCreateSeatbid(seatbid);

        Response.Builder responseBuilder = Response.newBuilder();
        responseBuilder.addSeatbid(seatbid);
        return responseBuilder;
    }

    protected void testShown(final AdType ad, boolean success) {
        if (ad instanceof FullScreenAd) {
            countDownLatch = new CountDownLatch(1);
            ((FullScreenAd) ad).show();
        } else if (ad instanceof ViewAd) {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
                @Override
                public void run() {
                    ((ViewAd) ad).show(activityTestRule.getActivity().parentFrame);
                }
            });
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        if (success) {
            try {
                countDownLatch.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            assertTrue(shownState.getState());
        } else {
            assertNull(shownState.getState());
            assertTrue(shownFailState.getState());
        }
    }

    protected void testLoad(@NonNull Response.Builder response, boolean success) {
        testLoad(response, null, success);
    }

    protected void testLoad(@NonNull Response.Builder response,
                            @Nullable final LoadRequestResponseCallback callback,
                            boolean success) {
        testLoad(createAd(), createAdRequest(), response, callback, success);
    }

    protected void testLoad(@NonNull AdType ad,
                            @NonNull AdRequestType adRequest,
                            @NonNull final Response.Builder response,
                            @Nullable final LoadRequestResponseCallback callback,
                            boolean success) {
        loadRequest(ad, adRequest, response, callback);
        if (!success) {
            assertNull(loadedState.getState());
            assertTrue(failedState.getState());
        } else {
            assertTrue(loadedState.getState());
            assertNull(failedState.getState());
        }
    }

    /*
    Tests
     */

    @Test
    @Override
    public void testLoadSuccess() {
        testLoad(buildResponse(), true);
    }

    @Test
    @Override
    public void testLoadFailBadRequest() {
        testLoad(buildResponse(), new LoadRequestResponseCallback() {
            @Override
            public void onBuild(MockResponse response) {
                response.setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
            }
        }, false);
        assertNotNull(failedState.error.getOriginError());
        assertSame(ErrorReason.ERROR_REASON_HTTP_BAD_REQUEST_VALUE, failedState.error.getOriginError().getCode());
    }

    @Test
    @Override
    public void testLoadFailAdmNull() {
        testLoad(buildResponse(new ResponseBuildCallback() {
            @Override
            void onCreatePlacementDisplayBuilder(PlacementDisplayBuilder builder) {
                super.onCreatePlacementDisplayBuilder(builder);
                builder.setAdm(null);
            }
        }), false);
        assertSame(ErrorReason.ERROR_REASON_BAD_CONTENT_VALUE, failedState.error.getCode());
    }

    @Test
    @Override
    public void testLoadFailNoFill() {
        testLoad(buildResponse(), new LoadRequestResponseCallback() {
            @Override
            public void onBuild(MockResponse response) {
                response.setResponseCode(HttpURLConnection.HTTP_NO_CONTENT);
            }
        }, false);
        assertNull(failedState.error.getOriginError());
        assertSame(ErrorReason.ERROR_REASON_NO_CONTENT_VALUE, failedState.error.getCode());
    }

    @Test
    @Override
    public void testLoadFailNoFillConnection() {
        setCurrentAuctionUrl("http://unknown_host_local.com");
        testLoad(buildResponse(), new LoadRequestResponseCallback() {
            @Override
            public void onBuild(MockResponse response) {
                response.throttleBody(1, 1, TimeUnit.MILLISECONDS);
            }
        }, false);
        assertNotNull(failedState.error.getOriginError());
        assertSame(ErrorReason.ERROR_REASON_NO_CONNECTION_VALUE, failedState.error.getOriginError().getCode());
    }

    @Test
    @Override
    public void testLoadFailNoFillTimeoutError() {
        testLoad(buildResponse(), new LoadRequestResponseCallback() {
            @Override
            public void onBuild(MockResponse response) {
                response.setSocketPolicy(SocketPolicy.NO_RESPONSE);
            }
        }, false);
        assertNotNull(failedState.error.getOriginError());
        assertSame(ErrorReason.ERROR_REASON_TIMEOUT_VALUE, failedState.error.getOriginError().getCode());
    }

    @Test
    @Override
    public void testShown() {
        AdType ad = createAd();
        AdRequestType adRequest = createAdRequest();

        testLoad(ad, adRequest, buildResponse(), null, true);
        testShown(ad, true);
    }

    @Test
    @Override
    public void testClicked() {
        AdType ad = createAd();
        AdRequestType adRequest = createAdRequest();

        testLoad(ad, adRequest, buildResponse(new ResponseBuildCallback() {
            @Override
            void onCreateAd(Ad.Builder adBuilder) {
                super.onCreateAd(adBuilder);
                AdExtension adExtension = AdExtension.newBuilder()
                                                     .setViewabilityTimeThreshold(1)
                                                     .build();
                adBuilder.addExtProto(Any.pack(adExtension));
            }
        }), null, true);

        testShown(ad, true);
        testImpression(3000, true);
        testClick(ad);
        assertTrue(clickedState.getState());
    }

    @Test
    @Override
    public void testImpressionTracked() {
        AdType ad = createAd();
        AdRequestType adRequest = createAdRequest();
        testLoad(ad, adRequest, buildResponse(new ResponseBuildCallback() {
            @Override
            void onCreateAd(Ad.Builder adBuilder) {
                super.onCreateAd(adBuilder);
                AdExtension adExtension = AdExtension.newBuilder()
                                                     .setViewabilityTimeThreshold(1)
                                                     .build();
                adBuilder.addExtProto(Any.pack(adExtension));
            }
        }), null, true);

        testShown(ad, true);
        testImpression(1100, true);
    }

    @Test
    @Override
    public void testImpressionNotTrackedByTimeout() {
        AdType ad = createAd();
        AdRequestType adRequest = createAdRequest();
        loadRequest(ad, adRequest, buildResponse(new ResponseBuildCallback() {
            @Override
            void onCreateAd(Ad.Builder adBuilder) {
                super.onCreateAd(adBuilder);
                AdExtension adExtension = AdExtension.newBuilder()
                                                     .setViewabilityTimeThreshold(3)
                                                     .build();
                adBuilder.addExtProto(Any.pack(adExtension));
            }
        }), null);

        testShown(ad, true);
        testImpression(500, false);
    }

    @Test
    @Override
    public void testImpressionNotTrackedByShown() {
        AdType ad = createAd();
        AdRequestType adRequest = createAdRequest();
        loadRequest(ad, adRequest, buildResponse(new ResponseBuildCallback() {
            @Override
            void onCreateAd(Ad.Builder adBuilder) {
                super.onCreateAd(adBuilder);
                AdExtension adExtension = AdExtension.newBuilder()
                                                     .setViewabilityTimeThreshold(1)
                                                     .build();
                adBuilder.addExtProto(Any.pack(adExtension));
            }
        }), null);

        testImpression(11000, false);
    }

    protected void testImpression(long delay, boolean success) {
        if (success) {
            new AwaitHelper() {
                @Override
                public boolean isReady() {
                    return impressionState.getState() != null;
                }
            }.start(10000);
            assertTrue(impressionState.getState());
            assertEquals(impressionState.getSettingTime(), System.currentTimeMillis(),
                         50);
        } else {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            assertNull(impressionState.getState());
        }
    }

    protected void testClick(AdType ad) {
        countDownLatch = new CountDownLatch(1);
        if (ad instanceof FullScreenAd) {
            ViewAction.click(activityTestRule.getActivity());
        } else if (ad instanceof ViewAd) {
            ViewAction.click(activityTestRule.getActivity().parentFrame.getChildAt(0));
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        try {
            countDownLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Override
    public void testExpired() {
        AdType ad = createAd();
        AdRequestType adRequest = createAdRequest();

        testLoad(ad, adRequest, buildResponse(new ResponseBuildCallback() {
            @Override
            void onCreateBid(Response.Seatbid.Bid.Builder builder) {
                super.onCreateBid(builder);
                builder.setExp(1);
            }
        }), null, true);
        testExpiration(ad, true, 1500);
    }

    private void testExpiration(AdType ad, boolean expired, long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        if (expired) {
            assertNotNull(expiredState.getState());
            assertTrue(expiredState.getState());
        } else {
            assertNull(expiredState.getState());
        }
    }

    @Test
    @Override
    public void testDestroy() {
        AdType ad = createAd();
        AdRequestType adRequest = createAdRequest();
        testLoad(ad, adRequest, buildResponse(), null, true);
        testShown(ad, true);
        testImpression(0, true);
        testDestroy(ad, true);
    }

    protected void testDestroy(final AdType ad, boolean success) {
        if (success) {
            ad.destroy();
            new AwaitHelper() {
                @Override
                public boolean isReady() {
                    return ad.isDestroyed();
                }
            }.start(1000);
            assertTrue(ad.isDestroyed());
        }
    }

    /*
    Listeners
     */

    @Override
    public void onAdLoaded(@NonNull AdType ad) {
        loadedState.setState(true);
        releaseLock();
    }

    @Override
    public void onAdLoadFailed(@NonNull AdType ad, @NonNull BMError error) {
        failedState.setState(true);
        failedState.setError(error);
        releaseLock();
    }

    @Override
    public void onAdShown(@NonNull AdType ad) {
        shownState.setState(true);
        releaseLock();
    }

    @Override
    public void onAdShowFailed(@NonNull AdType ad, @NonNull BMError error) {
        shownFailState.setState(true);
        shownFailState.setError(error);
        releaseLock();
    }

    @Override
    public void onAdImpression(@NonNull AdType ad) {
        impressionState.setState(true);
        releaseLock();
    }

    @Override
    public void onAdClicked(@NonNull AdType ad) {
        clickedState.setState(true);
        releaseLock();
    }

    @Override
    public void onAdClosed(@NonNull AdType ad, boolean finished) {
        closedState.setState(true);
        releaseLock();
    }

    @Override
    public void onAdExpired(@NonNull AdType ad) {
        expiredState.setState(true);
        releaseLock();
    }

    @Override
    public void onAdRewarded(@NonNull AdType ad) {
        rewardedState.setState(true);
        releaseLock();
    }

    private void releaseLock() {
        countDownLatch.countDown();
    }

    /*
    Helpers
     */

    protected void loadRequest(@NonNull Response.Builder response) {
        loadRequest(response, null);
    }

    protected void loadRequest(@NonNull Response.Builder response,
                               @Nullable final LoadRequestResponseCallback callback) {
        loadRequest(createAd(), createAdRequest(), response, callback);
    }

    protected void loadRequest(@NonNull AdType ad,
                               @NonNull AdRequestType adRequest,
                               @NonNull final Response.Builder response,
                               @Nullable final LoadRequestResponseCallback callback) {
        try {
            mockWebServer.enqueue(new MockResponse());
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    MockResponse mockResponse = new MockResponse();
                    switch (request.getPath()) {
                        case "/mock_request": {
                            Openrtb.Builder openrtb = Openrtb.newBuilder();
                            openrtb.setVer("3.0");
                            openrtb.setDomainspec("adcom");
                            openrtb.setDomainver("1.0");
                            openrtb.setResponse(response.build());

                            Buffer bufferRequest = new Buffer();
                            bufferRequest.write(openrtb.build().toByteArray());

                            mockResponse.setResponseCode(200).setBody(bufferRequest);
                            if (callback != null) callback.onBuild(mockResponse);
                            break;
                        }
                    }

                    return mockResponse;
                }
            });

            countDownLatch = new CountDownLatch(1);

            ad.setListener(this);
            ad.load(adRequest);
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    //This solution will work for emulator only
    protected void setNetworkState(boolean enabled) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) activityTestRule.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                countDownLatch.countDown();
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                countDownLatch.countDown();
            }
        });
        WifiManager wifi = (WifiManager) activityTestRule.getActivity().getSystemService(Context.WIFI_SERVICE);
        wifi.setWifiEnabled(enabled);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /*
    Help classes
     */

    public static class ResultState {

        private Boolean state;
        private long settingTime;

        private BMError error;

        ResultState() {
        }

        public Boolean getState() {
            return state;
        }

        void setState(boolean state) {
            this.state = state;
            settingTime = System.currentTimeMillis();
        }

        public void setError(BMError error) {
            this.error = error;
        }

        public BMError getError() {
            return error;
        }

        long getSettingTime() {
            return settingTime;
        }
    }

    /*
    Request response builders
     */

    interface LoadRequestResponseCallback {
        void onBuild(MockResponse response);
    }

    public interface PlacementDisplayBuilder<SelfType extends PlacementDisplayBuilder, DisplayType extends MessageOrBuilder> {
        DisplayType createDisplayBuilder();

        void bind(Ad.Builder adBuilder, DisplayType displayType);

        SelfType setAdm(@Nullable String adm);

        SelfType setSize(int width, int height);
    }

    public class MraidDisplayBuilder extends DisplayBuilder {
        public MraidDisplayBuilder() {
            setAdm(TestHelper.getTestMraidAdm());
        }
    }

    public class VastVideoDisplayBuilder extends VideoDisplayBuilder {
        public VastVideoDisplayBuilder() {
            setAdm(TestHelper.getAdFoxTestVastAdm());
        }
    }

    public class DisplayBuilder implements PlacementDisplayBuilder<DisplayBuilder, Ad.Display.Builder> {

        Ad.Display.Builder builder = Ad.Display.newBuilder();

        @Override
        public Ad.Display.Builder createDisplayBuilder() {
            return builder;
        }

        @Override
        public DisplayBuilder setAdm(@Nullable String adm) {
            if (adm == null) {
                builder.clearAdm();
            } else {
                builder.setAdm(adm);
            }
            return this;
        }

        @Override
        public void bind(Ad.Builder adBuilder, Ad.Display.Builder builder) {
            adBuilder.setDisplay(builder);
        }

        @Override
        public DisplayBuilder setSize(int width, int height) {
            builder.setW(width);
            builder.setH(height);
            return this;
        }
    }

    public class VideoDisplayBuilder implements PlacementDisplayBuilder<VideoDisplayBuilder, Ad.Video.Builder> {

        Ad.Video.Builder builder = Ad.Video.newBuilder();

        @Override
        public Ad.Video.Builder createDisplayBuilder() {
            return builder;
        }

        @Override
        public VideoDisplayBuilder setAdm(@Nullable String adm) {
            if (adm == null) {
                builder.clearAdm();
            } else {
                builder.setAdm(adm);
            }
            return this;
        }

        @Override
        public void bind(Ad.Builder adBuilder, Ad.Video.Builder builder) {
            adBuilder.setVideo(builder);
        }

        @Override
        public VideoDisplayBuilder setSize(int width, int height) {
            return this;
        }
    }

    interface OrtbAdCreator {
        Ad.Builder createAd();
    }

    class DefaultOrtbAdCreator implements OrtbAdCreator {
        @Override
        public Ad.Builder createAd() {
            return Ad.newBuilder()
                     .setId("test_id_1");
        }
    }

    interface OrtbSeatbidCreator {
        Response.Seatbid.Builder createSeatbid();
    }

    class DefaultSeatbidCreator implements OrtbSeatbidCreator {

        @Override
        public Response.Seatbid.Builder createSeatbid() {
            return Response.Seatbid
                    .newBuilder();
        }

    }

    interface OrtbBidCreator {
        Response.Seatbid.Bid.Builder createBid();
    }

    class DefaultBidCreator implements OrtbBidCreator {

        @Override
        public Response.Seatbid.Bid.Builder createBid() {
            return Response.Seatbid.Bid.newBuilder()
                                       .setId("test_bid_id_1")
                                       .setPrice(2.34D);
        }

    }

    protected abstract class AwaitHelper {

        private boolean isCanceled;

        public abstract boolean isReady();

        public AwaitHelper start(final long timeout) {
            final Thread awaitThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        Thread.sleep(timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isCanceled = true;
                }
            };
            while (!isReady() && !isCanceled) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return this;
        }

    }

}

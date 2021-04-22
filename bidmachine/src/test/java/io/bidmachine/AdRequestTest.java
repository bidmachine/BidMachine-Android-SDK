package io.bidmachine;

import androidx.annotation.NonNull;

import com.explorestack.protobuf.adcom.Context;
import com.explorestack.protobuf.adcom.Placement;
import com.explorestack.protobuf.openrtb.Openrtb;
import com.explorestack.protobuf.openrtb.Request;
import com.explorestack.protobuf.openrtb.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.verification.Times;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.bidmachine.protobuf.ErrorReason;
import io.bidmachine.protobuf.HeaderBiddingType;
import io.bidmachine.protobuf.RequestExtension;
import io.bidmachine.protobuf.ResponsePayload;
import io.bidmachine.utils.BMError;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AdRequestTest {

    private android.content.Context context;
    private TestAdRequest adRequest;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        adRequest = spy(new TestAdRequest.Builder(AdsType.Interstitial).build());
        BidMachine.setConsentConfig(true, null);
        BidMachine.setSubjectToGDPR(null);
        BidMachine.initialize(context, "1");
    }

    @Test
    public void build_userRestrictionParamsNotSet_useDefaultValues() throws Exception {
        Request request = (Request) adRequest.build(context, adRequest.getType());
        Context requestContext = request.getContext().unpack(Context.class);
        assertEquals("1", requestContext.getUser().getConsent());
        assertFalse(requestContext.getRegs().getGdpr());
    }

    @Test
    public void build_userRestrictionParamsIsSet_useValuesFromUserRestrictionParams() throws Exception {
        UserRestrictionParams userRestrictionParams = new UserRestrictionParams();
        userRestrictionParams.setConsentConfig(true,
                                               "private_consent_string");
        userRestrictionParams.setSubjectToGDPR(true);
        adRequest.userRestrictionParams = userRestrictionParams;
        Request request = (Request) adRequest.build(context, adRequest.getType());
        Context requestContext = request.getContext().unpack(Context.class);
        assertEquals("private_consent_string",
                     requestContext.getUser().getConsent());
        assertTrue(requestContext.getRegs().getGdpr());
    }

    @Test
    public void build_userSetParamsFromPublicApi_useValuesFromPublicApi() throws Exception {
        BidMachine.setConsentConfig(false, "public_consent_string");
        BidMachine.setSubjectToGDPR(true);
        Request request = (Request) adRequest.build(context, adRequest.getType());
        Context requestContext = request.getContext().unpack(Context.class);
        assertEquals("public_consent_string",
                     requestContext.getUser().getConsent());
        assertTrue(requestContext.getRegs().getGdpr());
    }

    @Test
    public void build_userRestrictionParamsIsSetAndUserSetParamsFromApi_useValuesFromUserRestrictionParams() throws Exception {
        BidMachine.setConsentConfig(false, "public_consent_string");
        BidMachine.setSubjectToGDPR(true);
        UserRestrictionParams userRestrictionParams = new UserRestrictionParams();
        userRestrictionParams.setConsentConfig(true,
                                               "private_consent_string");
        userRestrictionParams.setSubjectToGDPR(false);
        adRequest.userRestrictionParams = userRestrictionParams;
        Request request = (Request) adRequest.build(context, adRequest.getType());
        Context requestContext = request.getContext().unpack(Context.class);
        assertEquals("private_consent_string",
                     requestContext.getUser().getConsent());
        assertFalse(requestContext.getRegs().getGdpr());
    }

    @Test
    public void build_publisher() throws Exception {
        //Default publisher
        Request request = (Request) adRequest.build(context, adRequest.getType());
        Context requestContext = request.getContext().unpack(Context.class);
        Context.App.Publisher protoPublisher = requestContext.getApp().getPub();

        assertNotNull(protoPublisher);
        assertEquals(Context.App.Publisher.getDefaultInstance().getId(),
                     protoPublisher.getId());
        assertEquals(Context.App.Publisher.getDefaultInstance().getName(),
                     protoPublisher.getName());
        assertEquals(Context.App.Publisher.getDefaultInstance().getDomain(),
                     protoPublisher.getDomain());
        assertEquals(Context.App.Publisher.getDefaultInstance().getCatList(),
                     protoPublisher.getCatList());

        //Custom publisher
        List<String> categoryList = new ArrayList<>();
        categoryList.add("test_category_1");
        categoryList.add(null);
        categoryList.add("test_category_2");
        categoryList.add("");
        Publisher publisher = spy(new Publisher.Builder()
                                          .setId("test_id")
                                          .setName("test_name")
                                          .setDomain("test_domain")
                                          .addCategory("test_category")
                                          .addCategories(categoryList)
                                          .build());
        BidMachine.setPublisher(publisher);
        request = (Request) adRequest.build(context, adRequest.getType());
        verify(publisher).build(any(Context.App.Builder.class));
        requestContext = request.getContext().unpack(Context.class);
        protoPublisher = requestContext.getApp().getPub();

        assertNotNull(protoPublisher);
        assertEquals("test_id", protoPublisher.getId());
        assertEquals("test_name", protoPublisher.getName());
        assertEquals("test_domain", protoPublisher.getDomain());
        assertEquals(3, protoPublisher.getCatList().size());
        assertEquals("test_category", protoPublisher.getCatList().get(0));
        assertEquals("test_category_1", protoPublisher.getCatList().get(1));
        assertEquals("test_category_2", protoPublisher.getCatList().get(2));

        //Null publisher
        BidMachine.setPublisher(null);
        request = (Request) adRequest.build(context, adRequest.getType());
        requestContext = request.getContext().unpack(Context.class);
        protoPublisher = requestContext.getApp().getPub();

        assertNotNull(protoPublisher);
        assertEquals(Context.App.Publisher.getDefaultInstance().getId(),
                     protoPublisher.getId());
        assertEquals(Context.App.Publisher.getDefaultInstance().getName(),
                     protoPublisher.getName());
        assertEquals(Context.App.Publisher.getDefaultInstance().getDomain(),
                     protoPublisher.getDomain());
        assertEquals(Context.App.Publisher.getDefaultInstance().getCatList(),
                     protoPublisher.getCatList());
    }

    @Test
    public void adRequestListeners() {
        AdRequest.AdRequestListener adRequestListener1 = mock(AdRequest.AdRequestListener.class);
        AdRequest.AdRequestListener adRequestListener2 = mock(AdRequest.AdRequestListener.class);
        AdRequest.AdRequestListener adRequestListener3 = mock(AdRequest.AdRequestListener.class);
        adRequest.addListener(adRequestListener1);
        adRequest.addListener(adRequestListener2);
        adRequest.addListener(adRequestListener3);
        adRequest.addListener(null);

        assertNotNull(adRequest.adRequestListeners);
        assertEquals(3, adRequest.adRequestListeners.size());
        assertEquals(adRequestListener1, adRequest.adRequestListeners.get(0));
        assertEquals(adRequestListener2, adRequest.adRequestListeners.get(1));
        assertEquals(adRequestListener3, adRequest.adRequestListeners.get(2));

        adRequest.removeListener(adRequestListener2);
        adRequest.removeListener(null);

        assertEquals(2, adRequest.adRequestListeners.size());
        assertEquals(adRequestListener1, adRequest.adRequestListeners.get(0));
        assertEquals(adRequestListener3, adRequest.adRequestListeners.get(1));
    }

    @Test
    public void internalAdRequestListeners() {
        AdRequest.InternalAdRequestListener internalAdRequestListener1 = mock(AdRequest.InternalAdRequestListener.class);
        AdRequest.InternalAdRequestListener internalAdRequestListener2 = mock(AdRequest.InternalAdRequestListener.class);
        AdRequest.InternalAdRequestListener internalAdRequestListener3 = mock(AdRequest.InternalAdRequestListener.class);
        adRequest.addInternalListener(internalAdRequestListener1);
        adRequest.addInternalListener(internalAdRequestListener2);
        adRequest.addInternalListener(internalAdRequestListener3);
        adRequest.addInternalListener(null);

        assertNotNull(adRequest.internalAdRequestListeners);
        assertEquals(3, adRequest.internalAdRequestListeners.size());
        assertEquals(internalAdRequestListener1, adRequest.internalAdRequestListeners.get(0));
        assertEquals(internalAdRequestListener2, adRequest.internalAdRequestListeners.get(1));
        assertEquals(internalAdRequestListener3, adRequest.internalAdRequestListeners.get(2));

        adRequest.removeInternalListener(internalAdRequestListener2);
        adRequest.removeInternalListener(null);

        assertEquals(2, adRequest.internalAdRequestListeners.size());
        assertEquals(internalAdRequestListener1, adRequest.internalAdRequestListeners.get(0));
        assertEquals(internalAdRequestListener3, adRequest.internalAdRequestListeners.get(1));
    }

    @Test
    public void request_isDestroyed_processRequestFailWithRequestDestroyed() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        adRequest.addListener(new SimpleAdRequestListener() {
            @Override
            public void onRequestFailed(@NonNull TestAdRequest request, @NonNull BMError error) {
                if (error.getCode() == ErrorReason.ERROR_REASON_WAS_DESTROYED_VALUE) {
                    countDownLatch.countDown();
                }
            }
        });
        adRequest.destroy();
        adRequest.request(context);
        assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void destroy_internalListenerPresent_notifyListeners() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        adRequest.addInternalListener(new AdRequest.InternalAdRequestListener<TestAdRequest>() {
            @Override
            public void onRequestDestroyed(@NonNull TestAdRequest request) {
                countDownLatch.countDown();
            }
        });
        adRequest.destroy();
        assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void headerBidding() throws Exception {
        //Default HeaderBidding
        Request request = (Request) adRequest.build(context, adRequest.getType());
        RequestExtension requestExtension = request.getExtProto(0).unpack(RequestExtension.class);
        assertEquals(HeaderBiddingType.HEADER_BIDDING_TYPE_ENABLED_VALUE,
                     requestExtension.getHeaderBiddingTypeValue());

        //Disabled HeaderBidding
        adRequest.headerBiddingEnabled = false;
        request = (Request) adRequest.build(context, adRequest.getType());
        requestExtension = request.getExtProto(0).unpack(RequestExtension.class);
        assertEquals(HeaderBiddingType.HEADER_BIDDING_TYPE_DISABLED_VALUE,
                     requestExtension.getHeaderBiddingTypeValue());

        //Enabled HeaderBidding
        adRequest.headerBiddingEnabled = true;
        request = (Request) adRequest.build(context, adRequest.getType());
        requestExtension = request.getExtProto(0).unpack(RequestExtension.class);
        assertEquals(HeaderBiddingType.HEADER_BIDDING_TYPE_ENABLED_VALUE,
                     requestExtension.getHeaderBiddingTypeValue());
    }

    @Test
    public void headerBiddingBuilder() {
        //Default HeaderBidding
        TestAdRequestBuilder builder = new TestAdRequestBuilder();
        assertTrue(builder.build().headerBiddingEnabled);

        //Disabled HeaderBidding
        builder.disableHeaderBidding();
        assertFalse(builder.build().headerBiddingEnabled);

        //Enabled HeaderBidding
        builder.enableHeaderBidding();
        assertTrue(builder.build().headerBiddingEnabled);
    }

    @Test
    public void extractTrackUrls() {
        Map<TrackEventType, List<String>> trackUrls = adRequest.trackUrls;
        assertNull(trackUrls);

        adRequest.extractTrackUrls(null);
        trackUrls = adRequest.trackUrls;
        assertNull(trackUrls);

        Response.Seatbid.Bid bid = Response.Seatbid.Bid.newBuilder().build();
        adRequest.extractTrackUrls(bid);
        trackUrls = adRequest.trackUrls;
        assertNotNull(trackUrls);
        assertEquals(0, trackUrls.size());

        bid = Response.Seatbid.Bid.newBuilder()
                .setPurl("test_url_win")
                .setLurl("test_url_loss")
                .build();
        adRequest.extractTrackUrls(bid);
        trackUrls = adRequest.trackUrls;
        assertNotNull(trackUrls);
        assertEquals(2, trackUrls.size());
        List<String> urlList = trackUrls.get(TrackEventType.MediationWin);
        assertNotNull(urlList);
        assertEquals(1, urlList.size());
        assertEquals("test_url_win", urlList.get(0));
        urlList = trackUrls.get(TrackEventType.MediationLoss);
        assertNotNull(urlList);
        assertEquals(1, urlList.size());
        assertEquals("test_url_loss", urlList.get(0));
    }

    @Test
    public void onShown() {
        TestAdRequest testAdRequest = new TestAdRequest.Builder(AdsType.Banner).build();
        assertFalse(testAdRequest.isAdWasShown());
        testAdRequest.onShown();
        assertTrue(testAdRequest.isAdWasShown());
    }

    @Test
    public void isBidPayloadValid_responsePayloadIsEmpty_returnTrue() {
        TestAdRequest testAdRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setPlacementObjectValid(false)
                .build();
        ResponsePayload responsePayload = ResponsePayload.newBuilder().build();
        boolean result = testAdRequest.isBidPayloadValid(responsePayload);
        assertTrue(result);
    }

    @Test
    public void isBidPayloadValid_responsePayloadNotEmptyAndPlacementValid_returnTrue() {
        TestAdRequest testAdRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setPlacementObjectValid(true)
                .build();
        ResponsePayload responsePayload = ResponsePayload.newBuilder()
                .setRequestItemSpec(Placement.newBuilder().build())
                .build();
        boolean result = testAdRequest.isBidPayloadValid(responsePayload);
        assertTrue(result);
    }

    @Test
    public void isBidPayloadValid_responsePayloadNotEmptyAndPlacementInvalid_returnFalse() {
        TestAdRequest testAdRequest = new TestAdRequest.Builder(AdsType.Banner)
                .setPlacementObjectValid(false)
                .build();
        ResponsePayload responsePayload = ResponsePayload.newBuilder()
                .setRequestItemSpec(Placement.newBuilder().build())
                .build();
        boolean result = testAdRequest.isBidPayloadValid(responsePayload);
        assertFalse(result);
    }

    @Test
    public void processBidPayload_responsePayloadInvalid_processRequestFail() {
        ResponsePayload responsePayload = ResponsePayload.newBuilder().build();
        doReturn(false).when(adRequest).isBidPayloadValid(responsePayload);
        adRequest.processBidPayload(responsePayload);
        verify(adRequest, new Times(0)).processApiRequestSuccess(any(Response.class));
        verify(adRequest, new Times(0)).retrieveBody(any(String.class));
        verify(adRequest).processRequestFail(BMError.IncorrectContent);
    }

    @Test
    public void processBidPayload_responsePayloadIsEmpty_processRequestFail() {
        ResponsePayload responsePayload = ResponsePayload.newBuilder().build();
        adRequest.processBidPayload(responsePayload);
        verify(adRequest, new Times(0)).processApiRequestSuccess(any(Response.class));
        verify(adRequest, new Times(0)).retrieveBody(any(String.class));
        verify(adRequest).processRequestFail(BMError.IncorrectContent);
    }

    @Test
    public void processBidPayload_responsePayloadContainsResponseCache_processApiRequestSuccess() {
        Openrtb openrtb = Openrtb.newBuilder().build();
        ResponsePayload responsePayload = ResponsePayload.newBuilder()
                .setResponseCache(openrtb)
                .build();
        adRequest.processBidPayload(responsePayload);
        verify(adRequest).processApiRequestSuccess(openrtb.getResponse());
        verify(adRequest, new Times(0)).retrieveBody(any(String.class));
        verify(adRequest, new Times(0)).processRequestFail(BMError.IncorrectContent);
    }

    @Test
    public void processBidPayload_responsePayloadContainsResponseCacheUrlWithText_processRequestFail() {
        String responseCacheUrl = "test_link";
        ResponsePayload responsePayload = ResponsePayload.newBuilder()
                .setResponseCacheUrl(responseCacheUrl)
                .build();
        adRequest.processBidPayload(responsePayload);
        verify(adRequest, new Times(0)).processApiRequestSuccess(any(Response.class));
        verify(adRequest, new Times(0)).retrieveBody(any(String.class));
        verify(adRequest).processRequestFail(BMError.IncorrectContent);
    }

    @Test
    public void processBidPayload_responsePayloadContainsResponseCacheUrlWithUrl_retrieveBody() {
        String responseCacheUrl = "http://test.com";
        ResponsePayload responsePayload = ResponsePayload.newBuilder()
                .setResponseCacheUrl(responseCacheUrl)
                .build();
        adRequest.processBidPayload(responsePayload);
        verify(adRequest, new Times(0)).processApiRequestSuccess(any(Response.class));
        verify(adRequest).retrieveBody(responseCacheUrl);
        verify(adRequest, new Times(0)).processRequestFail(BMError.IncorrectContent);
    }

}
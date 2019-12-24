package io.bidmachine;

import android.support.annotation.NonNull;

import com.explorestack.protobuf.adcom.Context;
import com.explorestack.protobuf.openrtb.Request;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import io.bidmachine.models.DataRestrictions;
import io.bidmachine.protobuf.HeaderBiddingType;
import io.bidmachine.protobuf.RequestExtension;
import io.bidmachine.unified.UnifiedAdRequestParams;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AdRequestTest {

    private android.content.Context context;
    private AdRequest adRequest;

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        adRequest = new TestAdRequest(AdsType.Interstitial);
        BidMachine.setConsentConfig(true, null);
        BidMachine.setSubjectToGDPR(null);
        BidMachine.initialize(context, "1");
    }

    @Test
    public void build_userRestrictionParamsNotSet_useDefaultValues() throws Exception {
        Request request = (Request) adRequest.build(context, adRequest.getType());
        Context requestContext = request.getContext().unpack(Context.class);
        assertEquals(
                Context.User.getDefaultInstance().getConsent(),
                requestContext.getUser().getConsent());
        assertFalse(requestContext.getRegs().getGdpr());
    }

    @Test
    public void build_userRestrictionParamsIsSet_useValuesFromUserRestrictionParams() throws Exception {
        UserRestrictionParams userRestrictionParams = new UserRestrictionParams();
        userRestrictionParams.setConsentConfig(
                true,
                "private_consent_string");
        userRestrictionParams.setSubjectToGDPR(true);
        adRequest.userRestrictionParams = userRestrictionParams;
        Request request = (Request) adRequest.build(context, adRequest.getType());
        Context requestContext = request.getContext().unpack(Context.class);
        assertEquals(
                "private_consent_string",
                requestContext.getUser().getConsent());
        assertTrue(requestContext.getRegs().getGdpr());
    }

    @Test
    public void build_userSetParamsFromPublicApi_useValuesFromPublicApi() throws Exception {
        BidMachine.setConsentConfig(false, "public_consent_string");
        BidMachine.setSubjectToGDPR(true);
        Request request = (Request) adRequest.build(context, adRequest.getType());
        Context requestContext = request.getContext().unpack(Context.class);
        assertEquals(
                "public_consent_string",
                requestContext.getUser().getConsent());
        assertTrue(requestContext.getRegs().getGdpr());
    }

    @Test
    public void build_userRestrictionParamsIsSetAndUserSetParamsFromApi_useValuesFromUserRestrictionParams() throws Exception {
        BidMachine.setConsentConfig(false, "public_consent_string");
        BidMachine.setSubjectToGDPR(true);
        UserRestrictionParams userRestrictionParams = new UserRestrictionParams();
        userRestrictionParams.setConsentConfig(
                true,
                "private_consent_string");
        userRestrictionParams.setSubjectToGDPR(false);
        adRequest.userRestrictionParams = userRestrictionParams;
        Request request = (Request) adRequest.build(context, adRequest.getType());
        Context requestContext = request.getContext().unpack(Context.class);
        assertEquals(
                "private_consent_string",
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
    public void headerBidding() throws Exception {
        //Default HeaderBidding
        Request request = (Request) adRequest.build(context, adRequest.getType());
        RequestExtension requestExtension = request.getExt(0).unpack(RequestExtension.class);
        assertEquals(HeaderBiddingType.HEADER_BIDDING_TYPE_ENABLED_VALUE,
                     requestExtension.getHeaderBiddingTypeValue());

        //Disabled HeaderBidding
        adRequest.headerBiddingEnabled = false;
        request = (Request) adRequest.build(context, adRequest.getType());
        requestExtension = request.getExt(0).unpack(RequestExtension.class);
        assertEquals(HeaderBiddingType.HEADER_BIDDING_TYPE_DISABLED_VALUE,
                     requestExtension.getHeaderBiddingTypeValue());

        //Enabled HeaderBidding
        adRequest.headerBiddingEnabled = true;
        request = (Request) adRequest.build(context, adRequest.getType());
        requestExtension = request.getExt(0).unpack(RequestExtension.class);
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

    private static class TestAdRequest extends AdRequest {

        TestAdRequest(@NonNull AdsType adsType) {
            super(adsType);
        }

        @NonNull
        @Override
        protected UnifiedAdRequestParams createUnifiedAdRequestParams(@NonNull TargetingParams targetingParams,
                                                                      @NonNull DataRestrictions dataRestrictions) {
            return new BaseUnifiedAdRequestParams(targetingParams, dataRestrictions);
        }

    }

    private static class TestAdRequestBuilder extends AdRequest.AdRequestBuilderImpl<TestAdRequestBuilder, TestAdRequest> {

        @Override
        protected TestAdRequest createRequest() {
            return new TestAdRequest(AdsType.Interstitial);
        }

    }

}
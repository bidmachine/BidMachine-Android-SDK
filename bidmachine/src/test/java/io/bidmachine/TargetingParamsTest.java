package io.bidmachine;

import android.location.Location;

import com.explorestack.protobuf.adcom.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import io.bidmachine.utils.Gender;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class TargetingParamsTest {

    @Test
    public void merge_bothParamsAreEmpty_returnNull() {
        TargetingParams targetingParams = new TargetingParams();
        TargetingParams publisherTargetingParams = new TargetingParams();
        publisherTargetingParams.merge(targetingParams);

        assertNull(publisherTargetingParams.getUserId());
        assertNull(publisherTargetingParams.getGender());
        assertNull(publisherTargetingParams.getBirthdayYear());
        assertNull(publisherTargetingParams.getKeywords());
        assertNull(publisherTargetingParams.getCountry());
        assertNull(publisherTargetingParams.getCity());
        assertNull(publisherTargetingParams.getZip());
        assertNull(publisherTargetingParams.getDeviceLocation());
        assertNull(publisherTargetingParams.getStoreUrl());
        assertNull(publisherTargetingParams.getStoreCategory());
        assertNull(publisherTargetingParams.getStoreSubCategories());
        assertNull(publisherTargetingParams.getFramework());
        assertNull(publisherTargetingParams.getPaid());
        assertNull(publisherTargetingParams.getExternalUserIdList());
    }

    @Test
    public void merge_publisherNotSetParams_returnBidMachineParams() {
        String userId = "user_id";
        Gender gender = Gender.Female;
        Integer birthdayYear = 1990;
        String[] keywords = new String[]{"keyword_1", "keyword_2"};
        String country = "country";
        String city = "city";
        String zip = "zip";
        Location deviceLocation = new Location("location");
        String storeUrl = "store_url";
        String storeCategory = "store_category";
        String[] storeSubCategories = new String[]{"store_sub_category_1", "store_sub_category_2"};
        String framework = "framework";
        Boolean paid = true;
        List<ExternalUserId> externalUserIdList = new ArrayList<ExternalUserId>() {{
            add(new ExternalUserId("source_id_1", "value_1"));
            add(new ExternalUserId("source_id_2", "value_2"));
        }};
        TargetingParams targetingParams = new TargetingParams()
                .setUserId(userId)
                .setGender(gender)
                .setBirthdayYear(birthdayYear)
                .setKeywords(keywords)
                .setCountry(country)
                .setCity(city)
                .setZip(zip)
                .setDeviceLocation(deviceLocation)
                .setStoreUrl(storeUrl)
                .setStoreCategory(storeCategory)
                .setStoreSubCategories(storeSubCategories)
                .setFramework(framework)
                .setPaid(paid)
                .setExternalUserIds(externalUserIdList);

        TargetingParams publisherTargetingParams = new TargetingParams();
        publisherTargetingParams.merge(targetingParams);

        assertEquals(userId, publisherTargetingParams.getUserId());
        assertEquals(gender, publisherTargetingParams.getGender());
        assertEquals(birthdayYear, publisherTargetingParams.getBirthdayYear());
        assertArrayEquals(keywords, publisherTargetingParams.getKeywords());
        assertEquals(country, publisherTargetingParams.getCountry());
        assertEquals(city, publisherTargetingParams.getCity());
        assertEquals(zip, publisherTargetingParams.getZip());
        assertEquals(deviceLocation, publisherTargetingParams.getDeviceLocation());
        assertEquals(storeUrl, publisherTargetingParams.getStoreUrl());
        assertEquals(storeCategory, publisherTargetingParams.getStoreCategory());
        assertArrayEquals(storeSubCategories, publisherTargetingParams.getStoreSubCategories());
        assertEquals(framework, publisherTargetingParams.getFramework());
        assertEquals(paid, publisherTargetingParams.getPaid());
        assertEquals(2, publisherTargetingParams.getExternalUserIdList().size());
        assertEquals(externalUserIdList.get(0),
                     publisherTargetingParams.getExternalUserIdList().get(0));
        assertEquals(externalUserIdList.get(1),
                     publisherTargetingParams.getExternalUserIdList().get(1));
    }

    @Test
    public void merge_presentBothParams_returnPublisherParams() {
        String userId = "user_id";
        Gender gender = Gender.Female;
        Integer birthdayYear = 1990;
        String[] keywords = new String[]{"keyword_1", "keyword_2"};
        String country = "country";
        String city = "city";
        String zip = "zip";
        Location deviceLocation = new Location("location");
        String storeUrl = "store_url";
        String storeCategory = "store_category";
        String[] storeSubCategories = new String[]{"store_sub_category_1", "store_sub_category_2"};
        String framework = "framework";
        Boolean paid = true;
        List<ExternalUserId> externalUserIdList = new ArrayList<ExternalUserId>() {{
            add(new ExternalUserId("source_id_1", "value_1"));
            add(new ExternalUserId("source_id_2", "value_2"));
        }};
        TargetingParams targetingParams = new TargetingParams()
                .setUserId(userId)
                .setGender(gender)
                .setBirthdayYear(birthdayYear)
                .setKeywords(keywords)
                .setCountry(country)
                .setCity(city)
                .setZip(zip)
                .setDeviceLocation(deviceLocation)
                .setStoreUrl(storeUrl)
                .setStoreCategory(storeCategory)
                .setStoreSubCategories(storeSubCategories)
                .setFramework(framework)
                .setPaid(paid)
                .setExternalUserIds(externalUserIdList);

        String publisherUserId = "publisher_user_id";
        Gender publisherGender = Gender.Female;
        Integer publisherBirthdayYear = 1990;
        String[] publisherKeywords = new String[]{"publisher_keyword_1", "publisher_keyword_2"};
        String publisherCountry = "publisher_country";
        String publisherCity = "publisher_city";
        String publisherZip = "publisher_zip";
        Location publisherDeviceLocation = new Location("publisher_location");
        String publisherStoreUrl = "publisher_store_url";
        String publisherStoreCategory = "publisher_store_category";
        String[] publisherStoreSubCategories = new String[]{"publisher_store_sub_category_1", "publisher_store_sub_category_2"};
        String publisherFramework = "publisher_framework";
        Boolean publisherPaid = false;
        List<ExternalUserId> publisherExternalUserIdList = new ArrayList<ExternalUserId>() {{
            add(new ExternalUserId("publisher_source_id_1", "publisher_value_1"));
            add(new ExternalUserId("publisher_source_id_2", "publisher_value_2"));
        }};
        TargetingParams publisherTargetingParams = new TargetingParams()
                .setUserId(publisherUserId)
                .setGender(publisherGender)
                .setBirthdayYear(publisherBirthdayYear)
                .setKeywords(publisherKeywords)
                .setCountry(publisherCountry)
                .setCity(publisherCity)
                .setZip(publisherZip)
                .setDeviceLocation(publisherDeviceLocation)
                .setStoreUrl(publisherStoreUrl)
                .setStoreCategory(publisherStoreCategory)
                .setStoreSubCategories(publisherStoreSubCategories)
                .setFramework(publisherFramework)
                .setPaid(publisherPaid)
                .setExternalUserIds(publisherExternalUserIdList);
        publisherTargetingParams.merge(targetingParams);

        assertEquals(publisherUserId, publisherTargetingParams.getUserId());
        assertEquals(publisherGender, publisherTargetingParams.getGender());
        assertEquals(publisherBirthdayYear, publisherTargetingParams.getBirthdayYear());
        assertArrayEquals(publisherKeywords, publisherTargetingParams.getKeywords());
        assertEquals(publisherCountry, publisherTargetingParams.getCountry());
        assertEquals(publisherCity, publisherTargetingParams.getCity());
        assertEquals(publisherZip, publisherTargetingParams.getZip());
        assertEquals(publisherDeviceLocation, publisherTargetingParams.getDeviceLocation());
        assertEquals(publisherStoreUrl, publisherTargetingParams.getStoreUrl());
        assertEquals(publisherStoreCategory, publisherTargetingParams.getStoreCategory());
        assertArrayEquals(publisherStoreSubCategories,
                          publisherTargetingParams.getStoreSubCategories());
        assertEquals(publisherFramework, publisherTargetingParams.getFramework());
        assertEquals(publisherPaid, publisherTargetingParams.getPaid());
        assertEquals(2, publisherTargetingParams.getExternalUserIdList().size());
        assertEquals(publisherExternalUserIdList.get(0),
                     publisherTargetingParams.getExternalUserIdList().get(0));
        assertEquals(publisherExternalUserIdList.get(1),
                     publisherTargetingParams.getExternalUserIdList().get(1));
    }

    @Test
    public void merge_presentBothParamsButPartly_returnFusedParams() {
        String userId = "user_id";
        Gender gender = Gender.Female;
        Integer birthdayYear = 1990;
        String[] keywords = new String[]{"keyword_1", "keyword_2"};
        String country = "country";
        String city = "city";
        String zip = "zip";
        TargetingParams targetingParams = new TargetingParams()
                .setUserId(userId)
                .setGender(gender)
                .setBirthdayYear(birthdayYear)
                .setKeywords(keywords)
                .setCountry(country)
                .setCity(city)
                .setZip(zip);

        Location publisherDeviceLocation = new Location("publisher_location");
        String publisherStoreUrl = "publisher_store_url";
        String publisherStoreCategory = "publisher_store_category";
        String[] publisherStoreSubCategories = new String[]{"publisher_store_sub_category_1", "publisher_store_sub_category_2"};
        String publisherFramework = "publisher_framework";
        Boolean publisherPaid = false;
        List<ExternalUserId> publisherExternalUserIdList = new ArrayList<ExternalUserId>() {{
            add(new ExternalUserId("publisher_source_id_1", "publisher_value_1"));
            add(new ExternalUserId("publisher_source_id_2", "publisher_value_2"));
        }};
        TargetingParams publisherTargetingParams = new TargetingParams()
                .setDeviceLocation(publisherDeviceLocation)
                .setStoreUrl(publisherStoreUrl)
                .setStoreCategory(publisherStoreCategory)
                .setStoreSubCategories(publisherStoreSubCategories)
                .setFramework(publisherFramework)
                .setPaid(publisherPaid)
                .setExternalUserIds(publisherExternalUserIdList);
        publisherTargetingParams.merge(targetingParams);

        assertEquals(userId, publisherTargetingParams.getUserId());
        assertEquals(gender, publisherTargetingParams.getGender());
        assertEquals(birthdayYear, publisherTargetingParams.getBirthdayYear());
        assertArrayEquals(keywords, publisherTargetingParams.getKeywords());
        assertEquals(country, publisherTargetingParams.getCountry());
        assertEquals(city, publisherTargetingParams.getCity());
        assertEquals(zip, publisherTargetingParams.getZip());
        assertEquals(publisherDeviceLocation, publisherTargetingParams.getDeviceLocation());
        assertEquals(publisherStoreUrl, publisherTargetingParams.getStoreUrl());
        assertEquals(publisherStoreCategory, publisherTargetingParams.getStoreCategory());
        assertArrayEquals(publisherStoreSubCategories,
                          publisherTargetingParams.getStoreSubCategories());
        assertEquals(publisherFramework, publisherTargetingParams.getFramework());
        assertEquals(publisherPaid, publisherTargetingParams.getPaid());
        assertEquals(2, publisherTargetingParams.getExternalUserIdList().size());
        assertEquals(publisherExternalUserIdList.get(0),
                     publisherTargetingParams.getExternalUserIdList().get(0));
        assertEquals(publisherExternalUserIdList.get(1),
                     publisherTargetingParams.getExternalUserIdList().get(1));
    }

    @Test
    public void build_externalUserIdListIsNull_dataNotAdded() {
        Context.User.Builder builder = Context.User.newBuilder();
        TargetingParams targetingParams = new TargetingParams();
        targetingParams.build(builder);

        assertEquals(0, builder.getDataCount());
    }

    @Test
    public void build_externalUserIdListIsEmpty_dataNotAdded() {
        Context.User.Builder builder = Context.User.newBuilder();
        TargetingParams targetingParams = new TargetingParams()
                .setExternalUserIds(new ArrayList<ExternalUserId>());
        targetingParams.build(builder);

        assertEquals(0, builder.getDataCount());
    }

    @Test
    public void build_externalUserIdListIsFilled_dataAddedWithSegments() {
        Context.User.Builder builder = Context.User.newBuilder();
        TargetingParams targetingParams = new TargetingParams()
                .setExternalUserIds(new ArrayList<ExternalUserId>() {{
                    add(new ExternalUserId(null, null));
                    add(new ExternalUserId("", ""));
                    add(new ExternalUserId("", "value_3"));
                    add(new ExternalUserId("source_id_4", ""));
                    add(new ExternalUserId("source_id_5", "value_5"));
                    add(new ExternalUserId("source_id_6", "value_6"));
                }});
        targetingParams.build(builder);

        assertEquals(1, builder.getDataCount());
        Context.Data data = builder.getData(0);
        assertEquals(TargetingParams.DATA_ID_EXTERNAL_USER_ID, data.getId());
        List<Context.Data.Segment> segmentList = data.getSegmentList();
        assertEquals(2, segmentList.size());
        Context.Data.Segment segment1 = segmentList.get(0);
        assertEquals("source_id_5", segment1.getId());
        assertEquals("value_5", segment1.getValue());
        Context.Data.Segment segment2 = segmentList.get(1);
        assertEquals("source_id_6", segment2.getId());
        assertEquals("value_6", segment2.getValue());
    }

}
package io.bidmachine;

import android.location.Location;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.explorestack.protobuf.ListValue;
import com.explorestack.protobuf.Struct;
import com.explorestack.protobuf.Value;
import com.explorestack.protobuf.adcom.Context;

import java.util.List;

import io.bidmachine.core.Utils;
import io.bidmachine.models.ITargetingParams;
import io.bidmachine.models.RequestParams;
import io.bidmachine.utils.Gender;

import static io.bidmachine.core.Utils.oneOf;

public final class TargetingParams extends RequestParams<TargetingParams> implements ITargetingParams<TargetingParams> {

    @VisibleForTesting
    static final String DATA_ID_EXTERNAL_USER_ID = "external_user_ids";

    private String userId;
    private Gender gender;
    private Integer birthdayYear;
    private String[] keywords;
    private String country;
    private String city;
    private String zip;
    private Location deviceLocation;
    private String storeUrl;
    private String storeCategory;
    private String[] storeSubCategories;
    private String framework;
    private Boolean isPaid;
    private BlockedParams blockedParams;
    private List<ExternalUserId> externalUserIdList;

    Location getDeviceLocation() {
        return deviceLocation;
    }

    BlockedParams getBlockedParams() {
        return blockedParams;
    }

    @Override
    public void merge(@NonNull TargetingParams instance) {
        userId = oneOf(userId, instance.userId);
        gender = oneOf(gender, instance.gender);
        birthdayYear = oneOf(birthdayYear, instance.birthdayYear);
        keywords = oneOf(keywords, instance.keywords);
        country = oneOf(country, instance.country);
        city = oneOf(city, instance.city);
        zip = oneOf(zip, instance.zip);
        deviceLocation = oneOf(deviceLocation, instance.deviceLocation);
        storeUrl = oneOf(storeUrl, instance.storeUrl);
        storeCategory = oneOf(storeCategory, instance.storeCategory);
        storeSubCategories = oneOf(storeSubCategories, instance.storeSubCategories);
        framework = oneOf(framework, instance.framework);
        isPaid = oneOf(isPaid, instance.isPaid);
        externalUserIdList = oneOf(externalUserIdList, instance.externalUserIdList);
        if (instance.blockedParams != null) {
            if (blockedParams == null) {
                blockedParams = new BlockedParams();
            }
            blockedParams.merge(instance.blockedParams);
        }
    }

    void build(android.content.Context context, Context.App.Builder builder) {
        String packageName = context.getPackageName();
        if (packageName != null) {
            builder.setBundle(packageName);
        }
        String appVersion = Utils.getAppVersion(context);
        if (appVersion != null) {
            builder.setVer(appVersion);
        }
        String appName = Utils.getAppName(context);
        if (appName != null) {
            builder.setName(appName);
        }
        if (storeUrl != null) {
            builder.setStoreurl(storeUrl);
        }
        builder.setPaid(isPaid != null && isPaid);
    }

    void fillAppExtension(Struct.Builder appExtBuilder) {
        if (storeCategory != null) {
            appExtBuilder.putFields(ProtoExtConstants.Context.App.STORE_CATEGORY,
                                    Value.newBuilder()
                                            .setStringValue(storeCategory)
                                            .build());
        }
        if (storeSubCategories != null && storeSubCategories.length > 0) {
            ListValue.Builder listValueBuilder = ListValue.newBuilder();
            for (String storeSubCategory : storeSubCategories) {
                listValueBuilder.addValues(Value.newBuilder()
                                                   .setStringValue(storeSubCategory)
                                                   .build());
            }
            appExtBuilder.putFields(ProtoExtConstants.Context.App.STORE_SUB_CATEGORY,
                                    Value.newBuilder()
                                            .setListValue(listValueBuilder.build())
                                            .build());
        }
        if (framework != null) {
            appExtBuilder.putFields(ProtoExtConstants.Context.App.FRAMEWORK,
                                    Value.newBuilder()
                                            .setStringValue(framework)
                                            .build());
        }
        appExtBuilder.putFields(ProtoExtConstants.Context.App.API_LEVEL, Value.newBuilder()
                .setNumberValue(Build.VERSION.SDK_INT)
                .build());
    }

    void build(Context.User.Builder builder) {
        // User id
        if (userId != null) {
            builder.setId(userId);
        }
        // Birthday year
        if (birthdayYear != null) {
            builder.setYob(birthdayYear);
        }
        // Gender
        if (gender != null) {
            builder.setGender(gender.getOrtbValue());
        }
        // Keywords
        if (keywords != null && keywords.length > 0) {
            final StringBuilder keywordsBuilder = new StringBuilder();
            for (String keyword : keywords) {
                if (keywordsBuilder.length() > 0) keywordsBuilder.append(",");
                keywordsBuilder.append(keyword);
            }
            builder.setKeywords(keywordsBuilder.toString());
        }
        // Geo
        final Context.Geo.Builder geoBuilder = Context.Geo.newBuilder();
        build(geoBuilder);
        OrtbUtils.locationToGeo(geoBuilder, null, false);
        builder.setGeo(geoBuilder);

        // Data
        if (externalUserIdList != null && externalUserIdList.size() > 0) {
            Context.Data.Builder dataBuilder = Context.Data.newBuilder()
                    .setId(DATA_ID_EXTERNAL_USER_ID);
            for (ExternalUserId externalUserId : externalUserIdList) {
                String sourceId = externalUserId.getSourceId();
                String value = externalUserId.getValue();
                if (TextUtils.isEmpty(sourceId) || TextUtils.isEmpty(value)) {
                    continue;
                }
                dataBuilder.addSegment(Context.Data.Segment.newBuilder()
                                               .setId(sourceId)
                                               .setValue(value));
            }
            builder.addData(dataBuilder);
        }
    }

    void build(Context.Geo.Builder builder) {
        if (country != null) {
            builder.setCountry(country);
        }
        if (city != null) {
            builder.setCity(city);
        }
        if (zip != null) {
            builder.setZip(zip);
        }
    }

    @Override
    public TargetingParams setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public TargetingParams setGender(Gender gender) {
        this.gender = gender;
        return this;
    }

    @Override
    public TargetingParams setBirthdayYear(Integer birthdayYear) {
        if (birthdayYear != null) {
            Utils.assertYear(birthdayYear);
        }
        this.birthdayYear = birthdayYear;
        return this;
    }

    @Override
    public TargetingParams setKeywords(String... keywords) {
        this.keywords = keywords;
        return this;
    }

    @Override
    public TargetingParams setCountry(String country) {
        this.country = country;
        return this;
    }

    @Override
    public TargetingParams setCity(String city) {
        this.city = city;
        return this;
    }

    @Override
    public TargetingParams setZip(String zip) {
        this.zip = zip;
        return this;
    }

    @Override
    public TargetingParams setStoreUrl(String url) {
        this.storeUrl = url;
        return this;
    }

    @Override
    public TargetingParams setStoreCategory(String storeCategory) {
        this.storeCategory = storeCategory;
        return this;
    }

    @Override
    public TargetingParams setStoreSubCategories(String... storeSubCategories) {
        this.storeSubCategories = storeSubCategories;
        return this;
    }

    @Override
    public TargetingParams setFramework(String framework) {
        this.framework = framework;
        return this;
    }

    @Override
    public TargetingParams setPaid(Boolean paid) {
        this.isPaid = paid;
        return this;
    }

    @Override
    public TargetingParams setDeviceLocation(Location location) {
        this.deviceLocation = location;
        return this;
    }

    @Override
    public TargetingParams setExternalUserIds(List<ExternalUserId> externalUserIdList) {
        this.externalUserIdList = externalUserIdList;
        return this;
    }

    @Override
    public TargetingParams addBlockedApplication(String bundleOrPackage) {
        prepareBlockParams();
        blockedParams.addBlockedApplication(bundleOrPackage);
        return this;
    }

    @Override
    public TargetingParams addBlockedAdvertiserIABCategory(String category) {
        prepareBlockParams();
        blockedParams.addBlockedAdvertiserIABCategory(category);
        return this;
    }

    @Override
    public TargetingParams addBlockedAdvertiserDomain(String domain) {
        prepareBlockParams();
        blockedParams.addBlockedAdvertiserDomain(domain);
        return this;
    }

    String getUserId() {
        return userId;
    }

    Gender getGender() {
        return gender;
    }

    Integer getBirthdayYear() {
        return birthdayYear;
    }

    String[] getKeywords() {
        return keywords;
    }

    String getCountry() {
        return country;
    }

    String getCity() {
        return city;
    }

    String getZip() {
        return zip;
    }

    String getStoreUrl() {
        return storeUrl;
    }

    Boolean getPaid() {
        return isPaid;
    }

    @VisibleForTesting
    String getStoreCategory() {
        return storeCategory;
    }

    @VisibleForTesting
    String[] getStoreSubCategories() {
        return storeSubCategories;
    }

    @VisibleForTesting
    String getFramework() {
        return framework;
    }

    @VisibleForTesting
    List<ExternalUserId> getExternalUserIdList() {
        return externalUserIdList;
    }

    private void prepareBlockParams() {
        if (blockedParams == null) {
            blockedParams = new BlockedParams();
        }
    }

}

package io.bidmachine;

import android.location.Location;

import androidx.annotation.NonNull;

import com.explorestack.protobuf.adcom.Context;

import io.bidmachine.core.Utils;
import io.bidmachine.models.ITargetingParams;
import io.bidmachine.models.RequestParams;
import io.bidmachine.utils.Gender;

import static io.bidmachine.core.Utils.oneOf;

public final class TargetingParams extends RequestParams<TargetingParams> implements ITargetingParams<TargetingParams> {

    private String userId;
    private Gender gender;
    private Integer birthdayYear;
    private String[] keywords;
    private String country;
    private String city;
    private String zip;
    private Location deviceLocation;
    private String storeUrl;
    private Boolean isPaid;
    private BlockedParams blockedParams;

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
        isPaid = oneOf(isPaid, instance.isPaid);
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

    void build(Context.User.Builder builder) {
        //User id
        if (userId != null) {
            builder.setId(userId);
        }
        //Birthday year
        if (birthdayYear != null) {
            builder.setYob(birthdayYear);
        }
        //Gender
        if (gender != null) {
            builder.setGender(gender.getOrtbValue());
        }
        //Keywords
        if (keywords != null && keywords.length > 0) {
            final StringBuilder keywordsBuilder = new StringBuilder();
            for (String keyword : keywords) {
                if (keywordsBuilder.length() > 0) keywordsBuilder.append(",");
                keywordsBuilder.append(keyword);
            }
            builder.setKeywords(keywordsBuilder.toString());
        }
        //Geo
        final Context.Geo.Builder geoBuilder = Context.Geo.newBuilder();
        build(geoBuilder);
        OrtbUtils.locationToGeo(geoBuilder, null, false);
        builder.setGeo(geoBuilder);
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

    private void prepareBlockParams() {
        if (blockedParams == null) {
            blockedParams = new BlockedParams();
        }
    }

}

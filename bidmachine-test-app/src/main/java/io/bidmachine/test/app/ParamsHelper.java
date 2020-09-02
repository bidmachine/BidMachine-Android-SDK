package io.bidmachine.test.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import io.bidmachine.BidMachine;
import io.bidmachine.ExtraParams;
import io.bidmachine.PriceFloorParams;
import io.bidmachine.Publisher;
import io.bidmachine.TargetingParams;
import io.bidmachine.models.IExtraParams;
import io.bidmachine.models.IPriceFloorParams;
import io.bidmachine.models.ITargetingParams;
import io.bidmachine.models.IUserRestrictionsParams;
import io.bidmachine.models.RequestBuilder;
import io.bidmachine.utils.Gender;

public class ParamsHelper implements ITargetingParams<ParamsHelper>,
        IUserRestrictionsParams<ParamsHelper>, IExtraParams<ParamsHelper>, IPriceFloorParams<ParamsHelper> {

    public enum AdsType {
        Global, Banner, Interstitial, Rewarded, Native
    }

    private static Map<AdsType, ParamsHelper> instances = new HashMap<>();

    private static boolean isRestored = false;

    public static ParamsHelper getInstance(Context context) {
        return getInstance(context, null);
    }

    public static ParamsHelper getInstance(Context context, @Nullable AdsType adsType) {
        if (adsType == null) {
            adsType = AdsType.Global;
        }
        if (!isRestored) {
            restoreParams(context);
            isRestored = true;
        }
        final ParamsHelper instance;
        if (!instances.containsKey(adsType)) {
            instance = new ParamsHelper(adsType);
            instances.put(adsType, instance);
        } else {
            instance = instances.get(adsType);
        }
        return instance;
    }

    @Nullable
    private AdsType adsType;

    private Gender currentGender;
    private Integer currentBirthdayYear;
    private String[] currentKeywords;
    private String currentUserID;
    private String currentCountry;
    private String currentCity;
    private String currentZip;
    private String storeUrl;
    private Location currentDeviceLocation;
    private Boolean isPaid;

    private List<String> blockedAdvertiserIABCategories;
    private List<String> blockedAdvertiserDomains;
    private List<String> blockedApplications;

    //Restrictions
    private Boolean hasConsent;
    private String consentString;
    private Boolean subjectToGDPR;
    private Boolean hasCoppa;
    private String usPrivacyString;

    private String sellerId = "5";
    private String initUrl = null;

    //Only testers params
    private String appBundle;
    private String appName;
    private String appVersion;

    private String publisherId;
    private String publisherName;
    private String publisherDomain;
    private List<String> publisherCategories;

    public ParamsHelper() {
    }

    private ParamsHelper(@Nullable AdsType adsType) {
        this.adsType = adsType;
    }

    @Nullable
    public AdsType getAdsType() {
        return adsType;
    }

    public void syncGlobalParams(Context context) {
        if (adsType == AdsType.Global) {
            BidMachine.setTargetingParams(obtainTargetingParams());
//            BidMachine.setExtraParams(obtainExtraParams());
            //Restrictions
            BidMachine.setConsentConfig(hasConsent == Boolean.TRUE, consentString);
            BidMachine.setSubjectToGDPR(subjectToGDPR == Boolean.TRUE);
            BidMachine.setCoppa(hasCoppa == Boolean.TRUE);
            BidMachine.setPublisher(new Publisher.Builder()
                                            .setId(publisherId)
                                            .setName(publisherName)
                                            .setDomain(publisherDomain)
                                            .addCategories(publisherCategories)
                                            .build());
            BidMachine.setUSPrivacyString(usPrivacyString);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().putString("IABUSPrivacy_String", usPrivacyString).apply();
        }
    }

    public <T extends RequestBuilder> T createParams(T params) {
        params.setTargetingParams(obtainTargetingParams());
//        params.setExtraParams(obtainExtraParams());
        params.setPriceFloorParams(obtainPriceFloorParams());
        return params;
    }

    public TargetingParams obtainTargetingParams() {
        final TargetingParams params = new TargetingParams()
                .setGender(currentGender)
                .setBirthdayYear(currentBirthdayYear)
                .setKeywords(currentKeywords)
                .setUserId(currentUserID)
                .setCountry(currentCountry)
                .setCity(currentCity)
                .setZip(currentZip)
                .setDeviceLocation(currentDeviceLocation)
                .setStoreUrl(storeUrl)
                .setPaid(isPaid);
        if (blockedAdvertiserIABCategories != null) {
            for (String value : blockedAdvertiserIABCategories) {
                params.addBlockedAdvertiserIABCategory(value);
            }
        }
        if (blockedAdvertiserDomains != null) {
            for (String value : blockedAdvertiserDomains) {
                params.addBlockedAdvertiserDomain(value);
            }
        }
        if (blockedApplications != null) {
            for (String value : blockedApplications) {
                params.addBlockedApplication(value);
            }
        }
        return params;
    }

    @Override
    public ParamsHelper setGender(Gender gender) {
        currentGender = gender;
        return this;
    }

    public Gender getCurrentGender() {
        return currentGender;
    }

    @Override
    public ParamsHelper setBirthdayYear(Integer birthdayYear) {
        this.currentBirthdayYear = birthdayYear;
        return null;
    }

    public Integer getCurrentBirthdayYear() {
        return currentBirthdayYear;
    }

    @Override
    public ParamsHelper setKeywords(@Nullable String... keywords) {
        currentKeywords = keywords;
        return this;
    }

    public String[] getCurrentKeywords() {
        return currentKeywords;
    }

    @Override
    public ParamsHelper setUserId(String userId) {
        currentUserID = userId;
        return this;
    }

    public String getCurrentUserID() {
        return currentUserID;
    }

    @Override
    public ParamsHelper setCountry(String country) {
        currentCountry = country;
        return this;
    }

    public String getCurrentCountry() {
        return currentCountry;
    }

    @Override
    public ParamsHelper setCity(String city) {
        currentCity = city;
        return this;
    }

    public String getCurrentCity() {
        return currentCity;
    }

    @Override
    public ParamsHelper setZip(String zip) {
        currentZip = zip;
        return this;
    }

    public String getCurrentZip() {
        return currentZip;
    }


    @Override
    public ParamsHelper setDeviceLocation(Location location) {
        currentDeviceLocation = location;
        return this;
    }

    public Location getCurrentDeviceLocation() {
        return currentDeviceLocation;
    }

    @Override
    public ParamsHelper setStoreUrl(String url) {
        this.storeUrl = url;
        return this;
    }

    public String getStoreUrl() {
        return storeUrl;
    }

    @Override
    public ParamsHelper setPaid(Boolean paid) {
        this.isPaid = paid;
        return this;
    }

    public Boolean isPaid() {
        return isPaid;
    }

    @Override
    public ParamsHelper setConsentConfig(boolean hasConsent, String consentString) {
        this.consentString = consentString;
        this.hasConsent = hasConsent;
        return this;
    }

    public String getConsentString() {
        return consentString;
    }

    @Override
    public ParamsHelper setSubjectToGDPR(Boolean subject) {
        this.subjectToGDPR = subject;
        return this;
    }

    public Boolean getSubjectToGDPR() {
        return subjectToGDPR;
    }

    @Override
    public ParamsHelper setCoppa(Boolean hasConsent) {
        this.hasCoppa = hasConsent;
        return this;
    }

    @Override
    public ParamsHelper setUSPrivacyString(String usPrivacyString) {
        this.usPrivacyString = usPrivacyString;
        return this;
    }

    public Boolean hasCoppa() {
        return hasCoppa;
    }

    public void setHasConsent(Boolean hasConsent) {
        this.hasConsent = hasConsent;
    }

    public Boolean getHasConsent() {
        return hasConsent;
    }

    public String getUSPrivacyString() {
        return usPrivacyString;
    }

    /*
    Utils
     */

    private static ArrayList<Locale> availableLocales;

    public static ArrayList<Locale> obtainAvailableLocales() {
        if (availableLocales == null) {
            availableLocales = new ArrayList<>();
            final Locale[] defaultLocales = Locale.getAvailableLocales();
            final ArrayList<String> addedCountries = new ArrayList<>();
            for (Locale locale : defaultLocales) {
                if (!TextUtils.isEmpty(locale.getCountry())
                        && !addedCountries.contains(locale.getCountry())) {
                    availableLocales.add(locale);
                    addedCountries.add(locale.getCountry());
                }
            }
            Collections.sort(availableLocales, new Comparator<Locale>() {
                @Override
                public int compare(Locale o1, Locale o2) {
                    return o1.getDisplayCountry().compareTo(o2.getDisplayCountry());
                }
            });
        }
        return availableLocales;
    }

    @Override
    public ParamsHelper addBlockedAdvertiserIABCategory(String category) {
        if (blockedAdvertiserIABCategories == null) {
            blockedAdvertiserIABCategories = new ArrayList<>();
        }
        blockedAdvertiserIABCategories.add(category);
        return this;
    }

    public void setBlockedAdvertiserIABCategories(List<String> categories) {
        blockedAdvertiserIABCategories = categories;
    }

    public List<String> getBlockedAdvertiserIABCategories() {
        return blockedAdvertiserIABCategories;
    }

    public void removeBlockedAdvertiserIABCategory(String category) {
        blockedAdvertiserIABCategories.remove(category);
    }

    @Override
    public ParamsHelper addBlockedAdvertiserDomain(String domain) {
        if (blockedAdvertiserDomains == null) {
            blockedAdvertiserDomains = new ArrayList<>();
        }
        blockedAdvertiserDomains.add(domain);
        return this;
    }

    public void setBlockedAdvertiserDomains(List<String> domains) {
        blockedAdvertiserDomains = domains;
    }

    public List<String> getBlockedAdvertiserDomains() {
        return blockedAdvertiserDomains;
    }

    public void removeBlockedAdvertiserDomain(String domain) {
        blockedAdvertiserDomains.remove(domain);
    }

    @Override
    public ParamsHelper addBlockedApplication(String bundleOrPackage) {
        if (blockedApplications == null) {
            blockedApplications = new ArrayList<>();
        }
        blockedApplications.add(bundleOrPackage);
        return this;
    }

    public void setBlockedApplications(List<String> applications) {
        blockedApplications = applications;
    }

    public List<String> getBlockedApplications() {
        return blockedApplications;
    }

    public void removeBlockedApplication(String bundleOrPackage) {
        blockedApplications.remove(bundleOrPackage);
    }

    private final Map<String, String> extraParamsMap = new HashMap<>();

    public ExtraParams obtainExtraParams() {
        ExtraParams extraParams = new ExtraParams();
        for (Map.Entry<String, String> entry : extraParamsMap.entrySet()) {
            extraParams.addExtra(entry.getKey(), entry.getValue());
        }
        return extraParams;
    }

    public void setExtraParams(Map<String, String> params) {
        extraParamsMap.clear();
        extraParamsMap.putAll(params);
    }

    public Map<String, String> getExtraParamsMap() {
        return extraParamsMap;
    }

    @Override
    public ParamsHelper addExtra(String key, String value) {
        extraParamsMap.put(key, value);
        return this;
    }

    private PriceFloorParams obtainPriceFloorParams() {
        PriceFloorParams priceFloorParams = new PriceFloorParams();
        for (Map.Entry<String, Double> priceFloor : priceFloorParamsMap.entrySet()) {
            priceFloorParams.addPriceFloor(priceFloor.getKey(), priceFloor.getValue());
        }
        return priceFloorParams;
    }

    private Map<String, Double> priceFloorParamsMap = new HashMap<>();

    @Override
    public ParamsHelper addPriceFloor(double value) {
        priceFloorParamsMap.put(UUID.randomUUID().toString(), value);
        return this;
    }

    @Override
    public ParamsHelper addPriceFloor(String id, double value) {
        priceFloorParamsMap.put(id, value);
        return this;
    }

    public void setPriceFloorParams(Map<String, Double> params) {
        priceFloorParamsMap.clear();
        priceFloorParamsMap.putAll(params);
    }

    public Map<String, Double> getPriceFloorParamsMap() {
        return priceFloorParamsMap;
    }

    private JsonElement toJson() {
        Gson gson = new GsonBuilder().create();
        return gson.toJsonTree(this);
    }

    public static void storeParams(Context context) {
        JsonArray jsonArray = new JsonArray();
        for (AdsType type : AdsType.values()) {
            ParamsHelper instance = instances.get(type);
            if (instance != null) {
                jsonArray.add(instance.toJson());
            }
        }
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString("StoredParams", jsonArray.toString())
                .apply();
    }

    private static void restoreParams(Context context) {
        String storedParams = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("StoredParams", null);

        if (!TextUtils.isEmpty(storedParams)) {
            Type listType = new TypeToken<List<ParamsHelper>>() {
            }.getType();
            List<ParamsHelper> helpers = new GsonBuilder().create()
                    .fromJson(storedParams, listType);
            if (helpers != null) {
                for (ParamsHelper helper : helpers) {
                    instances.put(helper.adsType, helper);
                }
            }
        }
    }

    public static void clearParams(Context context) {
        for (AdsType type : AdsType.values()) {
            if (instances.containsKey(type)) {
                instances.put(type, new ParamsHelper(type));
            }
        }
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .remove("StoredParams")
                .apply();
        for (OnClearedListener listener : clearListeners) {
            listener.onParamsCleared();
        }
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setInitUrl(String initUrl) {
        this.initUrl = initUrl;
    }

    public String getInitUrl() {
        return initUrl;
    }

    public void setAppBundle(String appBundle) {
        this.appBundle = appBundle;
    }

    public String getAppBundle() {
        return appBundle;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public String getPublisherDomain() {
        return publisherDomain;
    }

    public void setPublisherDomain(String publisherDomain) {
        this.publisherDomain = publisherDomain;
    }

    public List<String> getPublisherCategories() {
        return publisherCategories;
    }

    public void setPublisherCategories(List<String> publisherCategories) {
        this.publisherCategories = publisherCategories;
    }

    private static final ArrayList<OnClearedListener> clearListeners = new ArrayList<>();

    public static void addOnClearListener(OnClearedListener listener) {
        clearListeners.add(listener);
    }

    public static void removeOnClearListener(OnClearedListener listener) {
        clearListeners.remove(listener);
    }

    public interface OnClearedListener {
        void onParamsCleared();
    }
}

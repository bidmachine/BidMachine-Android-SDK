package io.bidmachine.models;

import android.location.Location;

import java.util.List;

import io.bidmachine.ExternalUserId;
import io.bidmachine.Framework;
import io.bidmachine.utils.Gender;

public interface ITargetingParams<SelfType> extends IBlockedParams<SelfType> {

    /**
     * Sets Vendor-specific target user Id
     *
     * @param userId Vendor-specific ID for the user
     * @return Self instance
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType setUserId(String userId);

    /**
     * Sets target user gender
     *
     * @param gender Gender, one of: Female, Male, Omitted {@link Gender}
     * @return Self instance
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType setGender(Gender gender);

    /**
     * Sets target user birthday year in 4-digit integer (e.g - 1990) format
     *
     * @param birthdayYear Year of birth as a 4-digit integer (e.g - 1990)
     * @return Self instance
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType setBirthdayYear(Integer birthdayYear);

    /**
     * Sets array of keywords, interests, or intents (Comma separated if you use xml)
     *
     * @param keywords Array of keywords
     * @return Self instance
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType setKeywords(String... keywords);

    /**
     * Sets location of the user's home (i.e., not necessarily their current location)
     *
     * @param location Location of the user's home (i.e., not necessarily their current location)
     * @return Self instance
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType setDeviceLocation(Location location);

    /**
     * Sets Country of the user's home (i.e., not necessarily their current location)
     *
     * @param country An uppercase ISO 3166 2-letter code, or a UN M.49 3-digit code.
     * @return Self instance
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType setCountry(String country);

    /**
     * Sets city of the user's home (i.e., not necessarily their current location)
     *
     * @param city User's city
     * @return Self instance
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType setCity(String city);

    /**
     * Sets ZIP of the user's home (i.e., not necessarily their current location)
     *
     * @param zip User's ZIP
     * @return Self instance
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType setZip(String zip);

    /**
     * Sets App store URL for an installed app; for <a href="https://cdn2.hubspot.net/hubfs/2848641/TrustworthyAccountabilityGroup_May2017/Docs/Summary-of-Changes-in-IQG-2.1.pdf?t=1504724070693">IQG 2.1</a> compliance.
     *
     * @param url App store url
     * @return Self instance
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType setStoreUrl(String url);

    /**
     * Sets App store category definitions (e.g - "games")
     *
     * @param storeCategory App store category
     * @return Self instance
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType setStoreCategory(String storeCategory);

    /**
     * Sets App Store Subcategory definitions. The array is always capped at 3 strings.
     *
     * @param storeSubCategories App Store Subcategory definitions
     * @return Self instance
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType setStoreSubCategories(String... storeSubCategories);

    /**
     * Sets app framework definitions. For example, If the app is using the Unity, put {@link Framework#UNITY}
     *
     * @param framework App framework
     * @return Self instance
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType setFramework(String framework);

    /**
     * Sets if it is paid app version
     *
     * @param paid {@code true} if it's paid app
     * @return Self instance
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType setPaid(Boolean paid);

    /**
     * Set external user id list
     *
     * @param externalUserIdList List of external user ids
     * @return Self instance
     */
    @SuppressWarnings("UnusedReturnValue")
    SelfType setExternalUserIds(List<ExternalUserId> externalUserIdList);

}

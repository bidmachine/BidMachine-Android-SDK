package io.bidmachine.models;

import android.location.Location;

import androidx.annotation.Nullable;

import io.bidmachine.utils.Gender;

public interface TargetingInfo {

    @Nullable
    String getUserId();

    @Nullable
    Gender getGender();

    @Nullable
    Integer getUserBirthdayYear();

    @Nullable
    Integer getUserAge();

    @Nullable
    String[] getKeywords();

    @Nullable
    Location getDeviceLocation();

    @Nullable
    String getCountry();

    @Nullable
    String getCity();

    @Nullable
    String getZip();

    @Nullable
    String getStoreUrl();

    @Nullable
    Boolean isPaid();

}
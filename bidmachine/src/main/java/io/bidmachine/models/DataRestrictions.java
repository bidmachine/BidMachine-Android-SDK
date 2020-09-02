package io.bidmachine.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface DataRestrictions {

    boolean canSendGeoPosition();

    boolean canSendUserInfo();

    boolean canSendDeviceInfo();

    boolean canSendIfa();

    boolean isUserInGdprScope();

    boolean isUserHasConsent();

    boolean isUserGdprProtected();

    boolean isUserAgeRestricted();

    @Nullable
    String getUsPrivacy();

    @NonNull
    String getIABGDPRString();

}

package io.bidmachine.models;

import android.support.annotation.Nullable;

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

}

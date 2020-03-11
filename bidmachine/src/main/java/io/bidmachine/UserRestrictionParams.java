package io.bidmachine;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.explorestack.protobuf.Any;
import com.explorestack.protobuf.adcom.Context;

import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.IUserRestrictionsParams;
import io.bidmachine.models.RequestParams;
import io.bidmachine.protobuf.RegsCcpaExtension;

import static io.bidmachine.core.Utils.oneOf;

final class UserRestrictionParams
        extends RequestParams<UserRestrictionParams>
        implements IUserRestrictionsParams<UserRestrictionParams>, DataRestrictions {

    private String gdprConsentString;
    private Boolean subjectToGDPR;
    private Boolean hasConsent;
    private Boolean hasCoppa;

    @Override
    public void merge(@NonNull UserRestrictionParams instance) {
        gdprConsentString = oneOf(gdprConsentString, instance.gdprConsentString);
        subjectToGDPR = oneOf(subjectToGDPR, instance.subjectToGDPR);
        hasConsent = oneOf(hasConsent, instance.hasConsent);
        hasCoppa = oneOf(hasCoppa, instance.hasCoppa);
    }

    void build(@NonNull Context.Regs.Builder builder) {
        builder.setGdpr(subjectToGDPR());
        builder.setCoppa(hasCoppa != null && hasCoppa);

        String iabUsPrivacyString = BidMachineImpl.get().getIabSharedPreference().getUSPrivacyString();
        if (!TextUtils.isEmpty(iabUsPrivacyString)) {
            assert iabUsPrivacyString != null;
            RegsCcpaExtension regsCcpaExtension = RegsCcpaExtension.newBuilder()
                    .setUsPrivacy(iabUsPrivacyString)
                    .build();
            builder.addExt(Any.pack(regsCcpaExtension));
        }
    }

    void build(@NonNull Context.User.Builder builder) {
        String consentString = oneOf(
                gdprConsentString,
                BidMachineImpl.get().getIabSharedPreference().getGDPRConsentString());
        if (consentString != null) {
            builder.setConsent(consentString);
        }
    }

    @Override
    public UserRestrictionParams setConsentConfig(boolean hasConsent, String consentString) {
        this.gdprConsentString = consentString;
        this.hasConsent = hasConsent;
        return this;
    }

    @Override
    public UserRestrictionParams setSubjectToGDPR(Boolean subject) {
        subjectToGDPR = subject;
        return this;
    }

    @Override
    public UserRestrictionParams setCoppa(Boolean coppa) {
        hasCoppa = coppa;
        return this;
    }

    private boolean subjectToGDPR() {
        Boolean subject = oneOf(subjectToGDPR, BidMachineImpl.get().getIabSharedPreference().getSubjectToGDPR());
        return subject != null && subject;
    }

    private boolean hasConsent() {
        return hasConsent != null && hasConsent;
    }

    private boolean hasCoppa() {
        return hasCoppa != null && hasCoppa;
    }

    @Override
    public boolean canSendGeoPosition() {
        return !hasCoppa() && !isUserGdprProtected();
    }

    @Override
    public boolean canSendUserInfo() {
        return !hasCoppa() && !isUserGdprProtected();
    }

    @Override
    public boolean canSendDeviceInfo() {
        return !hasCoppa();
    }

    @Override
    public boolean canSendIfa() {
        return !isUserGdprProtected();
    }

    @Override
    public boolean isUserInGdprScope() {
        return subjectToGDPR();
    }

    @Override
    public boolean isUserHasConsent() {
        return hasConsent();
    }

    @Override
    public boolean isUserGdprProtected() {
        return subjectToGDPR() && !hasConsent();
    }

    @Override
    public boolean isUserAgeRestricted() {
        return hasCoppa();
    }

}

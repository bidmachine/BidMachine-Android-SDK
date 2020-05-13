package io.bidmachine.app_event;

import android.location.Location;

import com.google.android.gms.ads.mediation.MediationAdRequest;

import java.util.Date;
import java.util.Set;

public class BMAdManagerMediationAdRequest implements MediationAdRequest {

    static final BMAdManagerMediationAdRequest instance = new BMAdManagerMediationAdRequest();

    @Override
    public Date getBirthday() {
        return null;
    }

    @Override
    public int getGender() {
        return 0;
    }

    @Override
    public Set<String> getKeywords() {
        return null;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public int taggedForChildDirectedTreatment() {
        return MediationAdRequest.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED;
    }

    @Override
    public boolean isTesting() {
        return false;
    }

    @Override
    public boolean isDesignedForFamilies() {
        return false;
    }

}
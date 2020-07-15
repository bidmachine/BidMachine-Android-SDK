package io.bidmachine;

import android.support.annotation.NonNull;

import java.lang.reflect.Field;

import io.bidmachine.models.DataRestrictions;
import io.bidmachine.unified.UnifiedNativeAdRequestParams;

import static org.junit.Assert.assertNull;

public class TestUtils {

    public static void changeInitUrl(String url) {
        BidMachineImpl.DEF_INIT_URL = url;
    }

    public static void restoreInitUrl() {
        BidMachineImpl.DEF_INIT_URL = BuildConfig.BM_API_URL + "init";
    }

    public static void resetBidMachineInstance() throws NoSuchFieldException, IllegalAccessException {
        Field field = BidMachineImpl.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);
        assertNull(field.get(null));
    }

    public static UnifiedNativeAdRequestParams createUnifiedNativeAdRequestParams(@NonNull AdRequest adRequest,
                                                                                  @NonNull TargetingParams targetingParams,
                                                                                  @NonNull DataRestrictions dataRestrictions) {
        return (UnifiedNativeAdRequestParams) adRequest
                .createUnifiedAdRequestParams(targetingParams, dataRestrictions);
    }

}

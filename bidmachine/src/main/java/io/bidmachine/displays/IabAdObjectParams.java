package io.bidmachine.displays;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.models.AdObjectParams;
import io.bidmachine.protobuf.AdExtension;
import io.bidmachine.unified.UnifiedMediationParams;

import static io.bidmachine.utils.IabUtils.KEY_COMPANION_SKIP_OFFSET;
import static io.bidmachine.utils.IabUtils.KEY_CREATIVE_ADM;
import static io.bidmachine.utils.IabUtils.KEY_CREATIVE_ID;
import static io.bidmachine.utils.IabUtils.KEY_HEIGHT;
import static io.bidmachine.utils.IabUtils.KEY_LOAD_SKIP_OFFSET;
import static io.bidmachine.utils.IabUtils.KEY_PRELOAD;
import static io.bidmachine.utils.IabUtils.KEY_SKIP_OFFSET;
import static io.bidmachine.utils.IabUtils.KEY_WIDTH;

abstract class IabAdObjectParams
        extends AdObjectParams
        implements UnifiedMediationParams.MappedUnifiedMediationParams.DataProvider {

    private Map<String, Object> params;
    private UnifiedMediationParams mediationParams = new UnifiedMediationParams.MappedUnifiedMediationParams(this);

    IabAdObjectParams(@NonNull Response.Seatbid seatbid,
                      @NonNull Response.Seatbid.Bid bid,
                      @NonNull Ad ad) {
        super(seatbid, bid, ad);
        getData().put(KEY_CREATIVE_ID, ad.getId());
    }

    @NonNull
    @Override
    public Map<String, Object> getData() {
        if (params == null) {
            params = new HashMap<>();
        }
        return params;
    }

    @Override
    protected void prepareExtensions(@NonNull Response.Seatbid seatbid,
                                     @NonNull Response.Seatbid.Bid bid,
                                     @NonNull AdExtension extension) {
        super.prepareExtensions(seatbid, bid, extension);
        getData().put(KEY_PRELOAD, extension.getPreload());
        getData().put(KEY_LOAD_SKIP_OFFSET, extension.getLoadSkipoffset());
        getData().put(KEY_SKIP_OFFSET, extension.getSkipoffset());
        getData().put(KEY_COMPANION_SKIP_OFFSET, extension.getCompanionSkipoffset());
    }

    public void setWidth(int width) {
        getData().put(KEY_WIDTH, width);
    }

    public void setHeight(int height) {
        getData().put(KEY_HEIGHT, height);
    }

    void setCreativeAdm(String creativeAdm) {
        getData().put(KEY_CREATIVE_ADM, creativeAdm);
    }

    @Override
    public boolean isValid() {
        Object creativeAdm = params.get(KEY_CREATIVE_ADM);
        return creativeAdm instanceof String && !TextUtils.isEmpty((CharSequence) creativeAdm);
    }

    @NonNull
    @Override
    public UnifiedMediationParams toMediationParams() {
        return mediationParams;
    }
}

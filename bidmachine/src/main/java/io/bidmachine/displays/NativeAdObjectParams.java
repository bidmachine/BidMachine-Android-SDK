package io.bidmachine.displays;

import android.support.annotation.NonNull;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;
import io.bidmachine.TrackEventType;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.unified.UnifiedMediationParams;

import java.util.HashMap;
import java.util.Map;

import static io.bidmachine.utils.IabUtils.*;

final class NativeAdObjectParams
        extends AdObjectParams
        implements UnifiedMediationParams.MappedUnifiedMediationParams.DataProvider {

    private HashMap<String, Object> params;
    private UnifiedMediationParams mediationParams = new UnifiedMediationParams.MappedUnifiedMediationParams(this);

    NativeAdObjectParams(Response.Seatbid seatbid, Response.Seatbid.Bid bid, Ad ad) {
        super(seatbid, bid, ad);
        prepareEvents(ad.getDisplay().getEventList());

        Ad.Display.Native ortbData = ad.getDisplay().getNative();
        Ad.Display.Native.LinkAsset linkAsset = ortbData.getLink();
        if (linkAsset != null && linkAsset != Ad.Display.Native.LinkAsset.getDefaultInstance()) {
            getData().put(KEY_CLICK_URL, linkAsset.getUrl());
            for (int i = 0; i < linkAsset.getTrkrCount(); i++) {
                addEvent(TrackEventType.Click, linkAsset.getTrkr(i));
            }
        }
        for (Ad.Display.Native.Asset asset : ortbData.getAssetList()) {
            switch (asset.getId()) {
                case NativePlacementBuilder.TITLE_ASSET_ID: {
                    getData().put(KEY_TITLE, asset.getTitle().getText());
                    break;
                }
                case NativePlacementBuilder.ICON_ASSET_ID: {
                    getData().put(KEY_ICON_URL, asset.getImage().getUrl());
                    break;
                }
                case NativePlacementBuilder.IMAGE_ASSET_ID: {
                    getData().put(KEY_IMAGE_URL, asset.getImage().getUrl());
                    break;
                }
                case NativePlacementBuilder.DESC_ASSET_ID: {
                    getData().put(KEY_DESCRIPTION, asset.getData().getValue());
                    break;
                }
                case NativePlacementBuilder.CTA_ASSET_ID: {
                    getData().put(KEY_CTA, asset.getData().getValue());
                    break;
                }
                case NativePlacementBuilder.RATING_ASSET_ID: {
                    getData().put(KEY_RATING, Float.valueOf(asset.getData().getValue()));
                    break;
                }
                case NativePlacementBuilder.VIDEO_ASSET_ID: {
                    if (!asset.getVideo().getCurl()
                            .equals(Ad.Display.Native.Asset.VideoAsset.getDefaultInstance().getCurl())) {
                        getData().put(KEY_VIDEO_URL, asset.getVideo().getCurl());
                    }
                    if (!asset.getVideo().getAdm()
                            .equals(Ad.Display.Native.Asset.VideoAsset.getDefaultInstance().getAdm())) {
                        getData().put(KEY_VIDEO_ADM, asset.getVideo().getAdm());
                    }
                    break;
                }
            }
        }
    }

    @NonNull
    @Override
    public Map<String, Object> getData() {
        if (params == null) {
            params = new HashMap<>();
        }
        return params;
    }

    @NonNull
    @Override
    public UnifiedMediationParams toMediationParams() {
        return mediationParams;
    }

    @Override
    public boolean isValid() {
        return true;
    }

}

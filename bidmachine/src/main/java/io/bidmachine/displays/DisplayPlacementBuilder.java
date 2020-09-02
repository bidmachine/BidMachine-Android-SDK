package io.bidmachine.displays;

import android.graphics.Point;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.explorestack.protobuf.Any;
import com.explorestack.protobuf.Message;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.adcom.ApiFramework;
import com.explorestack.protobuf.adcom.Placement;
import com.explorestack.protobuf.adcom.PlacementPosition;
import com.explorestack.protobuf.adcom.SizeUnit;
import com.explorestack.protobuf.openrtb.Response;

import java.util.Arrays;
import java.util.Collection;

import io.bidmachine.AdContentType;
import io.bidmachine.AdsType;
import io.bidmachine.Constants;
import io.bidmachine.ContextProvider;
import io.bidmachine.NetworkConfig;
import io.bidmachine.core.Utils;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.unified.UnifiedAdRequestParams;

public class DisplayPlacementBuilder<UnifiedAdRequestParamsType extends UnifiedAdRequestParams>
        extends PlacementBuilder<UnifiedAdRequestParamsType>
        implements ISizableDisplayPlacement<UnifiedAdRequestParamsType> {

    private boolean isFullscreen;

    public DisplayPlacementBuilder(boolean fullscreen, boolean supportHeaderBidding) {
        super(AdContentType.Static, supportHeaderBidding);
        this.isFullscreen = fullscreen;
    }

    @Override
    public void createPlacement(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedAdRequestParamsType adRequestParams,
                                @NonNull AdsType adsType,
                                @NonNull Collection<NetworkConfig> networkConfigs,
                                @NonNull PlacementCreateCallback callback) {
        Placement.DisplayPlacement.Builder builder = Placement.DisplayPlacement.newBuilder();
        builder.addApi(ApiFramework.API_FRAMEWORK_MRAID_2_0);
        builder.setUnit(SizeUnit.SIZE_UNIT_DIPS);
        builder.addAllMime(Arrays.asList(Constants.IMAGE_MIME_TYPES));
        if (isFullscreen) {
            builder.setInstl(true);
            builder.setPos(PlacementPosition.PLACEMENT_POSITION_FULLSCREEN);
        }
        Point displaySize = getSize(contextProvider, adRequestParams);
        builder.setW(displaySize.x);
        builder.setH(displaySize.y);
        Message.Builder headerBiddingPlacement =
                createHeaderBiddingPlacement(contextProvider, adRequestParams, adsType, networkConfigs);
        if (headerBiddingPlacement != null) {
            builder.addExtProto(Any.pack(headerBiddingPlacement.build()));
        }
        callback.onCreated(builder);
    }

    @Override
    public Point getSize(ContextProvider contextProvider, UnifiedAdRequestParamsType adRequestParams) {
        return Utils.getScreenSize(contextProvider.getContext());
    }

    @Override
    public AdObjectParams createAdObjectParams(@NonNull ContextProvider contextProvider,
                                               @NonNull UnifiedAdRequestParamsType adRequest,
                                               @NonNull Response.Seatbid seatbid,
                                               @NonNull Response.Seatbid.Bid bid,
                                               @NonNull Ad ad) {
        if (!ad.hasDisplay()) {
            return null;
        }
        AdObjectParams params = createHeaderBiddingAdObjectParams(contextProvider, adRequest, seatbid, bid, ad);
        if (params == null) {
            Ad.Display display = ad.getDisplay();
            if (TextUtils.isEmpty(display.getAdm())) {
                return null;
            }
            DisplayAdObjectParams displayParams = new DisplayAdObjectParams(seatbid, bid, ad);
            displayParams.setCreativeAdm(display.getAdm());
            displayParams.setWidth(display.getW());
            displayParams.setHeight(display.getH());
            params = displayParams;
        }
        return params;
    }

}

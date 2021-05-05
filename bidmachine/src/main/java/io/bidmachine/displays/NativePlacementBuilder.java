package io.bidmachine.displays;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.explorestack.protobuf.Any;
import com.explorestack.protobuf.Message;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.adcom.NativeDataAssetType;
import com.explorestack.protobuf.adcom.NativeImageAssetType;
import com.explorestack.protobuf.adcom.Placement;
import com.explorestack.protobuf.adcom.SizeUnit;
import com.explorestack.protobuf.adcom.VideoCreativeType;
import com.explorestack.protobuf.openrtb.Response;

import java.util.Arrays;
import java.util.Collection;

import io.bidmachine.AdContentType;
import io.bidmachine.AdsType;
import io.bidmachine.Constants;
import io.bidmachine.ContextProvider;
import io.bidmachine.MediaAssetType;
import io.bidmachine.NetworkConfig;
import io.bidmachine.models.AdObjectParams;
import io.bidmachine.unified.UnifiedNativeAdRequestParams;

public class NativePlacementBuilder extends PlacementBuilder<UnifiedNativeAdRequestParams> {

    static final int TITLE_ASSET_ID = 123;
    static final int DESC_ASSET_ID = 127;
    static final int CTA_ASSET_ID = 8;
    static final int RATING_ASSET_ID = 7;
    static final int ICON_ASSET_ID = 124;
    static final int IMAGE_ASSET_ID = 128;
    static final int VIDEO_ASSET_ID = 4;

    private static final Placement.DisplayPlacement.NativeFormat.AssetFormat.Builder titleAsset;
    private static final Placement.DisplayPlacement.NativeFormat.AssetFormat.Builder descAsset;
    private static final Placement.DisplayPlacement.NativeFormat.AssetFormat.Builder ctaAsset;
    private static final Placement.DisplayPlacement.NativeFormat.AssetFormat.Builder ratingAsset;

    static {
        // Title
        titleAsset = Placement.DisplayPlacement.NativeFormat.AssetFormat.newBuilder();
        titleAsset.setId(TITLE_ASSET_ID);
        titleAsset.setReq(true);
        titleAsset.setTitle(Placement.DisplayPlacement.NativeFormat.AssetFormat.TitleAssetFormat.newBuilder()
                                    .setLen(104)
                                    .build());

        // Data
        descAsset = Placement.DisplayPlacement.NativeFormat.AssetFormat.newBuilder();
        descAsset.setId(DESC_ASSET_ID);
        descAsset.setReq(true);
        descAsset.setData(Placement.DisplayPlacement.NativeFormat.AssetFormat.DataAssetFormat.newBuilder()
                                  .setType(NativeDataAssetType.NATIVE_DATA_ASSET_TYPE_DESC)
                                  .build());

        // Call to Action
        ctaAsset = Placement.DisplayPlacement.NativeFormat.AssetFormat.newBuilder();
        ctaAsset.setId(CTA_ASSET_ID);
        ctaAsset.setReq(true);
        ctaAsset.setData(Placement.DisplayPlacement.NativeFormat.AssetFormat.DataAssetFormat.newBuilder()
                                 .setType(NativeDataAssetType.NATIVE_DATA_ASSET_TYPE_CTA_TEXT)
                                 .build());

        // Rating
        ratingAsset = Placement.DisplayPlacement.NativeFormat.AssetFormat.newBuilder();
        ratingAsset.setId(RATING_ASSET_ID);
        ratingAsset.setReq(false);
        ratingAsset.setData(Placement.DisplayPlacement.NativeFormat.AssetFormat.DataAssetFormat.newBuilder()
                                    .setType(NativeDataAssetType.NATIVE_DATA_ASSET_TYPE_RATING)
                                    .build());
    }

    @VisibleForTesting
    static Placement.DisplayPlacement.NativeFormat.AssetFormat createIconAsset(@NonNull UnifiedNativeAdRequestParams adRequestParams) {
        Placement.DisplayPlacement.NativeFormat.AssetFormat.Builder asset =
                Placement.DisplayPlacement.NativeFormat.AssetFormat.newBuilder();
        asset.setId(ICON_ASSET_ID);
        asset.setReq(adRequestParams.containsAssetType(MediaAssetType.Icon));
        asset.setImg(Placement.DisplayPlacement.NativeFormat.AssetFormat.ImageAssetFormat.newBuilder()
                             .setType(NativeImageAssetType.NATIVE_IMAGE_ASSET_TYPE_ICON_IMAGE)
                             .addAllMime(Arrays.asList(Constants.IMAGE_MIME_TYPES))
                             .build());
        return asset.build();
    }

    @VisibleForTesting
    static Placement.DisplayPlacement.NativeFormat.AssetFormat createImageAsset(@NonNull UnifiedNativeAdRequestParams adRequestParams) {
        Placement.DisplayPlacement.NativeFormat.AssetFormat.Builder asset =
                Placement.DisplayPlacement.NativeFormat.AssetFormat.newBuilder();
        asset.setId(IMAGE_ASSET_ID);
        asset.setReq(adRequestParams.containsAssetType(MediaAssetType.Image));
        asset.setImg(Placement.DisplayPlacement.NativeFormat.AssetFormat.ImageAssetFormat.newBuilder()
                             .setType(NativeImageAssetType.NATIVE_IMAGE_ASSET_TYPE_MAIN_IMAGE)
                             .addAllMime(Arrays.asList(Constants.IMAGE_MIME_TYPES))
                             .build());
        return asset.build();
    }

    @VisibleForTesting
    static Placement.DisplayPlacement.NativeFormat.AssetFormat createVideoAsset(@NonNull UnifiedNativeAdRequestParams adRequestParams) {
        Placement.DisplayPlacement.NativeFormat.AssetFormat.Builder asset =
                Placement.DisplayPlacement.NativeFormat.AssetFormat.newBuilder();
        asset.setId(VIDEO_ASSET_ID);
        asset.setReq(adRequestParams.containsAssetType(MediaAssetType.Video));
        asset.setVideo(Placement.VideoPlacement.newBuilder()
                               .setSkip(false)
                               .addCtype(VideoCreativeType.VIDEO_CREATIVE_TYPE_VAST_2_0)
                               .addCtype(VideoCreativeType.VIDEO_CREATIVE_TYPE_VAST_3_0)
                               .addCtype(VideoCreativeType.VIDEO_CREATIVE_TYPE_VAST_WRAPPER_2_0)
                               .addCtype(VideoCreativeType.VIDEO_CREATIVE_TYPE_VAST_WRAPPER_3_0)
                               .addAllMime(Arrays.asList(Constants.VIDEO_MIME_TYPES))
                               .setMinbitr(Constants.VIDEO_MINBITR)
                               .setMaxbitr(Constants.VIDEO_MAXBITR)
                               .setMindur(Constants.VIDEO_MINDUR)
                               .setMaxdur(Constants.VIDEO_MAXDUR)
                               .setLinearValue(Constants.VIDEO_LINEARITY)
                               .build());
        return asset.build();
    }

    public NativePlacementBuilder(boolean supportHeaderBidding) {
        super(AdContentType.All, supportHeaderBidding);
    }

    @Override
    public void createPlacement(@NonNull ContextProvider contextProvider,
                                @NonNull UnifiedNativeAdRequestParams adRequestParams,
                                @NonNull AdsType adsType,
                                @NonNull Collection<NetworkConfig> networkConfigs,
                                @NonNull PlacementCreateCallback callback) throws Exception {
        Placement.DisplayPlacement.Builder builder = Placement.DisplayPlacement.newBuilder();
        builder.setInstl(false);
        builder.setUnit(SizeUnit.SIZE_UNIT_DIPS);

        Placement.DisplayPlacement.NativeFormat.Builder formatBuilder =
                Placement.DisplayPlacement.NativeFormat.newBuilder();
        formatBuilder.addAsset(titleAsset);
        formatBuilder.addAsset(descAsset);
        formatBuilder.addAsset(ctaAsset);
        formatBuilder.addAsset(ratingAsset);
        formatBuilder.addAsset(createIconAsset(adRequestParams));
        formatBuilder.addAsset(createImageAsset(adRequestParams));
        formatBuilder.addAsset(createVideoAsset(adRequestParams));
        builder.setNativefmt(formatBuilder);

        builder.addAllMime(Arrays.asList(Constants.IMAGE_MIME_TYPES));
        builder.addAllMime(Arrays.asList(Constants.VIDEO_MIME_TYPES));

        Message.Builder headerBiddingPlacement = createHeaderBiddingPlacement(contextProvider,
                                                                              adRequestParams,
                                                                              adsType,
                                                                              networkConfigs);
        if (headerBiddingPlacement != null) {
            builder.addExtProto(Any.pack(headerBiddingPlacement.build()));
        }
        callback.onCreated(builder);
    }

    @Override
    public AdObjectParams createAdObjectParams(@NonNull Response.Seatbid seatbid,
                                               @NonNull Response.Seatbid.Bid bid,
                                               @NonNull Ad ad) {
        AdObjectParams params = createHeaderBiddingAdObjectParams(seatbid, bid, ad);
        if (params == null && (ad.hasDisplay() && ad.getDisplay().hasNative())) {
            params = new NativeAdObjectParams(seatbid, bid, ad);
        }
        return params;
    }

}
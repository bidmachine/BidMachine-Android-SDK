package io.bidmachine.displays;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.explorestack.iab.utils.IabElementStyle;
import com.explorestack.protobuf.Value;
import com.explorestack.protobuf.adcom.Ad;
import com.explorestack.protobuf.openrtb.Response;

import java.util.HashMap;
import java.util.Map;

import io.bidmachine.models.AdObjectParams;
import io.bidmachine.protobuf.AdExtension;
import io.bidmachine.unified.UnifiedMediationParams;

import static io.bidmachine.utils.IabUtils.KEY_CLOSABLE_VIEW_STYLE;
import static io.bidmachine.utils.IabUtils.KEY_COMPANION_SKIP_OFFSET;
import static io.bidmachine.utils.IabUtils.KEY_COUNTDOWN_STYLE;
import static io.bidmachine.utils.IabUtils.KEY_CREATIVE_ADM;
import static io.bidmachine.utils.IabUtils.KEY_CREATIVE_ID;
import static io.bidmachine.utils.IabUtils.KEY_HEIGHT;
import static io.bidmachine.utils.IabUtils.KEY_IGNORE_SAFE_AREA_LAYOUT_GUIDE;
import static io.bidmachine.utils.IabUtils.KEY_LOAD_SKIP_OFFSET;
import static io.bidmachine.utils.IabUtils.KEY_OM_SDK_ENABLED;
import static io.bidmachine.utils.IabUtils.KEY_PRELOAD;
import static io.bidmachine.utils.IabUtils.KEY_PROGRESS_DURATION;
import static io.bidmachine.utils.IabUtils.KEY_PROGRESS_STYLE;
import static io.bidmachine.utils.IabUtils.KEY_R1;
import static io.bidmachine.utils.IabUtils.KEY_R2;
import static io.bidmachine.utils.IabUtils.KEY_SKIP_OFFSET;
import static io.bidmachine.utils.IabUtils.KEY_STORE_URL;
import static io.bidmachine.utils.IabUtils.KEY_USE_NATIVE_CLOSE;
import static io.bidmachine.utils.IabUtils.KEY_WIDTH;

abstract class IabAdObjectParams
        extends AdObjectParams
        implements UnifiedMediationParams.MappedUnifiedMediationParams.DataProvider {

    private Map<String, Object> params;
    private UnifiedMediationParams mediationParams =
            new UnifiedMediationParams.MappedUnifiedMediationParams(this);

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
        getData().put(KEY_USE_NATIVE_CLOSE, extension.getUseNativeClose());
        getData().put(KEY_SKIP_OFFSET, extension.getSkipoffset());
        getData().put(KEY_COMPANION_SKIP_OFFSET, extension.getCompanionSkipoffset());
        getData().put(KEY_R1, extension.getR1());
        getData().put(KEY_R2, extension.getR2());
        getData().put(KEY_IGNORE_SAFE_AREA_LAYOUT_GUIDE, extension.getIgnoresSafeAreaLayoutGuide());
        getData().put(KEY_STORE_URL, extension.getStoreUrl());
        getData().put(KEY_PROGRESS_DURATION, extension.getProgressDuration());

        IabElementStyle closeButtonIabElementStyle = transform(extension.getCloseButton());
        if (closeButtonIabElementStyle != null) {
            getData().put(KEY_CLOSABLE_VIEW_STYLE, closeButtonIabElementStyle);
        }
        IabElementStyle countdownIabElementStyle = transform(extension.getCountdown());
        if (countdownIabElementStyle != null) {
            getData().put(KEY_COUNTDOWN_STYLE, countdownIabElementStyle);
        }
        IabElementStyle progressIabElementStyle = transform(extension.getProgress());
        if (progressIabElementStyle != null) {
            getData().put(KEY_PROGRESS_STYLE, progressIabElementStyle);
        }
    }

    @Override
    protected void prepareExtensions(@NonNull Response.Seatbid seatbid,
                                     @NonNull Response.Seatbid.Bid bid,
                                     @NonNull Map<String, Value> extensionMap) {
        super.prepareExtensions(seatbid, bid, extensionMap);

        Value omsdkEnabledValue = extensionMap.get(KEY_OM_SDK_ENABLED);
        if (omsdkEnabledValue != null) {
            getData().put(KEY_OM_SDK_ENABLED, omsdkEnabledValue.getBoolValue());
        }
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

    @Nullable
    @VisibleForTesting
    IabElementStyle transform(@Nullable AdExtension.ControlAsset extensionControlView) {
        if (extensionControlView == null) {
            return null;
        }
        try {
            IabElementStyle iabElementStyle = new IabElementStyle();
            iabElementStyle.setMargin(extensionControlView.getMargin());
            iabElementStyle.setPadding(extensionControlView.getPadding());
            iabElementStyle.setContent(extensionControlView.getContent());
            iabElementStyle.setFillColor(parseColor(extensionControlView.getFill()));
            iabElementStyle.setFontStyle(extensionControlView.getFontStyle());
            iabElementStyle.setWidth(extensionControlView.getWidth());
            iabElementStyle.setHeight(extensionControlView.getHeight());
            iabElementStyle.setHideAfter((float) extensionControlView.getHideafter());
            iabElementStyle.setHorizontalPosition(parseHorizontalPosition(extensionControlView.getX()));
            iabElementStyle.setVerticalPosition(parseVerticalPosition(extensionControlView.getY()));
            iabElementStyle.setOpacity(extensionControlView.getOpacity());
            iabElementStyle.setOutlined(extensionControlView.getOutlined());
            iabElementStyle.setStrokeColor(parseColor(extensionControlView.getStroke()));
            iabElementStyle.setStrokeWidth((float) extensionControlView.getStrokeWidth());
            iabElementStyle.setStyle(extensionControlView.getStyle());
            iabElementStyle.setVisible(extensionControlView.getVisible());
            return iabElementStyle;
        } catch (Exception ignore) {
        }
        return null;
    }

    @Nullable
    @VisibleForTesting
    Integer parseColor(String color) {
        if (TextUtils.isEmpty(color)) {
            return null;
        }
        try {
            return Color.parseColor(color);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressLint("RtlHardcoded")
    @Nullable
    @VisibleForTesting
    Integer parseHorizontalPosition(String horizontalPosition) {
        if (TextUtils.isEmpty(horizontalPosition)) {
            return null;
        }
        switch (horizontalPosition) {
            case "left":
                return Gravity.LEFT;
            case "right":
                return Gravity.RIGHT;
            case "center":
                return Gravity.CENTER_HORIZONTAL;
            default:
                return null;
        }
    }

    @Nullable
    @VisibleForTesting
    Integer parseVerticalPosition(String verticalPosition) {
        if (TextUtils.isEmpty(verticalPosition)) {
            return null;
        }
        switch (verticalPosition) {
            case "top":
                return Gravity.TOP;
            case "bottom":
                return Gravity.BOTTOM;
            case "center":
                return Gravity.CENTER_VERTICAL;
            default:
                return null;
        }
    }

}
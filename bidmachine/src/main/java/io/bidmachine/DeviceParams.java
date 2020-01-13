package io.bidmachine;

import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import com.explorestack.protobuf.adcom.Context;
import com.explorestack.protobuf.adcom.DeviceType;
import com.explorestack.protobuf.adcom.OS;
import io.bidmachine.core.DeviceInfo;
import io.bidmachine.core.Utils;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.RequestParams;

import java.util.Locale;

final class DeviceParams extends RequestParams {

    private final String[] tmpOperatorInfo = new String[4];

    void build(@NonNull android.content.Context context,
               @NonNull Context.Device.Builder builder,
               @NonNull TargetingParams targetingParams,
               @NonNull TargetingParams defaultTargetingParams,
               @NonNull DataRestrictions restrictions) {
        final DeviceInfo deviceInfo = DeviceInfo.obtain(context);
        builder.setType(deviceInfo.isTablet ? DeviceType.DEVICE_TYPE_TABLET :
                DeviceType.DEVICE_TYPE_PHONE_DEVICE);
        builder.setOs(OS.OS_ANDROID);
        builder.setOsv(Build.VERSION.RELEASE);

        builder.setPxratio(deviceInfo.screenDensity);
        builder.setPpi(deviceInfo.screenDpi);

        final Point screenSize = Utils.getScreenSize(context);
        builder.setW(screenSize.x);
        builder.setH(screenSize.y);

        builder.setIfa(AdvertisingPersonalData.getAdvertisingId(context, !restrictions.canSendIfa()));
        builder.setLmt(AdvertisingPersonalData.isLimitAdTrackingEnabled());

        if (restrictions.canSendDeviceInfo()) {
            builder.setContype(OrtbUtils.getConnectionType(context));
            builder.setMake(Build.MANUFACTURER);

            if (deviceInfo.httpAgent != null) {
                builder.setUa(deviceInfo.httpAgent);
            }
            if (deviceInfo.model != null) {
                builder.setModel(deviceInfo.model);
                builder.setHwv(deviceInfo.deviceModel);
            }

            String lang = Locale.getDefault().getLanguage();
            if (lang != null) {
                builder.setLang(lang);
            }
            Utils.getOperatorInfo(context, tmpOperatorInfo);
            if (tmpOperatorInfo[Utils.INDEX_CRR] != null) {
                builder.setMccmnc(tmpOperatorInfo[Utils.INDEX_CRR]);
            }
            if (tmpOperatorInfo[Utils.INDEX_OPERATOR_NAME] != null) {
                builder.setCarrier(tmpOperatorInfo[Utils.INDEX_OPERATOR_NAME]);
            }
        }
        if (restrictions.canSendGeoPosition()) {
            Location location = OrtbUtils.obtainBestLocation(context,
                    targetingParams.getDeviceLocation(),
                    defaultTargetingParams.getDeviceLocation());
            builder.setGeo(OrtbUtils.locationToGeo(location, true));
        }
    }

    @Override
    public void merge(@NonNull RequestParams instance) {
        // ignore
    }
}

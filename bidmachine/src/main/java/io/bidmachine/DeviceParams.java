package io.bidmachine;

import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import com.explorestack.protobuf.ListValue;
import com.explorestack.protobuf.Struct;
import com.explorestack.protobuf.Value;
import com.explorestack.protobuf.adcom.Context;
import com.explorestack.protobuf.adcom.DeviceType;
import com.explorestack.protobuf.adcom.OS;

import java.util.Locale;
import java.util.Set;

import io.bidmachine.core.Utils;
import io.bidmachine.models.DataRestrictions;
import io.bidmachine.models.RequestParams;
import io.bidmachine.utils.BluetoothUtils;

final class DeviceParams extends RequestParams<DeviceParams> {

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
            }
            String hwv = deviceInfo.getHWV();
            if (hwv != null) {
                builder.setHwv(hwv);
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

    void fillDeviceExtension(android.content.Context context, Struct.Builder deviceExtBuilder) {
        Set<String> inputLanguageSet = io.bidmachine.Utils.getInputLanguageSet(context);
        if (inputLanguageSet.size() > 0) {
            ListValue.Builder listValueBuilder = ListValue.newBuilder();
            for (String inputLanguage : inputLanguageSet) {
                listValueBuilder.addValues(Value.newBuilder()
                                                   .setStringValue(inputLanguage)
                                                   .build());
            }
            deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.INPUT_LANGUAGE,
                                       Value.newBuilder()
                                               .setListValue(listValueBuilder.build())
                                               .build());
        }
        final DeviceInfo deviceInfo = DeviceInfo.obtain(context);
        Long availableDiskSpaceInMB = deviceInfo.getAvailableDiskSpaceInMB();
        if (availableDiskSpaceInMB != null) {
            deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.DISK_SPACE,
                                       Value.newBuilder()
                                               .setNumberValue(availableDiskSpaceInMB)
                                               .build());
        }
        Long totalDiskSpaceInMB = deviceInfo.getTotalDiskSpaceInMB();
        if (totalDiskSpaceInMB != null) {
            deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.TOTAL_DISK,
                                       Value.newBuilder()
                                               .setNumberValue(totalDiskSpaceInMB)
                                               .build());
        }
        Boolean isRingMuted = io.bidmachine.Utils.isRingMuted(context);
        if (isRingMuted != null) {
            deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.RING_MUTE,
                                       Value.newBuilder()
                                               .setNumberValue(isRingMuted ? 1 : 0)
                                               .build());
        }
        Boolean isCharging = io.bidmachine.Utils.isCharging(context);
        if (isCharging != null) {
            deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.CHARGING,
                                       Value.newBuilder()
                                               .setNumberValue(isCharging ? 1 : 0)
                                               .build());
        }
        Boolean isBluetoothEnabled = BluetoothUtils.isEnabled();
        if (isBluetoothEnabled != null) {
            deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.BLUETOOTH,
                                       Value.newBuilder()
                                               .setNumberValue(isBluetoothEnabled ? 1 : 0)
                                               .build());
        }
        Set<String> bluetoothConnectedDevices = BluetoothUtils.getConnectedDevices();
        if (bluetoothConnectedDevices != null && bluetoothConnectedDevices.size() > 0) {
            ListValue.Builder listValueBuilder = ListValue.newBuilder();
            for (String bluetoothConnectedDevice : bluetoothConnectedDevices) {
                listValueBuilder.addValues(Value.newBuilder()
                                                   .setStringValue(bluetoothConnectedDevice)
                                                   .build());
            }
            deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.BLUETOOTH_NAME,
                                       Value.newBuilder()
                                               .setListValue(listValueBuilder.build())
                                               .build());
        }
        Boolean isHeadsetConnected = BluetoothUtils.isHeadsetConnected();
        if (isHeadsetConnected != null) {
            deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.HEADSET,
                                       Value.newBuilder()
                                               .setNumberValue(isHeadsetConnected ? 1 : 0)
                                               .build());
        }
        Integer batteryLevel = io.bidmachine.Utils.getBatteryLevel(context);
        if (batteryLevel != null) {
            deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.BATTERY_LEVEL,
                                       Value.newBuilder()
                                               .setNumberValue(batteryLevel)
                                               .build());
        }
        Boolean isBatterySaverEnabled = io.bidmachine.Utils.isBatterySaverEnabled(context);
        if (isBatterySaverEnabled != null) {
            deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.BATTERY_SAVER,
                                       Value.newBuilder()
                                               .setNumberValue(isBatterySaverEnabled ? 1 : 0)
                                               .build());
        }
        boolean isDarkModeEnabled = io.bidmachine.Utils.isDarkModeEnabled(context);
        deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.DARK_MODE,
                                   Value.newBuilder()
                                           .setNumberValue(isDarkModeEnabled ? 1 : 0)
                                           .build());
        Boolean isAirplaneModeOn = io.bidmachine.Utils.isAirplaneModeOn(context);
        if (isAirplaneModeOn != null) {
            deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.AIR_PLANE,
                                       Value.newBuilder()
                                               .setNumberValue(isAirplaneModeOn ? 1 : 0)
                                               .build());
        }
        Boolean isDoNotDisturbOn = io.bidmachine.Utils.isDoNotDisturbOn(context);
        if (isDoNotDisturbOn != null) {
            deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.DO_NOT_DISTURB,
                                       Value.newBuilder()
                                               .setNumberValue(isDoNotDisturbOn ? 1 : 0)
                                               .build());
        }
        String deviceName = io.bidmachine.Utils.getDeviceName(context);
        if (deviceName != null) {
            deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.DEVICE_NAME,
                                       Value.newBuilder()
                                               .setStringValue(deviceName)
                                               .build());
        }
        deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.TIME,
                                   Value.newBuilder()
                                           .setNumberValue(System.currentTimeMillis())
                                           .build());
        Float screenBrightnessRatio = io.bidmachine.Utils.getScreenBrightnessRatio(context);
        if (screenBrightnessRatio != null) {
            deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.SCREEN_BRIGHT,
                                       Value.newBuilder()
                                               .setNumberValue(screenBrightnessRatio)
                                               .build());
        }
        deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.JAIL_BREAK,
                                   Value.newBuilder()
                                           .setNumberValue(deviceInfo.isDeviceRooted() ? 1 : 0)
                                           .build());
        deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.BOOT,
                                   Value.newBuilder()
                                           .setNumberValue(SystemClock.elapsedRealtime())
                                           .build());
        Set<String> bluetoothConnectedHeadsets = BluetoothUtils.getConnectedHeadsets();
        if (bluetoothConnectedHeadsets != null && bluetoothConnectedHeadsets.size() > 0) {
            String headset = bluetoothConnectedHeadsets.iterator().next();
            deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.HEADSET_NAME,
                                       Value.newBuilder()
                                               .setStringValue(headset)
                                               .build());
        }
        Long totalRAMInB = deviceInfo.getTotalRAMInB();
        if (totalRAMInB != null) {
            deviceExtBuilder.putFields(ProtoExtConstants.Context.Device.TOTAL_MEMORY,
                                       Value.newBuilder()
                                               .setNumberValue(totalRAMInB)
                                               .build());
        }
    }

    @Override
    public void merge(@NonNull DeviceParams instance) {
        // ignore
    }

}
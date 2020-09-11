package io.bidmachine.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.bidmachine.core.Utils;

@SuppressLint("MissingPermission")
public class BluetoothUtils {

    private static final int[] profileArray = new int[]{
            BluetoothProfile.HEADSET
//            BluetoothProfile.A2DP,
//            BluetoothProfile.HEALTH
    };
    private static final Map<Integer, BluetoothProfile> proxyMap = new HashMap<>(profileArray.length);
    private static final BluetoothProfile.ServiceListener listener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            proxyMap.put(profile, proxy);
        }

        @Override
        public void onServiceDisconnected(int profile) {

        }
    };

    private static boolean isRegistered = false;

    public static void register(@Nullable Context context) {
        if (context == null || context.getApplicationContext() == null) {
            return;
        }
        if (isRegistered) {
            return;
        }
        if (!Utils.isPermissionGranted(context, Manifest.permission.BLUETOOTH)) {
            return;
        }
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                return;
            }
            for (int profile : profileArray) {
                bluetoothAdapter.getProfileProxy(context.getApplicationContext(),
                                                 listener,
                                                 profile);
            }
        } catch (Exception ignore) {
        }
        isRegistered = true;
    }

    @Nullable
    public static Boolean isHeadsetConnected(@NonNull Context context) {
        if (!Utils.isPermissionGranted(context, Manifest.permission.BLUETOOTH)) {
            return null;
        }
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            int connectionState = bluetoothAdapter != null
                    ? bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET)
                    : -1;
            return connectionState == BluetoothProfile.STATE_CONNECTED;
        } catch (Exception ignore) {
        }
        return null;
    }

    @Nullable
    public static Set<String> getConnectedHeadsets(@NonNull Context context) {
        return getConnectedDevices(context, BluetoothProfile.HEADSET);
    }

    @Nullable
    private static Set<String> getConnectedDevices(@NonNull Context context,
                                                   @Nullable Integer profile) {
        if (!Utils.isPermissionGranted(context, Manifest.permission.BLUETOOTH)) {
            return null;
        }
        Set<String> connectedDeviceSet = null;
        try {
            final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                return null;
            }
            connectedDeviceSet = new HashSet<>();
            for (Map.Entry<Integer, BluetoothProfile> entry : proxyMap.entrySet()) {
                if (profile == null || entry.getKey().equals(profile)) {
                    BluetoothProfile bluetoothProfile = entry.getValue();
                    if (bluetoothProfile != null) {
                        for (BluetoothDevice bluetoothDevice : bluetoothProfile.getConnectedDevices()) {
                            connectedDeviceSet.add(bluetoothDevice.getName());
                        }
                    }
                }
            }
        } catch (Exception ignore) {
        }
        return connectedDeviceSet;
    }

}

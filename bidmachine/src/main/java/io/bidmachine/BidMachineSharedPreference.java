package io.bidmachine;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

import io.bidmachine.protobuf.InitResponse;

class BidMachineSharedPreference {

    private static final String NAME = "BidMachinePref";
    private static final String KEY_INIT_DATA = "initData";
    private static final String KEY_BM_IFV = "bid_machine_ifv";

    @NonNull
    static String obtainIFV(@NonNull Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        String ifv;
        try {
            ifv = preferences.getString(KEY_BM_IFV, null);
        } catch (Exception e) {
            ifv = null;
        }
        if (!TextUtils.isEmpty(ifv)) {
            return ifv;
        }
        ifv = UUID.randomUUID().toString();
        preferences.edit()
                .putString(KEY_BM_IFV, ifv)
                .apply();
        return ifv;
    }

    static void storeInitResponse(@NonNull Context context, @NonNull InitResponse response) {
        SharedPreferences preferences = getSharedPreferences(context);
        try {
            String initResponse = Base64.encodeToString(response.toByteArray(), Base64.DEFAULT);
            preferences.edit()
                    .putString(KEY_INIT_DATA, initResponse)
                    .apply();
        } catch (Exception ignore) {
        }
    }

    @Nullable
    static InitResponse getInitResponse(@NonNull Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        if (!preferences.contains(KEY_INIT_DATA)) {
            return null;
        }
        try {
            String initResponse = preferences.getString(KEY_INIT_DATA, null);
            return InitResponse.parseFrom(Base64.decode(initResponse, Base64.DEFAULT));
        } catch (Exception e) {
            preferences.edit().remove(KEY_INIT_DATA).apply();
            return null;
        }
    }

    @NonNull
    private static SharedPreferences getSharedPreferences(@NonNull Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

}
package io.bidmachine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

import io.bidmachine.utils.BMError;

public interface HeaderBiddingCollectParamsCallback {

    void onCollectFinished(@NonNull Map<String, String> params);

    void onCollectFail(@Nullable BMError error);

}

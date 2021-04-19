package io.bidmachine.ads.networks.adcolony;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyReward;
import com.adcolony.sdk.AdColonyRewardListener;

import java.util.ArrayList;
import java.util.List;

public class AdColonyRewardListenerWrapper implements AdColonyRewardListener {

    private static volatile AdColonyRewardListenerWrapper instance;

    static AdColonyRewardListenerWrapper get() {
        if (instance == null) {
            synchronized (AdColonyRewardListenerWrapper.class) {
                if (instance == null) {
                    instance = new AdColonyRewardListenerWrapper();
                    AdColony.setRewardListener(instance);
                }
            }
        }
        return instance;
    }

    private final List<AdColonyFullscreenAdListener> listeners = new ArrayList<>();

    void addListener(@NonNull AdColonyFullscreenAdListener listener) {
        listeners.add(listener);
    }

    void removeListener(@NonNull AdColonyFullscreenAdListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onReward(AdColonyReward adColonyReward) {
        for (AdColonyFullscreenAdListener listener : listeners) {
            if (adColonyReward == null
                    || TextUtils.equals(adColonyReward.getZoneID(), listener.getZoneId())) {
                listener.onReward(adColonyReward);
            }
        }
    }

}

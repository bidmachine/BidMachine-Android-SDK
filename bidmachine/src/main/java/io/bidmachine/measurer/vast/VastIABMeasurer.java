package io.bidmachine.measurer.vast;

import android.content.Context;
import android.support.annotation.FloatRange;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.iab.omid.library.appodeal.adsession.AdSession;
import com.iab.omid.library.appodeal.adsession.AdSessionConfiguration;
import com.iab.omid.library.appodeal.adsession.AdSessionContext;
import com.iab.omid.library.appodeal.adsession.CreativeType;
import com.iab.omid.library.appodeal.adsession.ImpressionType;
import com.iab.omid.library.appodeal.adsession.Owner;
import com.iab.omid.library.appodeal.adsession.Partner;
import com.iab.omid.library.appodeal.adsession.VerificationScriptResource;
import com.iab.omid.library.appodeal.adsession.media.InteractionType;
import com.iab.omid.library.appodeal.adsession.media.MediaEvents;
import com.iab.omid.library.appodeal.adsession.media.Position;
import com.iab.omid.library.appodeal.adsession.media.VastProperties;

import java.util.ArrayList;
import java.util.List;

import io.bidmachine.measurer.BMIABMeasurer;

public class VastIABMeasurer extends BMIABMeasurer {

    private float skipOffset;
    private boolean isAutoPlay;
    private final List<VerificationScriptResource> verificationScriptResourceList = new ArrayList<>();

    private MediaEvents mediaEvents;

    public void setSkipOffset(float skipOffset) {
        this.skipOffset = skipOffset;
    }

    public void setAutoPlay(boolean autoPlay) {
        isAutoPlay = autoPlay;
    }

    public void addVerificationScriptResource(@Nullable VerificationScriptResource verificationScriptResource) {
        if (verificationScriptResource != null) {
            verificationScriptResourceList.add(verificationScriptResource);
        }
    }

    public void addVerificationScriptResources(@Nullable List<VerificationScriptResource> verificationScriptResourceList) {
        if (verificationScriptResourceList != null) {
            for (VerificationScriptResource verificationScriptResource : verificationScriptResourceList) {
                addVerificationScriptResource(verificationScriptResource);
            }
        }
    }

    @Override
    @MainThread
    public AdSessionConfiguration createAdSessionConfiguration() throws Exception {
        return AdSessionConfiguration.createAdSessionConfiguration(CreativeType.VIDEO,
                                                                   ImpressionType.VIEWABLE,
                                                                   Owner.NATIVE,
                                                                   Owner.NATIVE,
                                                                   false);
    }

    @Override
    @MainThread
    public AdSessionContext createAdSessionContext(@NonNull Partner partner,
                                                   @NonNull View view) throws Exception {
        if (TextUtils.isEmpty(measurerJs)) {
            return null;
        }
        return AdSessionContext.createNativeAdSessionContext(
                partner,
                measurerJs,
                verificationScriptResourceList,
                "");
    }

    @Override
    @MainThread
    public boolean configure(@NonNull Context context, @NonNull View view) {
        boolean result = super.configure(context, view);
        if (result) {
            try {
                AdSession adSession = getAdSession();
                if (adSession == null) {
                    return false;
                }
                mediaEvents = MediaEvents.createMediaEvents(adSession);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return result;
    }

    @Override
    @MainThread
    public void loaded() {
        try {
            VastProperties vastProperties;
            if (skipOffset == -1) {
                vastProperties = VastProperties
                        .createVastPropertiesForNonSkippableMedia(isAutoPlay,
                                                                  Position.STANDALONE);
            } else {
                vastProperties = VastProperties
                        .createVastPropertiesForSkippableMedia(skipOffset,
                                                               isAutoPlay,
                                                               Position.STANDALONE);
            }
            if (mediaEvents != null) {
                mediaEvents.loaded(vastProperties);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @MainThread
    public void clicked() {
        try {
            if (mediaEvents != null) {
                mediaEvents.adUserInteraction(InteractionType.CLICK);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MainThread
    public void videoStarted(float duration, @FloatRange(from = 0, to = 1) float volume) {
        try {
            if (mediaEvents != null) {
                mediaEvents.start(duration, volume);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MainThread
    public void videoFirstQuartile() {
        try {
            if (mediaEvents != null) {
                mediaEvents.firstQuartile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MainThread
    public void videoMidpoint() {
        try {
            if (mediaEvents != null) {
                mediaEvents.midpoint();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MainThread
    public void videoThirdQuartile() {
        try {
            if (mediaEvents != null) {
                mediaEvents.thirdQuartile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MainThread
    public void videoCompleted() {
        try {
            if (mediaEvents != null) {
                mediaEvents.complete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MainThread
    public void videoPaused() {
        try {
            if (mediaEvents != null) {
                mediaEvents.pause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MainThread
    public void videoResumed() {
        try {
            if (mediaEvents != null) {
                mediaEvents.resume();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MainThread
    public void videoVolumeChanged(@FloatRange(from = 0, to = 1) float volume) {
        try {
            if (mediaEvents != null) {
                mediaEvents.volumeChange(volume);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MainThread
    public void videoSkipped() {
        try {
            if (mediaEvents != null) {
                mediaEvents.skipped();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
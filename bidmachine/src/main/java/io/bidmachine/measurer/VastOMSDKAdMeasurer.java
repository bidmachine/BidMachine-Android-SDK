package io.bidmachine.measurer;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.explorestack.iab.measurer.VastAdMeasurer;
import com.explorestack.iab.vast.VastPlaybackListener;
import com.explorestack.iab.vast.tags.AdVerificationsExtensionTag;
import com.explorestack.iab.vast.tags.JavaScriptResourceTag;
import com.explorestack.iab.vast.tags.VerificationTag;
import com.iab.omid.library.appodeal.adsession.AdEvents;
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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;

public class VastOMSDKAdMeasurer extends OMSDKAdMeasurer<View> implements VastAdMeasurer, VastPlaybackListener {

    private static final boolean IS_AUTO_PLAY = true;

    private final List<VerificationScriptResource> resourceList = new ArrayList<>();
    private float skipOffset = -1;

    private MediaEvents mediaEvents;

    public void addVerificationScriptResourceList(@Nullable final List<AdVerificationsExtensionTag> adVerificationsExtensionTagList) {
        if (adVerificationsExtensionTagList == null) {
            return;
        }
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (AdVerificationsExtensionTag adVerificationsExtensionTag : adVerificationsExtensionTagList) {
                        if (adVerificationsExtensionTag == null) {
                            continue;
                        }
                        for (VerificationTag verificationTag : adVerificationsExtensionTag.getVerificationTagList()) {
                            JavaScriptResourceTag javaScriptResourceTag = verificationTag.getJavaScriptResourceTag();
                            String resourceUrl = javaScriptResourceTag != null
                                    ? javaScriptResourceTag.getText()
                                    : null;
                            String vendorKey = verificationTag.getVendor();
                            String params = verificationTag.getVerificationParameters();
                            if (!TextUtils.isEmpty(resourceUrl)) {
                                assert resourceUrl != null;
                                addVerificationScriptResource(resourceUrl, vendorKey, params);
                            }
                        }
                    }
                } catch (Throwable t) {
                    Logger.log(t);
                }
            }
        });
    }

    public void setSkipOffset(float skipOffset) {
        this.skipOffset = skipOffset;
    }

    @UiThread
    public void addVerificationScriptResource(@NonNull String resourceUrl,
                                              @Nullable String vendorKey,
                                              @Nullable String params) {
        try {
            URL url = new URL(resourceUrl);
            VerificationScriptResource resource;
            if (!TextUtils.isEmpty(vendorKey) && !TextUtils.isEmpty(params)) {
                resource = VerificationScriptResource
                        .createVerificationScriptResourceWithParameters(vendorKey, url, params);
            } else {
                resource = VerificationScriptResource
                        .createVerificationScriptResourceWithoutParameters(url);
            }
            resourceList.add(resource);
        } catch (Throwable t) {
            Logger.log(t);
        }
    }

    @Override
    public void onAdViewReady(@NonNull View view) {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Partner partner = OMSDKSettings.getPartner();
                    if (partner == null || isSessionPrepared()) {
                        return;
                    }
                    AdSessionConfiguration adSessionConfiguration = AdSessionConfiguration
                            .createAdSessionConfiguration(CreativeType.VIDEO,
                                                          ImpressionType.BEGIN_TO_RENDER,
                                                          Owner.NATIVE,
                                                          Owner.NATIVE,
                                                          false);
                    AdSessionContext adSessionContext = AdSessionContext
                            .createNativeAdSessionContext(partner,
                                                          OMSDKSettings.OM_JS,
                                                          resourceList,
                                                          OMSDKSettings.AD_SESSION_CONTEXT_CONTENT_URL,
                                                          OMSDKSettings.AD_SESSION_CONTEXT_CUSTOM_REFERENCE_DATA);
                    AdSession adSession = AdSession.createAdSession(adSessionConfiguration,
                                                                    adSessionContext);
                    mediaEvents = MediaEvents.createMediaEvents(adSession);
                    prepareAdSession(adSession);
                } catch (Throwable t) {
                    Logger.log(t);
                }
            }
        });
    }

    public void onAdClicked() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mediaEvents != null) {
                        mediaEvents.adUserInteraction(InteractionType.CLICK);
                    }

                    log("onAdClicked");
                } catch (Throwable t) {
                    Logger.log(t);
                }
            }
        });
    }

    @UiThread
    @Override
    protected void onAdLoaded(@NonNull AdEvents adEvents) throws Throwable {
        VastProperties vastProperties;
        if (VastOMSDKAdMeasurer.this.skipOffset == -1) {
            vastProperties = VastProperties
                    .createVastPropertiesForNonSkippableMedia(IS_AUTO_PLAY,
                                                              Position.STANDALONE);
        } else {
            vastProperties = VastProperties
                    .createVastPropertiesForSkippableMedia(VastOMSDKAdMeasurer.this.skipOffset,
                                                           IS_AUTO_PLAY,
                                                           Position.STANDALONE);
        }
        adEvents.loaded(vastProperties);

        log("onAdLoaded");
    }

    @Override
    public void onVideoStarted(final float duration,
                               @FloatRange(from = 0, to = 1) final float volume) {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mediaEvents != null) {
                        mediaEvents.start(duration, volume);
                    }

                    log("onVideoStarted");
                } catch (Throwable t) {
                    Logger.log(t);
                }
            }
        });
    }

    @Override
    public void onVideoFirstQuartile() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mediaEvents != null) {
                        mediaEvents.firstQuartile();
                    }

                    log("onVideoFirstQuartile");
                } catch (Throwable t) {
                    Logger.log(t);
                }
            }
        });
    }

    @Override
    public void onVideoMidpoint() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mediaEvents != null) {
                        mediaEvents.midpoint();
                    }

                    log("onVideoMidpoint");
                } catch (Throwable t) {
                    Logger.log(t);
                }
            }
        });
    }

    @Override
    public void onVideoThirdQuartile() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mediaEvents != null) {
                        mediaEvents.thirdQuartile();
                    }

                    log("onVideoThirdQuartile");
                } catch (Throwable t) {
                    Logger.log(t);
                }
            }
        });
    }

    @Override
    public void onVideoCompleted() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mediaEvents != null) {
                        mediaEvents.complete();
                    }

                    log("onVideoCompleted");
                } catch (Throwable t) {
                    Logger.log(t);
                }
            }
        });
    }

    @Override
    public void onVideoPaused() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mediaEvents != null) {
                        mediaEvents.pause();
                    }

                    log("onVideoPaused");
                } catch (Throwable t) {
                    Logger.log(t);
                }
            }
        });
    }

    @Override
    public void onVideoResumed() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mediaEvents != null) {
                        mediaEvents.resume();
                    }

                    log("onVideoResumed");
                } catch (Throwable t) {
                    Logger.log(t);
                }
            }
        });
    }

    @Override
    public void onVideoVolumeChanged(@FloatRange(from = 0, to = 1) final float volume) {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mediaEvents != null) {
                        mediaEvents.volumeChange(volume);
                    }

                    log("onVideoVolumeChanged");
                } catch (Throwable t) {
                    Logger.log(t);
                }
            }
        });
    }

    @Override
    public void onVideoSkipped() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mediaEvents != null) {
                        mediaEvents.skipped();
                    }

                    log("onVideoSkipped");
                } catch (Throwable t) {
                    Logger.log(t);
                }
            }
        });
    }

    @Override
    public void destroy() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                mediaEvents = null;
            }
        });
        super.destroy();
    }

}
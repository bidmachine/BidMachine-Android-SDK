package io.bidmachine.measurer;

import android.os.Handler;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.explorestack.iab.measurer.MraidAdMeasurer;
import com.iab.omid.library.appodeal.ScriptInjector;
import com.iab.omid.library.appodeal.adsession.AdEvents;
import com.iab.omid.library.appodeal.adsession.AdSession;
import com.iab.omid.library.appodeal.adsession.AdSessionConfiguration;
import com.iab.omid.library.appodeal.adsession.AdSessionContext;
import com.iab.omid.library.appodeal.adsession.CreativeType;
import com.iab.omid.library.appodeal.adsession.ImpressionType;
import com.iab.omid.library.appodeal.adsession.Owner;

import io.bidmachine.core.Logger;
import io.bidmachine.core.Utils;

@UiThread
public class MraidOMSDKAdMeasurer extends OMSDKAdMeasurer<WebView> implements MraidAdMeasurer {

    private static final long DESTROY_DELAY = 1000;

    @UiThread
    public String injectMeasurerJS(@NonNull String baseCreative) {
        try {
            return ScriptInjector.injectScriptContentIntoHtml(OMSDKSettings.OM_JS, baseCreative);
        } catch (Throwable t) {
            Logger.log(t);
        }
        return baseCreative;
    }

    @Override
    public void onAdViewReady(@NonNull final WebView webView) {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isSessionPrepared()) {
                        return;
                    }
                    AdSessionConfiguration adSessionConfiguration = AdSessionConfiguration
                            .createAdSessionConfiguration(CreativeType.HTML_DISPLAY,
                                                          ImpressionType.BEGIN_TO_RENDER,
                                                          Owner.NATIVE,
                                                          Owner.NONE,
                                                          false);
                    AdSessionContext adSessionContext = AdSessionContext
                            .createHtmlAdSessionContext(OMSDKSettings.getPartner(),
                                                        webView,
                                                        OMSDKSettings.AD_SESSION_CONTEXT_CONTENT_URL,
                                                        OMSDKSettings.AD_SESSION_CONTEXT_CUSTOM_REFERENCE_DATA);
                    AdSession adSession = AdSession.createAdSession(adSessionConfiguration,
                                                                    adSessionContext);
                    prepareAdSession(adSession);
                } catch (Throwable t) {
                    Logger.log(t);
                }
            }
        });
    }

    @UiThread
    @Override
    protected void onAdLoaded(@NonNull AdEvents adEvents) throws Throwable {
        adEvents.loaded();

        log("onAdLoaded");
    }

    public void destroy(@Nullable final Runnable postBack) {
        final Handler handler = new Handler();
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                destroy();
                if (postBack != null) {
                    handler.postDelayed(postBack, DESTROY_DELAY);
                }
            }
        });
    }

}
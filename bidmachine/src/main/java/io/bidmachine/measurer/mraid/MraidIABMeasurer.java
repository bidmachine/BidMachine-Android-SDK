package io.bidmachine.measurer.mraid;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;

import com.explorestack.iab.measurer.MraidWrapperListener;
import com.explorestack.iab.mraid.MRAIDView;
import com.explorestack.iab.mraid.MRAIDViewLifecycleListener;
import com.explorestack.iab.utils.Utils;
import com.iab.omid.library.appodeal.ScriptInjector;
import com.iab.omid.library.appodeal.adsession.AdEvents;
import com.iab.omid.library.appodeal.adsession.AdSessionConfiguration;
import com.iab.omid.library.appodeal.adsession.AdSessionContext;
import com.iab.omid.library.appodeal.adsession.CreativeType;
import com.iab.omid.library.appodeal.adsession.ImpressionType;
import com.iab.omid.library.appodeal.adsession.Owner;
import com.iab.omid.library.appodeal.adsession.Partner;

import io.bidmachine.measurer.BMIABMeasurer;

public class MraidIABMeasurer extends BMIABMeasurer {

    private final static int DESTROY_DELAY_MS = 1000;
    private WebView webView;

    public MRAIDView.builder createMraidViewBuilder(@NonNull Context context,
                                                    @NonNull String adm,
                                                    int width,
                                                    int height,
                                                    @Nullable MRAIDViewLifecycleListener lifecycleListener) {
        MRAIDView.builder builder = new MRAIDView.builder(
                context,
                transformCreative(adm),
                width,
                height);
        builder.setLifecycleListener(new MraidWrapperListener(this, lifecycleListener));
        return builder;
    }

    @Override
    public String transformCreative(String originalCreative) {
        if (!isInitialized()
                || TextUtils.isEmpty(originalCreative)
                || TextUtils.isEmpty(measurerJs)) {
            return super.transformCreative(originalCreative);
        }
        try {
            return ScriptInjector.injectScriptContentIntoHtml(measurerJs, originalCreative);
        } catch (Exception ignored) {

        }
        return super.transformCreative(originalCreative);
    }

    @Override
    @MainThread
    public AdSessionConfiguration createAdSessionConfiguration() throws Exception {
        return AdSessionConfiguration.createAdSessionConfiguration(CreativeType.HTML_DISPLAY,
                                                                   ImpressionType.BEGIN_TO_RENDER,
                                                                   Owner.NATIVE,
                                                                   Owner.NONE,
                                                                   true);
    }

    @Override
    @MainThread
    public AdSessionContext createAdSessionContext(@NonNull Partner partner,
                                                   @NonNull View view) throws Exception {
        WebView webView;
        if (view instanceof WebView) {
            webView = (WebView) view;
        } else {
            return null;
        }
        this.webView = webView;
        return AdSessionContext.createHtmlAdSessionContext(partner, webView, "");
    }

    @Override
    @MainThread
    public void loaded() {
        try {
            AdEvents adEvents = getAdEvents();
            if (adEvents != null) {
                adEvents.loaded();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        Utils.onUiThread(new Runnable() {
            @Override
            public void run() {
                MraidIABMeasurer.super.destroy();
                webView = null;
            }
        }, DESTROY_DELAY_MS);
    }
}
package io.bidmachine.measurer.vast;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.explorestack.iab.vast.tags.AdVerificationsExtensionTag;
import com.explorestack.iab.vast.tags.JavaScriptResourceTag;
import com.explorestack.iab.vast.tags.VerificationTag;
import com.iab.omid.library.appodeal.adsession.VerificationScriptResource;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class VerificationResource {

    @NonNull
    static List<VerificationScriptResource> transformToList(@Nullable AdVerificationsExtensionTag adVerificationsExtension) {
        List<VerificationScriptResource> verificationResourceList = new ArrayList<>();
        if (adVerificationsExtension == null) {
            return verificationResourceList;
        }
        List<VerificationTag> verificationTagList = adVerificationsExtension.getVerificationTagList();
        for (VerificationTag verificationTag : verificationTagList) {
            try {
                VerificationScriptResource verificationScriptResource =
                        createVerificationScriptResource(verificationTag);
                if (verificationScriptResource == null) {
                    continue;
                }
                verificationResourceList.add(verificationScriptResource);
            } catch (Exception ignore) {

            }
        }

        return verificationResourceList;
    }

    @Nullable
    private static VerificationScriptResource createVerificationScriptResource(@Nullable VerificationTag verificationTag) throws Exception {
        if (verificationTag == null) {
            return null;
        }
        URL url = findUrl(verificationTag);
        if (url == null) {
            return null;
        }
        String vendor = verificationTag.getVendor();
        String parameters = verificationTag.getVerificationParameters();
        if (TextUtils.isEmpty(vendor)) {
            return VerificationScriptResource
                    .createVerificationScriptResourceWithoutParameters(url);
        }
        if (TextUtils.isEmpty(parameters)) {
            return VerificationScriptResource
                    .createVerificationScriptResourceWithoutParameters(vendor, url);
        }
        return VerificationScriptResource
                .createVerificationScriptResourceWithParameters(vendor, url, parameters);
    }

    private static URL findUrl(@NonNull VerificationTag verificationTag) {
        JavaScriptResourceTag javaScriptResourceTag = verificationTag.getJavaScriptResourceTag();
        if (javaScriptResourceTag != null) {
            String text = javaScriptResourceTag.getText();
            if (TextUtils.isEmpty(text)) {
                return null;
            }
            try {
                return new URL(text);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

}
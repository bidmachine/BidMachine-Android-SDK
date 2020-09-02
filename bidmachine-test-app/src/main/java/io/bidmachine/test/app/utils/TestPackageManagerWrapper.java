package io.bidmachine.test.app.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import io.bidmachine.test.app.ParamsHelper;

public class TestPackageManagerWrapper extends PackageManagerWrapper {

    private Context baseContext;

    public TestPackageManagerWrapper(Context baseContext, @NonNull PackageManager base) {
        super(base);
        this.baseContext = baseContext;
    }

    @Override
    public PackageInfo getPackageInfo(String packageName, int flags) throws PackageManager.NameNotFoundException {
        String appPackage = ParamsHelper.getInstance(baseContext, ParamsHelper.AdsType.Global).getAppBundle();
        PackageInfo info;
        if (packageName.equals(appPackage)) {
            info = super.getPackageInfo(baseContext.getPackageName(), flags);
        } else {
            info = super.getPackageInfo(packageName, flags);
        }
        String appVersion = ParamsHelper.getInstance(baseContext, ParamsHelper.AdsType.Global).getAppVersion();
        if (appVersion != null) {
            info.versionName = appVersion;
        }
        return info;
    }

    @Override
    public CharSequence getApplicationLabel(ApplicationInfo info) {
        String appName = ParamsHelper.getInstance(baseContext, ParamsHelper.AdsType.Global).getAppName();
        return appName != null ? appName : super.getApplicationLabel(info);
    }

}

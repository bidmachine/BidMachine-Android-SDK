package io.bidmachine.test.app.utils;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ChangedPackages;
import android.content.pm.FeatureInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.SharedLibraryInfo;
import android.content.pm.VersionedPackage;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PackageManagerWrapper extends PackageManager {

    @NotNull
    private final PackageManager base;

    public PackageManagerWrapper(@NonNull PackageManager base) {
        this.base = base;
    }

    @Override
    public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
        return base.getPackageInfo(packageName, flags);
    }

    @SuppressLint("NewApi")
    @Override
    public PackageInfo getPackageInfo(VersionedPackage versionedPackage, int flags) throws NameNotFoundException {
        return base.getPackageInfo(versionedPackage, flags);
    }

    @SuppressLint("NewApi")
    @Override
    public String[] currentToCanonicalPackageNames(String[] names) {
        return base.currentToCanonicalPackageNames(names);
    }

    @SuppressLint("NewApi")
    @Override
    public String[] canonicalToCurrentPackageNames(String[] names) {
        return base.canonicalToCurrentPackageNames(names);
    }

    @SuppressLint("NewApi")
    @Nullable
    @Override
    public Intent getLaunchIntentForPackage(@NonNull String packageName) {
        return base.getLaunchIntentForPackage(packageName);
    }

    @SuppressLint("NewApi")
    @Nullable
    @Override
    public Intent getLeanbackLaunchIntentForPackage(@NonNull String packageName) {
        return base.getLeanbackLaunchIntentForPackage(packageName);
    }

    @Override
    public int[] getPackageGids(@NonNull String packageName) throws NameNotFoundException {
        return base.getPackageGids(packageName);
    }

    @SuppressLint("NewApi")
    @Override
    public int[] getPackageGids(String packageName, int flags) throws NameNotFoundException {
        return base.getPackageGids(packageName, flags);
    }

    @SuppressLint("NewApi")
    @Override
    public int getPackageUid(String packageName, int flags) throws NameNotFoundException {
        return base.getPackageUid(packageName, flags);
    }

    @Override
    public PermissionInfo getPermissionInfo(String name, int flags) throws NameNotFoundException {
        return base.getPermissionInfo(name, flags);
    }

    @Override
    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws NameNotFoundException {
        return base.queryPermissionsByGroup(group, flags);
    }

    @Override
    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws NameNotFoundException {
        return base.getPermissionGroupInfo(name, flags);
    }

    @Override
    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
        return base.getAllPermissionGroups(flags);
    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {
        return base.getApplicationInfo(packageName, flags);
    }

    @Override
    public ActivityInfo getActivityInfo(ComponentName component, int flags) throws NameNotFoundException {
        return base.getActivityInfo(component, flags);
    }

    @Override
    public ActivityInfo getReceiverInfo(ComponentName component, int flags) throws NameNotFoundException {
        return base.getReceiverInfo(component, flags);
    }

    @Override
    public ServiceInfo getServiceInfo(ComponentName component, int flags) throws NameNotFoundException {
        return base.getServiceInfo(component, flags);
    }

    @SuppressLint("NewApi")
    @Override
    public ProviderInfo getProviderInfo(ComponentName component, int flags) throws NameNotFoundException {
        return base.getProviderInfo(component, flags);
    }

    @Override
    public List<PackageInfo> getInstalledPackages(int flags) {
        return base.getInstalledPackages(flags);
    }

    @SuppressLint("NewApi")
    @Override
    public List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags) {
        return base.getPackagesHoldingPermissions(permissions, flags);
    }

    @Override
    public int checkPermission(String permName, String pkgName) {
        return base.checkPermission(permName, pkgName);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean isPermissionRevokedByPolicy(@NonNull String permName, @NonNull String pkgName) {
        return base.isPermissionRevokedByPolicy(permName, pkgName);
    }

    @Override
    public boolean addPermission(PermissionInfo info) {
        return base.addPermission(info);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean addPermissionAsync(PermissionInfo info) {
        return base.addPermissionAsync(info);
    }

    @Override
    public void removePermission(String name) {
        base.removePermission(name);
    }

    @Override
    public int checkSignatures(String pkg1, String pkg2) {
        return base.checkSignatures(pkg1, pkg2);
    }

    @SuppressLint("NewApi")
    @Override
    public int checkSignatures(int uid1, int uid2) {
        return base.checkSignatures(uid1, uid2);
    }

    @Nullable
    @Override
    public String[] getPackagesForUid(int uid) {
        return base.getPackagesForUid(uid);
    }

    @Nullable
    @Override
    public String getNameForUid(int uid) {
        return base.getNameForUid(uid);
    }

    @Override
    public List<ApplicationInfo> getInstalledApplications(int flags) {
        return base.getInstalledApplications(flags);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean isInstantApp() {
        return base.isInstantApp();
    }

    @SuppressLint("NewApi")
    @Override
    public boolean isInstantApp(String packageName) {
        return base.isInstantApp(packageName);
    }

    @SuppressLint("NewApi")
    @Override
    public int getInstantAppCookieMaxBytes() {
        return base.getInstantAppCookieMaxBytes();
    }

    @SuppressLint("NewApi")
    @NonNull
    @Override
    public byte[] getInstantAppCookie() {
        return base.getInstantAppCookie();
    }

    @SuppressLint("NewApi")
    @Override
    public void clearInstantAppCookie() {
        base.clearInstantAppCookie();
    }

    @SuppressLint("NewApi")
    @Override
    public void updateInstantAppCookie(@Nullable byte[] cookie) {
        base.updateInstantAppCookie(cookie);
    }

    @SuppressLint("NewApi")
    @Override
    public String[] getSystemSharedLibraryNames() {
        return base.getSystemSharedLibraryNames();
    }

    @SuppressLint("NewApi")
    @NonNull
    @Override
    public List<SharedLibraryInfo> getSharedLibraries(int flags) {
        return base.getSharedLibraries(flags);
    }

    @SuppressLint("NewApi")
    @Nullable
    @Override
    public ChangedPackages getChangedPackages(int sequenceNumber) {
        return base.getChangedPackages(sequenceNumber);
    }

    @SuppressLint("NewApi")
    @Override
    public FeatureInfo[] getSystemAvailableFeatures() {
        return base.getSystemAvailableFeatures();
    }

    @SuppressLint("NewApi")
    @Override
    public boolean hasSystemFeature(String name) {
        return base.hasSystemFeature(name);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean hasSystemFeature(String name, int version) {
        return base.hasSystemFeature(name, version);
    }

    @Override
    public ResolveInfo resolveActivity(Intent intent, int flags) {
        return base.resolveActivity(intent, flags);
    }

    @Override
    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        return base.queryIntentActivities(intent, flags);
    }

    @Override
    public List<ResolveInfo> queryIntentActivityOptions(@Nullable ComponentName caller, @Nullable Intent[] specifics, Intent intent, int flags) {
        return base.queryIntentActivityOptions(caller, specifics, intent, flags);
    }

    @Override
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
        return base.queryBroadcastReceivers(intent, flags);
    }

    @Override
    public ResolveInfo resolveService(Intent intent, int flags) {
        return base.resolveService(intent, flags);
    }

    @Override
    public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
        return base.queryIntentServices(intent, flags);
    }

    @SuppressLint("NewApi")
    @Override
    public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
        return base.queryIntentContentProviders(intent, flags);
    }

    @Override
    public ProviderInfo resolveContentProvider(String name, int flags) {
        return base.resolveContentProvider(name, flags);
    }

    @Override
    public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
        return base.queryContentProviders(processName, uid, flags);
    }

    @Override
    public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws NameNotFoundException {
        return base.getInstrumentationInfo(className, flags);
    }

    @Override
    public List<InstrumentationInfo> queryInstrumentation(String targetPackage, int flags) {
        return base.queryInstrumentation(targetPackage, flags);
    }

    @Override
    public Drawable getDrawable(String packageName, int resid, ApplicationInfo appInfo) {
        return base.getDrawable(packageName, resid, appInfo);
    }

    @Override
    public Drawable getActivityIcon(ComponentName activityName) throws NameNotFoundException {
        return base.getActivityIcon(activityName);
    }

    @Override
    public Drawable getActivityIcon(Intent intent) throws NameNotFoundException {
        return base.getActivityIcon(intent);
    }

    @SuppressLint("NewApi")
    @Override
    public Drawable getActivityBanner(ComponentName activityName) throws NameNotFoundException {
        return base.getActivityBanner(activityName);
    }

    @SuppressLint("NewApi")
    @Override
    public Drawable getActivityBanner(Intent intent) throws NameNotFoundException {
        return base.getActivityBanner(intent);
    }

    @Override
    public Drawable getDefaultActivityIcon() {
        return base.getDefaultActivityIcon();
    }

    @Override
    public Drawable getApplicationIcon(ApplicationInfo info) {
        return base.getApplicationIcon(info);
    }

    @Override
    public Drawable getApplicationIcon(String packageName) throws NameNotFoundException {
        return base.getApplicationIcon(packageName);
    }

    @SuppressLint("NewApi")
    @Override
    public Drawable getApplicationBanner(ApplicationInfo info) {
        return base.getApplicationBanner(info);
    }

    @SuppressLint("NewApi")
    @Override
    public Drawable getApplicationBanner(String packageName) throws NameNotFoundException {
        return base.getApplicationBanner(packageName);
    }

    @SuppressLint("NewApi")
    @Override
    public Drawable getActivityLogo(ComponentName activityName) throws NameNotFoundException {
        return base.getActivityLogo(activityName);
    }

    @SuppressLint("NewApi")
    @Override
    public Drawable getActivityLogo(Intent intent) throws NameNotFoundException {
        return base.getActivityLogo(intent);
    }

    @SuppressLint("NewApi")
    @Override
    public Drawable getApplicationLogo(ApplicationInfo info) {
        return base.getApplicationLogo(info);
    }

    @SuppressLint("NewApi")
    @Override
    public Drawable getApplicationLogo(String packageName) throws NameNotFoundException {
        return base.getApplicationLogo(packageName);
    }

    @SuppressLint("NewApi")
    @Override
    public Drawable getUserBadgedIcon(Drawable icon, UserHandle user) {
        return base.getUserBadgedIcon(icon, user);
    }

    @SuppressLint("NewApi")
    @Override
    public Drawable getUserBadgedDrawableForDensity(Drawable drawable, UserHandle user, Rect badgeLocation, int badgeDensity) {
        return base.getUserBadgedDrawableForDensity(drawable, user, badgeLocation, badgeDensity);
    }

    @SuppressLint("NewApi")
    @Override
    public CharSequence getUserBadgedLabel(CharSequence label, UserHandle user) {
        return base.getUserBadgedLabel(label, user);
    }

    @Override
    public CharSequence getText(String packageName, int resid, ApplicationInfo appInfo) {
        return base.getText(packageName, resid, appInfo);
    }

    @Override
    public XmlResourceParser getXml(String packageName, int resid, ApplicationInfo appInfo) {
        return base.getXml(packageName, resid, appInfo);
    }

    @Override
    public CharSequence getApplicationLabel(ApplicationInfo info) {
        return base.getApplicationLabel(info);
    }

    @Override
    public Resources getResourcesForActivity(ComponentName activityName) throws NameNotFoundException {
        return base.getResourcesForActivity(activityName);
    }

    @Override
    public Resources getResourcesForApplication(ApplicationInfo app) throws NameNotFoundException {
        return base.getResourcesForApplication(app);
    }

    @Override
    public Resources getResourcesForApplication(String appPackageName) throws NameNotFoundException {
        return base.getResourcesForApplication(appPackageName);
    }

    @SuppressLint("NewApi")
    @Override
    public void verifyPendingInstall(int id, int verificationCode) {
        base.verifyPendingInstall(id, verificationCode);
    }

    @SuppressLint("NewApi")
    @Override
    public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) {
        base.extendVerificationTimeout(id, verificationCodeAtTimeout, millisecondsToDelay);
    }

    @SuppressLint("NewApi")
    @Override
    public void setInstallerPackageName(String targetPackage, String installerPackageName) {
        base.setInstallerPackageName(targetPackage, installerPackageName);
    }

    @SuppressLint("NewApi")
    @Override
    public String getInstallerPackageName(String packageName) {
        return base.getInstallerPackageName(packageName);
    }

    @Override
    public void addPackageToPreferred(String packageName) {
        base.addPackageToPreferred(packageName);
    }

    @Override
    public void removePackageFromPreferred(String packageName) {
        base.removePackageFromPreferred(packageName);
    }

    @Override
    public List<PackageInfo> getPreferredPackages(int flags) {
        return base.getPreferredPackages(flags);
    }

    @Override
    public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
        base.addPreferredActivity(filter, match, set, activity);
    }

    @Override
    public void clearPackagePreferredActivities(String packageName) {
        base.clearPackagePreferredActivities(packageName);
    }

    @Override
    public int getPreferredActivities(@NonNull List<IntentFilter> outFilters, @NonNull List<ComponentName> outActivities, String packageName) {
        return base.getPreferredActivities(outFilters, outActivities, packageName);
    }

    @Override
    public void setComponentEnabledSetting(@NonNull ComponentName componentName, int newState, int flags) {
        base.setComponentEnabledSetting(componentName, newState, flags);
    }

    @Override
    public int getComponentEnabledSetting(ComponentName componentName) {
        return base.getComponentEnabledSetting(componentName);
    }

    @Override
    public void setApplicationEnabledSetting(@NonNull String packageName, int newState, int flags) {
        base.setApplicationEnabledSetting(packageName, newState, flags);
    }

    @Override
    public int getApplicationEnabledSetting(String packageName) {
        return base.getApplicationEnabledSetting(packageName);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean isSafeMode() {
        return base.isSafeMode();
    }

    @SuppressLint("NewApi")
    @Override
    public void setApplicationCategoryHint(@NonNull String packageName, int categoryHint) {
        base.setApplicationCategoryHint(packageName, categoryHint);
    }

    @SuppressLint("NewApi")
    @NonNull
    @Override
    public PackageInstaller getPackageInstaller() {
        return base.getPackageInstaller();
    }

    @SuppressLint("NewApi")
    @Override
    public boolean canRequestPackageInstalls() {
        return base.canRequestPackageInstalls();
    }
}
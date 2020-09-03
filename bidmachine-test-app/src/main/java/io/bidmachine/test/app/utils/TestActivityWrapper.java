package io.bidmachine.test.app.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.bidmachine.test.app.ParamsHelper;

@SuppressLint("Registered")
public class TestActivityWrapper extends Activity {

    private Activity base;

    public TestActivityWrapper(Activity base) {
        this.base = base;
        attachBaseContext(new TestContextWrapper(base));
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return base.getApplicationInfo();
    }

    @Override
    public Context getApplicationContext() {
        return base.getApplicationContext();
    }

    @Override
    public void setContentView(View view) {
        base.setContentView(view);
    }

    @Override
    public void setContentView(int layoutResID) {
        base.setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        base.setContentView(view, params);
    }

    @Override
    public AssetManager getAssets() {
        return base.getAssets();
    }

    @Override
    public Resources getResources() {
        return base.getResources();
    }

    @Override
    public Window getWindow() {
        return base.getWindow();
    }

    @Override
    public WindowManager getWindowManager() {
        return base.getWindowManager();
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        base.setRequestedOrientation(requestedOrientation);
    }

    @Override
    public int getRequestedOrientation() {
        return base.getRequestedOrientation();
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return base.getSharedPreferences(name, mode);
    }

    @Override
    public String getPackageName() {
        String appBundle = ParamsHelper.getInstance(base, ParamsHelper.AdsType.Global).getAppBundle();
        return appBundle != null ? appBundle : super.getPackageName();
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        return base.getSystemService(name);
    }

    @SuppressLint("NewApi")
    @Override
    public String getSystemServiceName(Class<?> serviceClass) {
        return base.getSystemServiceName(serviceClass);
    }

    @Override
    public PackageManager getPackageManager() {
        return new TestPackageManagerWrapper(base, super.getPackageManager());
    }

    @Override
    public Context getBaseContext() {
        return base.getBaseContext();
    }

    @Override
    public ContentResolver getContentResolver() {
        return base.getContentResolver();
    }

    @Override
    public void startActivity(Intent intent) {
        fixReplacedIntent(intent);
        base.startActivity(intent);
    }

    @SuppressLint("NewApi")
    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {
        fixReplacedIntent(intent);
        base.startActivity(intent, options);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        fixReplacedIntent(intent);
        base.startActivityForResult(intent, requestCode);
    }

    @SuppressLint("NewApi")
    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        fixReplacedIntent(intent);
        base.startActivityForResult(intent, requestCode, options);
    }

    private void fixReplacedIntent(Intent intent){
        String appBundle = ParamsHelper.getInstance(base, ParamsHelper.AdsType.Global).getAppBundle();
        if (intent.getComponent() != null && intent.getComponent().getPackageName().equals(appBundle)) {
            intent.setComponent(new ComponentName(base.getPackageName(), intent.getComponent().getClassName()));
        }
    }

}

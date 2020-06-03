package com.ctl.plugintest.hook;

import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.ctl.plugin.lib.HookPluginManager;

import java.io.File;

public class HookApplication extends Application {

    private Resources resources;
    private AssetManager assetManager;

    @Override
    public void onCreate() {
        super.onCreate();

        HookPluginManager.getInstance(this).loadPlugin(getExternalFilesDir(null) + File.separator + "p.apk");
        resources = HookPluginManager.getInstance(this).getResources();
        assetManager = HookPluginManager.getInstance(this).getAssetManager();

    }

    @Override
    public Resources getResources() {
        return resources == null ? super.getResources() : resources;
    }

    @Override
    public AssetManager getAssets() {
        return assetManager == null ? super.getAssets() : assetManager;
    }
}

package com.ctl.plugintest.hook;

import android.app.Application;
import android.content.res.Resources;

import com.ctl.plugin.lib.core.AMSCheckEngine;
import com.ctl.plugin.lib.core.ActivityThreadmHRestore;
import com.ctl.plugin.lib.core.CustomLoadedApkAction;
import com.ctl.plugin.lib.core.DexElementFuse;

import java.io.File;

public class HookApplication extends Application {

    private Resources resources;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            AMSCheckEngine.mHookAMS(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ActivityThreadmHRestore.mActivityThreadmHAction(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DexElementFuse dexElementFuse = new DexElementFuse();
        try {
            dexElementFuse.mainPluginFuse(this, getExternalFilesDir(null) + File.separator + "p.apk");
            resources = dexElementFuse.getResources();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        CustomLoadedApkAction customLoadedApkAction = new CustomLoadedApkAction();
//        try {
//            customLoadedApkAction.action(this, getExternalFilesDir(null) + File.separator + "p.apk");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public Resources getResources() {
        return resources == null ? super.getResources() : resources;
    }

}

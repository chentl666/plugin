package com.ctl.plugin.lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.ctl.plugin.lib.proxy.ProxyActivity;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import dalvik.system.DexClassLoader;

/**
 * created by : chentl
 * Date: 2020/06/02
 */
public class PluginManager {

    public static final String CLASSNAME = "className";
    private final String TAG = getClass().getSimpleName();
    private static PluginManager pluginManager;
    private Context context;
    private DexClassLoader dexClassLoader;
    private Resources resources;
    private String pluginApkPath;

    public static PluginManager getInstance(Context context) {
        if (pluginManager == null) {
            synchronized (PluginManager.class) {
                if (pluginManager == null) {
                    pluginManager = new PluginManager(context);
                }
            }
        }
        return pluginManager;
    }

    public PluginManager(Context context) {
        this.context = context;
    }

    /**
     * 加载插件
     *
     * @param path 插件路径
     */
    public PluginManager loadPlugin(String path) {
        File file = new File(path);
        if (!file.exists()) {
            Log.e(TAG, "插件不存在...");
            return this;
        }
        pluginApkPath = file.getAbsolutePath();
        File pluginDir = context.getDir("pluginDir", Context.MODE_PRIVATE);//dexClassLoader需要一个缓存目录
        dexClassLoader = new DexClassLoader(pluginApkPath, pluginDir.getAbsolutePath(), null, context.getClassLoader());

        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPathMethod = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPathMethod.invoke(assetManager, pluginApkPath);
            resources = new Resources(assetManager, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 打开插件
     */
    public void openPlugin() {
        if (pluginApkPath == null || dexClassLoader == null || resources == null) {
            Log.e(TAG, "插件启动失败...");
            return;
        }
        PackageManager packageManager = context.getPackageManager();
        PackageInfo archiveInfo = packageManager.getPackageArchiveInfo(pluginApkPath, PackageManager.GET_ACTIVITIES);
        ActivityInfo activityInfo = archiveInfo.activities[0];

        Intent intent = new Intent(context, ProxyActivity.class);
        intent.putExtra(CLASSNAME, activityInfo.name);
        context.startActivity(intent);
    }

    public DexClassLoader getDexClassLoader() {
        return dexClassLoader;
    }

    public Resources getResources() {
        return resources;
    }

    /**
     * 注册插件里的静态广播
     */
    public void loadStaticReceiver() {
        if (pluginApkPath == null) {
            Log.e(TAG, "插件不存在...");
            return;
        }
        File file = new File(pluginApkPath);
        if (!file.exists()) {
            Log.e(TAG, "插件不存在...");
            return;
        }
        try {
            Class mPackageParserClass = Class.forName("android.content.pm.PackageParser");
            Object mPackageParser = mPackageParserClass.newInstance();

            //public Package parsePackage(File packageFile, int flags)
            Method parsePackageMethod = mPackageParserClass.getMethod("parsePackage", File.class, int.class);
            Object mPackage = parsePackageMethod.invoke(mPackageParser, file, PackageManager.GET_ACTIVITIES);

            Field receiversField = mPackage.getClass().getDeclaredField("receivers");
            Object receivers = receiversField.get(mPackage);

            ArrayList arrayList = (ArrayList) receivers;

            for (Object mActivity : arrayList) {
                Class<?> mComponentClass = Class.forName("android.content.pm.PackageParser$Component");
                Field intentsField = mComponentClass.getDeclaredField("intents");
                ArrayList<IntentFilter> intents = (ArrayList) intentsField.get(mActivity);

                Class mPackageUserState = Class.forName("android.content.pm.PackageUserState");
                Class<?> mUserHandle = Class.forName("android.os.UserHandle");
                int userId = (int) mUserHandle.getMethod("getCallingUserId").invoke(null);
                Method generateActivityInfoMethod = mPackageParserClass.getMethod("generateActivityInfo", mActivity.getClass(), int.class,
                        mPackageUserState, int.class);
                ActivityInfo activityInfo = (ActivityInfo) generateActivityInfoMethod.invoke(null, mActivity, 0,
                        mPackageUserState.newInstance(), userId);
                String name = activityInfo.name;
                Class<?> aClass = getDexClassLoader().loadClass(name);
                BroadcastReceiver broadcastReceiver = (BroadcastReceiver) aClass.newInstance();

                for (IntentFilter intentFilter : intents) {
                    context.registerReceiver(broadcastReceiver, intentFilter);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

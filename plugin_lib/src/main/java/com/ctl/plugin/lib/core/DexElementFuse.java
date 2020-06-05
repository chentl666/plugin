package com.ctl.plugin.lib.core;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * Description: 宿主和插件 DexElement进行合并 融合
 */
public class DexElementFuse {

    private Resources resources = null;

    /**
     * 此方法的主要目的是，宿主和插件的 DexElement融合
     */
    public void mainPluginFuse(Context mContext, String pluginPath) throws Exception {
        // TODO 宿主的dexElements
        Class mBaseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
        Field pathListField = mBaseDexClassLoaderClass.getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object mDexPathList = pathListField.get(mContext.getClassLoader());
        Field dexElementsField = mDexPathList.getClass().getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);
        Object mainDexElements = dexElementsField.get(mDexPathList);

        // TODO 插件的dexElements
        File fileDir = mContext.getDir("pDir", Context.MODE_PRIVATE);
        DexClassLoader dexClassLoader = new DexClassLoader(pluginPath, fileDir.getAbsolutePath(), null, mContext.getClassLoader());
        Class mBaseDexClassLoaderClass2 = Class.forName("dalvik.system.BaseDexClassLoader");
        Field pathListField2 = mBaseDexClassLoaderClass2.getDeclaredField("pathList");
        pathListField2.setAccessible(true);
        Object mDexPathList2 = pathListField2.get(dexClassLoader);
        Field dexElementsField2 = mDexPathList2.getClass().getDeclaredField("dexElements");
        dexElementsField2.setAccessible(true);
        Object pluginDexElements = dexElementsField2.get(mDexPathList2);

        // TODO 创造出新的 newDexElements
        int mainLen = Array.getLength(mainDexElements);
        int pluginLen = Array.getLength(pluginDexElements);
        int newDexElementsLength = (mainLen + pluginLen);
        Object newDexElements = Array.newInstance(mainDexElements.getClass().getComponentType(), newDexElementsLength);

        // 进行融合
        for (int i = 0; i < newDexElementsLength; i++) {
            // 先融合宿主
            if (i < mainLen) {
                Array.set(newDexElements, i, Array.get(mainDexElements, i));
            } else { // 在融合插件，为什么要i - mainLen，是为了保证取出pluginDexElements，是从0 开始取的
                Array.set(newDexElements, i, Array.get(pluginDexElements, i - mainLen));
            }
        }

        // 把新的替换到宿主中去

        dexElementsField.set(mDexPathList, newDexElements);

        loadResource(mContext, pluginPath);
    }

    /*public ClassLoader getClassLoader() {
        return dexClassLoader;
    }*/

    /**
     * 拥有加载资源的能力
     *
     * @param mContext
     * @throws Exception
     */
    private void loadResource(Context mContext, String pluginPath) throws Exception {
        Resources r = mContext.getResources();
        AssetManager assetManager = AssetManager.class.newInstance();
        Method addAssetpathMethod = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
        addAssetpathMethod.setAccessible(true);
        addAssetpathMethod.invoke(assetManager, pluginPath);

        resources = new Resources(assetManager, r.getDisplayMetrics(), r.getConfiguration());
    }

    public Resources getResources() {
        return resources;
    }

}

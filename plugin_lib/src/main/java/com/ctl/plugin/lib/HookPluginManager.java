package com.ctl.plugin.lib;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ctl.plugin.lib.proxy.HookProxyActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * created by : chentl
 * Date: 2020/06/03
 */
public class HookPluginManager {

    private final String TAG = getClass().getSimpleName();
    private static final String ACTIONINTENT = "HookPluginManager.actionIntent";
    private static HookPluginManager hookPluginManager;
    private final Context context;
    private Resources resources;
    private String pluginApkPath;
    private AssetManager assetManager;

    public static HookPluginManager getInstance(Context context) {
        if (hookPluginManager == null) {
            synchronized (HookPluginManager.class) {
                if (hookPluginManager == null) {
                    hookPluginManager = new HookPluginManager(context);
                }
            }
        }
        return hookPluginManager;
    }

    private HookPluginManager(Context context) {
        this.context = context;
    }

    /**
     * 加载插件
     *
     * @param path 插件路径
     */
    public HookPluginManager loadPlugin(String path) {
        File file = new File(path);
        if (!file.exists()) {
            Log.e(TAG, "插件不存在...");
            return this;
        }
        pluginApkPath = file.getAbsolutePath();
        try {

            hookAmsAction();

            hookLaunchActivity();

            pluginToAppAction();

            assetManager = AssetManager.class.newInstance();
            Method addAssetPathMethod = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
            addAssetPathMethod.setAccessible(true);
            addAssetPathMethod.invoke(assetManager, pluginApkPath);

            // 实例化此方法 final StringBlock[] ensureStringBlocks()
            Method ensureStringBlocksMethod = assetManager.getClass().getDeclaredMethod("ensureStringBlocks");
            ensureStringBlocksMethod.setAccessible(true);
            ensureStringBlocksMethod.invoke(assetManager); // 执行了ensureStringBlocks  string.xml  color.xml   anim.xml 被初始化

            resources = new Resources(assetManager, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());


        } catch (Exception e) {
            e.printStackTrace();
            Log.e("hook", "hookAction 失败 e:" + e.toString());
        }
        return this;
    }

    /**
     * 要在执行 AMS之前，替换可用的 Activity，替换在AndroidManifest里面配置的Activity
     */
    private void hookAmsAction() throws Exception {
        // 动态代理
        Class mIActivityManagerClass = Class.forName("android.app.IActivityManager");

        // 我们要拿到IActivityManager对象，才能让动态代理里面的 invoke 正常执行下
        // 执行此方法 static public IActivityManager getDefault()，就能拿到 IActivityManager
        Class mActivityManagerNativeClass2 = Class.forName("android.app.ActivityManagerNative");
        final Object mIActivityManager = mActivityManagerNativeClass2.getMethod("getDefault").invoke(null);

        // 本质是IActivityManager
        Object mIActivityManagerProxy = Proxy.newProxyInstance(
                context.getClassLoader(),
                new Class[]{mIActivityManagerClass}, // 要监听的接口
                new InvocationHandler() { // IActivityManager 接口的回调方法
                    /**
                     * @param proxy
                     * @param method IActivityManager里面的方法
                     * @param args IActivityManager里面的参数
                     * @return
                     * @throws
                     */
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("startActivity".equals(method.getName())) {
                            // 做自己的业务逻辑
                            // 换成 可以 通过 AMS检查的 HookProxyActivity
                            // 用ProxyActivity 绕过了 AMS检查
                            Intent intent = new Intent(context, HookProxyActivity.class);
                            intent.putExtra(ACTIONINTENT, ((Intent) args[2])); // 把之前TestActivity保存 携带过去
                            args[2] = intent;
                        }
                        Log.i("hook", "拦截到了IActivityManager里面的方法" + method.getName());
                        // 让系统继续正常往下执行
                        return method.invoke(mIActivityManager, args);
                    }
                });

        /**
         * 为了拿到 gDefault
         * 通过 ActivityManagerNative 拿到 gDefault变量(对象)
         */
        Class mActivityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
        Field gDefaultField = mActivityManagerNativeClass.getDeclaredField("gDefault");
        gDefaultField.setAccessible(true); // 授权
        Object gDefault = gDefaultField.get(null);

        // 替换点
        Class mSingletonClass = Class.forName("android.util.Singleton");
        // 获取此字段 mInstance
        Field mInstanceField = mSingletonClass.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true); // 让虚拟机不要检测 权限修饰符
        // 替换
        mInstanceField.set(gDefault, mIActivityManagerProxy); // 替换是需要gDefault
    }

    /**
     * Hook LaunchActivity
     */
    private void hookLaunchActivity() throws Exception {

        Field mCallbackFiled = Handler.class.getDeclaredField("mCallback");
        mCallbackFiled.setAccessible(true); // 授权

        /**
         * handler对象怎么来
         * 1.寻找H，先寻找ActivityThread
         *
         * 执行此方法 public static ActivityThread currentActivityThread()
         *
         * 通过ActivityThread 找到 H
         *
         */
        Class mActivityThreadClass = Class.forName("android.app.ActivityThread");
        // 获得ActivityThrea对象
        Object mActivityThread = mActivityThreadClass.getMethod("currentActivityThread").invoke(null);

        Field mHField = mActivityThreadClass.getDeclaredField("mH");
        mHField.setAccessible(true);
        // 获取真正对象
        Handler mH = (Handler) mHField.get(mActivityThread);

        mCallbackFiled.set(mH, new MyCallback(mH)); // 替换 增加我们自己的实现代码
    }

    public static final int LAUNCH_ACTIVITY = 100;

    class MyCallback implements Handler.Callback {

        private Handler mH;

        public MyCallback(Handler mH) {
            this.mH = mH;
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case LAUNCH_ACTIVITY:
                    // 做我们在自己的业务逻辑（HookProxyActivity 换成  TestActivity）
                    Object obj = msg.obj;
                    try {
                        // 我们要获取之前Hook携带过来的 TestActivity
                        Field intentField = obj.getClass().getDeclaredField("intent");
                        intentField.setAccessible(true);

                        // 获取 intent 对象，才能取出携带过来的 actionIntent
                        Intent intent = (Intent) intentField.get(obj);
                        // actionIntent == TestActivity的Intent
                        Intent actionIntent = intent.getParcelableExtra(ACTIONINTENT);

                        if (actionIntent != null) {
//                            if (activityList.contains(actionIntent.getComponent().getClassName())) {
                            intentField.set(obj, actionIntent); // HookProxyActivity 换成  TestActivity
//                            } else { // 没有权限
//                                intentField.set(obj, new Intent(context, PermissionActivity.class));
//                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
            mH.handleMessage(msg);
            // 让系统继续正常往下执行
            // return false; // 系统就会往下执行
            return true; // 系统不会往下执行
        }
    }
    /**
     * 把插件的dexElements 和 宿主中的 dexElements 融为一体
     */
    private void pluginToAppAction() throws Exception {
        // 第一步：找到宿主 dexElements 得到此对象   PathClassLoader代表是宿主
        PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader(); // 本质就是PathClassLoader
        Class mBaseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
        // private final DexPathList pathList;
        Field pathListField = mBaseDexClassLoaderClass.getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object mDexPathList = pathListField.get(pathClassLoader);

        Field dexElementsField = mDexPathList.getClass().getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);
        // 本质就是 Element[] dexElements
        Object dexElements = dexElementsField.get(mDexPathList);

        /*** ---------------------- ***/


        // 第二步：找到插件 dexElements 得到此对象，代表插件 DexClassLoader--代表插件
        File file = new File(pluginApkPath);
        if (!file.exists()) {
            throw new FileNotFoundException("没有找到插件包!!");
        }
        String pluginPath = file.getAbsolutePath();
        File fileDir = context.getDir("pluginDir", Context.MODE_PRIVATE); // data/data/包名/pluginDir/
        DexClassLoader dexClassLoader = new
                DexClassLoader(pluginPath, fileDir.getAbsolutePath(), null, context.getClassLoader());

        Class mBaseDexClassLoaderClassPlugin = Class.forName("dalvik.system.BaseDexClassLoader");
        // private final DexPathList pathList;
        Field pathListFieldPlugin = mBaseDexClassLoaderClassPlugin.getDeclaredField("pathList");
        pathListFieldPlugin.setAccessible(true);
        Object mDexPathListPlugin = pathListFieldPlugin.get(dexClassLoader);

        Field dexElementsFieldPlugin = mDexPathListPlugin.getClass().getDeclaredField("dexElements");
        dexElementsFieldPlugin.setAccessible(true);
        // 本质就是 Element[] dexElements
        Object dexElementsPlugin = dexElementsFieldPlugin.get(mDexPathListPlugin);


        // 第三步：创建出 新的 dexElements []
        int mainDexLeng =  Array.getLength(dexElements);
        int pluginDexLeng =  Array.getLength(dexElementsPlugin);
        int sumDexLeng = mainDexLeng + pluginDexLeng;

        // 参数一：int[]  String[] ...  我们需要Element[]
        // 参数二：数组对象的长度
        // 本质就是 Element[] newDexElements
        Object newDexElements = Array.newInstance(dexElements.getClass().getComponentType(),sumDexLeng); // 创建数组对象


        // 第四步：宿主dexElements + 插件dexElements =----> 融合  新的 newDexElements
        for (int i = 0; i < sumDexLeng; i++) {
            // 先融合宿主
            if (i < mainDexLeng) {
                // 参数一：新要融合的容器 -- newDexElements
                Array.set(newDexElements, i, Array.get(dexElements, i));
            } else { // 再融合插件的
                Array.set(newDexElements, i, Array.get(dexElementsPlugin, i - mainDexLeng));
            }

        }

        // 第五步：把新的 newDexElements，设置到宿主中去
        // 宿主
        dexElementsField.set(mDexPathList, newDexElements);

    }

    public Resources getResources() {
        return resources;
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }
}

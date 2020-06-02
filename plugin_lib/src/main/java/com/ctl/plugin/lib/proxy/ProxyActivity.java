package com.ctl.plugin.lib.proxy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.ctl.plugin.lib.PluginManager;
import com.ctl.plugin.lib.stander.ActivityInterface;

import java.lang.reflect.Constructor;

/**
 * created by : chentl
 * Date: 2020/05/07
 */
public class ProxyActivity extends Activity {

    private  ActivityInterface activityInterface;

    @Override
    public Resources getResources() {
        return PluginManager.getInstance(this).getResources();
    }

    @Override
    public ClassLoader getClassLoader() {
        return PluginManager.getInstance(this).getDexClassLoader();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String classname = getIntent().getStringExtra(PluginManager.CLASSNAME);
        try {
            Class loadClass = getClassLoader().loadClass(classname);
            Constructor constructor = loadClass.getConstructor(new Class[]{});
            Object mPluginActivity = constructor.newInstance(new Object[]{});

            activityInterface = (ActivityInterface) mPluginActivity;
            activityInterface.insertAppContext(this);

            activityInterface.onCreate(savedInstanceState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (activityInterface != null) {
            activityInterface.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (activityInterface != null) {
            activityInterface.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (activityInterface != null) {
            activityInterface.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (activityInterface != null) {
            activityInterface.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activityInterface != null) {
            activityInterface.onDestroy();
        }
    }

    @Override
    public void startActivity(Intent intent) {
        String classname = intent.getStringExtra(PluginManager.CLASSNAME);
        Intent intentProxy = new Intent(this, ProxyActivity.class);
        intentProxy.putExtra(PluginManager.CLASSNAME, classname);
        super.startActivity(intentProxy);
    }

    @Override
    public ComponentName startService(Intent service) {
        String classname = service.getStringExtra(PluginManager.CLASSNAME);
        Intent intent = new Intent(this, ProxyService.class);
        intent.putExtra(PluginManager.CLASSNAME, classname);
        return super.startService(intent);
    }

    @Override
    public boolean stopService(Intent name) {
        String classname = name.getStringExtra(PluginManager.CLASSNAME);
        Intent intent = new Intent(this, ProxyService.class);
        intent.putExtra(PluginManager.CLASSNAME, classname);
        return super.stopService(intent);
    }

    private ProxyReceiver proxyReceiver;

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        String pluginReceiverClassName = receiver.getClass().getName();
        proxyReceiver = new ProxyReceiver(pluginReceiverClassName);
        return super.registerReceiver(proxyReceiver, filter);
    }

    @Override
    public void sendBroadcast(Intent intent) {
        super.sendBroadcast(intent);
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        super.unregisterReceiver(proxyReceiver);
    }
}

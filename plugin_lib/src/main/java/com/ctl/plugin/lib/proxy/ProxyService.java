package com.ctl.plugin.lib.proxy;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.ctl.plugin.lib.PluginManager;
import com.ctl.plugin.lib.stander.ServiceInterface;

/**
 * created by : chentl
 * Date: 2020/05/07
 */
public class ProxyService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private ServiceInterface serviceInterface;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String classname = intent.getStringExtra(PluginManager.CLASSNAME);
        try {
            Class loadClass = PluginManager.getInstance(this).getDexClassLoader().loadClass(classname);
            Object instance = loadClass.newInstance();
            serviceInterface = (ServiceInterface) instance;
            serviceInterface.insertAppContext(this);
            serviceInterface.onStartCommand(intent, flags, startId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (serviceInterface != null) {
            serviceInterface.onDestroy();
        }
        super.onDestroy();
    }
}

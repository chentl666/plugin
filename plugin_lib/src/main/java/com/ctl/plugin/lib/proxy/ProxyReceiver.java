package com.ctl.plugin.lib.proxy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ctl.plugin.lib.PluginManager;
import com.ctl.plugin.lib.stander.ReceiverInterface;

/**
 * created by : chentl
 * Date: 2020/05/08
 */
public class ProxyReceiver extends BroadcastReceiver {
    private final String pluginReceiverClassName;

    public ProxyReceiver(String pluginReceiverClassName) {
        this.pluginReceiverClassName = pluginReceiverClassName;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            Class<?> aClass = PluginManager.getInstance(context).getDexClassLoader().loadClass(pluginReceiverClassName);
            Object instance = aClass.newInstance();
            ReceiverInterface receiverInterface = (ReceiverInterface)instance;
            receiverInterface.onReceive(context, intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.ctl.plugin_package;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.ctl.plugin.lib.stander.ReceiverInterface;


/**
 * created by : chentl
 * Date: 2020/05/08
 */
public class PluginReceiver extends BroadcastReceiver implements ReceiverInterface {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "我是插件广播接收者", Toast.LENGTH_LONG).show();
    }
}

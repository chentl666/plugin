package com.ctl.plugin_package;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * created by : chentl
 * Date: 2020/05/08
 */
public class StaticReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,"插件里的静态广播",Toast.LENGTH_LONG).show();
    }
}

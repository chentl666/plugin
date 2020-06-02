package com.ctl.plugin_package;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ctl.plugin.lib.base.BasePluginActivity;

public class PluginActivity extends BasePluginActivity {

    private static final String ACTION = "com.ctl.plugin_package.PluginActivity";

    private PluginReceiver pluginReceiver;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(appActivity, "加载插件成功", Toast.LENGTH_LONG).show();

        findViewById(R.id.btn_jump_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(appActivity, Plugin2Activity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_jump_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(appActivity, PluginService.class);
                startService(intent);
            }
        });

        findViewById(R.id.btn_stop_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(appActivity, PluginService.class);
                stopService(intent);
            }
        });

        findViewById(R.id.btn_start_receiver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(ACTION);
                pluginReceiver = new PluginReceiver();
                registerReceiver(pluginReceiver, intentFilter);
            }
        });

        findViewById(R.id.btn_send_receiver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(ACTION);
                sendBroadcast(intent);
            }
        });
        findViewById(R.id.btn_unregister_receiver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pluginReceiver != null) {
                    unregisterReceiver(pluginReceiver);
                }
            }
        });
    }

}

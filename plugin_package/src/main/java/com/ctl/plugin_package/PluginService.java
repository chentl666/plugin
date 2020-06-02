package com.ctl.plugin_package;

import android.content.Intent;
import android.util.Log;

import com.ctl.plugin.lib.base.BasePluginService;

/**
 * created by : chentl
 * Date: 2020/05/07
 */
public class PluginService extends BasePluginService {

    private Thread thread;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!thread.isInterrupted()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    } finally {
                        Log.i("PluginService", "插件服务启动中");
                    }
                }
            }
        });
        thread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (thread != null) {
            thread.interrupt();
        }
        super.onDestroy();
    }
}

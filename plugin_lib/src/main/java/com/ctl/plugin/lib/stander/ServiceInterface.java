package com.ctl.plugin.lib.stander;

import android.app.Service;
import android.content.Intent;

/**
 * created by : chentl
 * Date: 2020/05/07
 */
public interface ServiceInterface {

    void insertAppContext(Service appService);

    void onCreate();

    int onStartCommand(Intent intent, int flags, int startId);

    void onDestroy();
}

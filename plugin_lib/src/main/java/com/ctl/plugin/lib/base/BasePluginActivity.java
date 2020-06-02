package com.ctl.plugin.lib.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.ctl.plugin.lib.PluginManager;
import com.ctl.plugin.lib.stander.ActivityInterface;


/**
 * created by : chentl
 * Date: 2020/05/07
 */
public class BasePluginActivity extends Activity implements ActivityInterface {
    public Activity appActivity;

    @Override
    public void insertAppContext(Activity appActivity) {
        this.appActivity = appActivity;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onCreate(Bundle savedInstanceState) {

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onStart() {

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onResume() {

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onPause() {

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onStop() {

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onDestroy() {

    }

    public void setContentView(int resId) {
        appActivity.setContentView(resId);
    }

    public View findViewById(int id) {
        return appActivity.findViewById(id);
    }

    @Override
    public void startActivity(Intent intent) {
        Intent intentNew = new Intent();
        intentNew.putExtra(PluginManager.CLASSNAME, intent.getComponent().getClassName());
        appActivity.startActivity(intentNew);
    }

    @Override
    public ComponentName startService(Intent service) {
        Intent intent = new Intent();
        intent.putExtra(PluginManager.CLASSNAME, service.getComponent().getClassName());
        return appActivity.startService(intent);
    }

    @Override
    public boolean stopService(Intent name) {
        Intent intent = new Intent();
        intent.putExtra(PluginManager.CLASSNAME, name.getComponent().getClassName());
        return appActivity.stopService(intent);
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return appActivity.registerReceiver(receiver, filter);
    }

    @Override
    public void sendBroadcast(Intent intent) {
        appActivity.sendBroadcast(intent);
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        appActivity.unregisterReceiver(receiver);
    }
}

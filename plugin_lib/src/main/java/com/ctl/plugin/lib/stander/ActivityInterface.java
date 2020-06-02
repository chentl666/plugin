package com.ctl.plugin.lib.stander;

import android.app.Activity;
import android.os.Bundle;

/**
 * created by : chentl
 * Date: 2020/05/07
 */
public interface ActivityInterface {

    void insertAppContext(Activity appActivity);

    void onCreate(Bundle savedInstanceState);

    void onStart();

    void onResume();

    void onPause();

    void onStop();

    void onDestroy();

}

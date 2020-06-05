package com.ctl.plugin_package.hook;

import android.os.Bundle;
import android.widget.Toast;

import com.ctl.plugin_package.R;

/**
 * created by : chentl
 * Date: 2020/06/03
 */
public class HookPluginActivity extends HookBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin_hook);
        // 占位式 报错
        // Toast.makeText(appActivity, "plugin", Toast.LENGTH_SHORT).show();

        // Hook式 不会报错  this 当前运行宿主  插件中的dexElements 和 宿主中的dexElements
        Toast.makeText(this, "Hook式启动插件Activity", Toast.LENGTH_SHORT).show();
    }
}

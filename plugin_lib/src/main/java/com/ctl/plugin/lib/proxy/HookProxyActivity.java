package com.ctl.plugin.lib.proxy;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

// 必须要在AndroidManifest注册，为什么，因为此Activity是需要通过 AMS 检查的
public class HookProxyActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Toast.makeText(this, "我是代理的Activity", Toast.LENGTH_SHORT).show();
    }
}

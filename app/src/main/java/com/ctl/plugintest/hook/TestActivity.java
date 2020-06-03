package com.ctl.plugintest.hook;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.ctl.plugintest.R;

public class TestActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);
    }
}

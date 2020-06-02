package com.ctl.plugintest;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ctl.plugin.lib.PluginManager;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button hookBtn = findViewById(R.id.btn_hook);
        hookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, ((Button) v).getText(), Toast.LENGTH_LONG).show();
            }
        });
        try {
            hook(hookBtn);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "hook失败" + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void hook(View view) throws Exception {

        Class viewClass = Class.forName("android.view.View");
        Method getListenerInfoMethod = viewClass.getDeclaredMethod("getListenerInfo");
        getListenerInfoMethod.setAccessible(true);
        Object mListenerInfo = getListenerInfoMethod.invoke(view);

        Class mListenerInfoClass = Class.forName("android.view.View$ListenerInfo");
        Field mOnClickListenerField = mListenerInfoClass.getField("mOnClickListener");
        final Object mOnClickListenerObj = mOnClickListenerField.get(mListenerInfo);

        Object object = Proxy.newProxyInstance(MainActivity.this.getClassLoader(),
                new Class[]{View.OnClickListener.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Button button = new Button(MainActivity.this);
                        button.setText("修改成功！");
                        return method.invoke(mOnClickListenerObj, button);
                    }
                });
        mOnClickListenerField.set(mListenerInfo, object);
    }

    public void loadPlugin(View view) {
        PluginManager
                .getInstance(this)
                .loadPlugin(getExternalFilesDir(null) + File.separator + "p.apk");
    }

    public void openPlugin(View view) {
        PluginManager
                .getInstance(this)
                .openPlugin();
    }

    public void registerStaticReceiver(View view) {

    }

    public void sendStaticReceiver(View view) {

    }


}

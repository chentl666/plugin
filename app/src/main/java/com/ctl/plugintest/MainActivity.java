package com.ctl.plugintest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ctl.plugin.lib.PluginManager;
import com.ctl.plugintest.hook.TestActivity;

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
            hook(hookBtn);// 在不修改以上代码的情况下，通过Hook把 ((Button) v).getText() 内容给修改
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "hook失败" + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void hook(View view) throws Exception {
        // 之前 的 还是 用户写的 实现代码
        // 为了获取 @1 对象，需要执行 这个方法，才能拿到
        /*
            ListenerInfo getListenerInfo()
         */
        Class viewClass = Class.forName("android.view.View");
        Method getListenerInfoMethod = viewClass.getDeclaredMethod("getListenerInfo");
        getListenerInfoMethod.setAccessible(true); // 授权
        Object mListenerInfo = getListenerInfoMethod.invoke(view);  // 执行方法

        // 替 换  public OnClickListener mOnClickListener; 替换我们自己的
        Class mListenerInfoClass = Class.forName("android.view.View$ListenerInfo");
        Field mOnClickListenerField = mListenerInfoClass.getField("mOnClickListener");
        final Object mOnClickListenerObj = mOnClickListenerField.get(mListenerInfo);// 需要@1对象

        // 1.监听 onClick，当用户点击按钮的时候-->onClick， 我们自己要先拦截这个事件
        // 动态代理
        // mOnClickListener 本质是==OnClickListener
        Object object = Proxy.newProxyInstance(MainActivity.this.getClassLoader(), // 1加载器
                new Class[]{View.OnClickListener.class},// 2要监听的接口，监听什么接口，就返回什么接口
                new InvocationHandler() {// 3监听接口方法里面的回调
                    /**
                     *
                     * void onClick(View v);
                     *
                     * onClick ---> Method
                     * View v ---> Object[] args
                     *
                     * @param proxy
                     * @param method
                     * @param args
                     * @return
                     * @throws
                     */
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // 加入了自己逻辑
                        Log.d("hook", "拦截到了 OnClickListener的方法了");
                        Button button = new Button(MainActivity.this);
                        button.setText("修改成功！");
                        // 让系统程序片段 --- 正常继续的执行下去
                        return method.invoke(mOnClickListenerObj, button);
                    }
                });
        // 狸猫换太子 把系统的 mOnClickListener  换成 我们自己写的 动态代理
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
        PluginManager
                .getInstance(this)
                .loadStaticReceiver();
    }

    public void sendStaticReceiver(View view) {
        Intent intent = new Intent();
        intent.setAction("com.ctl.plugin_package.StaticReceiver");
        sendBroadcast(intent);
    }


    public void openHookTest(View view) {
        startActivity(new Intent(MainActivity.this, TestActivity.class));
    }
}

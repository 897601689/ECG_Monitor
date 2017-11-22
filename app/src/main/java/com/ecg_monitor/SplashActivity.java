package com.ecg_monitor;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import util.FontsUtils;

/**
 * Created by admin on 2017/7/28.
 */

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideSystemUIMenu();//隐藏系统菜单
        setContentView(R.layout.activity_splash);

        FontsUtils.setAppTypeface(SplashActivity.this);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent it = new Intent();
                it.setClass(SplashActivity.this, MainActivity.class);
                startActivity(it);
                finish();
            }
        }, 100 * 2);
    }

    private void hideSystemUIMenu() {
        //实现无标题栏（但有系统自带的任务栏）
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        //实现全屏效果
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //应用运行时，保持屏幕高亮，不锁屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //隐藏虚拟按键，并且全屏
        //这里的4108可防止从底部滑动调出底部导航栏
        getWindow().getDecorView().setSystemUiVisibility(4108);

    }
}

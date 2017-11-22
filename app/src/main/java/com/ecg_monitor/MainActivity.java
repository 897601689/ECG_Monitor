package com.ecg_monitor;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

import butterknife.BindView;
import butterknife.ButterKnife;
import util.FontsUtils;

public class MainActivity extends Activity {

    private static final String TAG ="MainActivity" ;
    @BindView(R.id.txt_ecg_high)
    TextView txtEcgHigh;
    @BindView(R.id.txt_ecg_low)
    TextView txtEcgLow;
    @BindView(R.id.txt_ecg)
    TextView txtEcg;
    @BindView(R.id.fullscreen)
    LinearLayout fullscreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //hideSystemUIMenu();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
//        Typeface mTypeface = Typeface.createFromAsset(getAssets(), "fonts/PingFang Medium.ttf");
//
//        try {
//            Field field = Typeface.class.getDeclaredField("SERIF");
//            field.setAccessible(true);
//            field.set(null, mTypeface);
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
        init();
        Log.e(TAG, "onCreate: " );
    }

    private void init() {
        FontsUtils.setAppTypeface(MainActivity.this);
    }

    //隐藏系统菜单
    private void hideSystemUIMenu() {
        //实现无标题栏（但有系统自带的任务栏）
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        //实现全屏效果
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //应用运行时，保持屏幕高亮，不锁屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //隐藏虚拟按键，并且全屏
        //if (Build.VERSION.SDK_INT >= 19) {
        //for new api versions.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //}

    }


}

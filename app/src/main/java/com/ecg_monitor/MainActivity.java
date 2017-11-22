package com.ecg_monitor;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import util.MySurfaceView;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    static SimpleDateFormat format;//系统时间
    @BindView(R.id.ecg_Curve1)
    MySurfaceView ecgCurve1;
    @BindView(R.id.ecg_Curve2)
    MySurfaceView ecgCurve2;
    @BindView(R.id.ecg_Curve3)
    MySurfaceView ecgCurve3;
    @BindView(R.id.spo2_Curve)
    MySurfaceView spo2Curve;
    @BindView(R.id.resp_Curve)
    MySurfaceView respCurve;
    @BindView(R.id.txt_tech_alarm)
    TextView txtTechAlarm;
    @BindView(R.id.txt_bio_alarm)
    TextView txtBioAlarm;
    @BindView(R.id.txt_ecg_high)
    TextView txtEcgHigh;
    @BindView(R.id.txt_ecg_low)
    TextView txtEcgLow;
    @BindView(R.id.txt_ecg)
    TextView txtEcg;
    @BindView(R.id.txt_nibp_high)
    TextView txtNibpHigh;
    @BindView(R.id.txt_nibp_low)
    TextView txtNibpLow;
    @BindView(R.id.txt_nibp)
    TextView txtNibp;
    @BindView(R.id.txt_bp_avg)
    TextView txtBpAvg;
    @BindView(R.id.txt_spo2_high)
    TextView txtSpo2High;
    @BindView(R.id.txt_spo2_low)
    TextView txtSpo2Low;
    @BindView(R.id.txt_spo2)
    TextView txtSpo2;
    @BindView(R.id.txt_pulse_high)
    TextView txtPulseHigh;
    @BindView(R.id.txt_pulse_low)
    TextView txtPulseLow;
    @BindView(R.id.txt_pulse)
    TextView txtPulse;
    @BindView(R.id.txt_resp_high)
    TextView txtRespHigh;
    @BindView(R.id.txt_resp_low)
    TextView txtRespLow;
    @BindView(R.id.txt_resp)
    TextView txtResp;
    @BindView(R.id.txt_temp_high)
    TextView txtTempHigh;
    @BindView(R.id.txt_temp_low)
    TextView txtTempLow;
    @BindView(R.id.txt_temp)
    TextView txtTemp;
    @BindView(R.id.txt_time)
    TextView txtTime;
    private List<String> mSafeAlertMessage = new ArrayList<>();//生理参数报警
    private List<String> mTechnologyMessage = new ArrayList<>();//技术报警
    MyHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUIMenu();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        init();
        Log.e(TAG, "onCreate: ");
    }

    private void init() {
        //实例化一个MyHandler对象
        mHandler = new MyHandler(MainActivity.this);


        ecgCurve1.setPen(Color.rgb(0, 255, 255));//设置曲线颜色
        ecgCurve1.setMax(150);

        ecgCurve2.setPen(Color.rgb(0, 255, 255));//设置曲线颜色
        ecgCurve2.setMax(150);

        ecgCurve3.setPen(Color.rgb(0, 255, 255));//设置曲线颜色
        ecgCurve3.setMax(150);

        spo2Curve.setPen(Color.rgb(50, 255, 50));//设置曲线颜色
        spo2Curve.setMax(127);

        respCurve.setPen(Color.rgb(50, 255, 50));//设置曲线颜色
        respCurve.setMax(127);




        new Thread(new AlarmAndTime()).start();
    }

    static class MyHandler extends Handler {
        //注意下面的“PopupActivity”类是MyHandler类所在的外部类，即所在的activity
        WeakReference<MainActivity> mActivity;

        MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity theActivity = mActivity.get();
            switch (msg.what) { //此处可以根据what的值处理多条信息
                case 0x01://时间
                    format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    theActivity.txtTime.setText(format.format(new Date()));
                    //Log.e(TAG, format.format(new Date()) );
                    break;
                case 0x02://心电
                    break;
                case 0x03://血压
                    break;
                case 0x04://血氧
                    break;
                case 0x05://
                    break;
                case 0x06://
                    break;
            }
        }
    }


    //报警和时间显示
    private class AlarmAndTime implements Runnable {

        @Override
        public void run() {
            try {
                while (true) {
                    Thread.sleep(1000);
                    mHandler.sendEmptyMessage(0x01);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 添加报警信息
     *
     * @param value   当前显示值
     * @param alertH  上限值
     * @param alertL  下限值
     * @param mSwitch 报警开关
     * @param name    报警项
     */
    private void AddAlarmInfo(TextView value, TextView alertH, TextView alertL, boolean mSwitch, String name) {
        float now = 0;
        int H = Integer.parseInt(alertH.getText().toString());
        int L = Integer.parseInt(alertL.getText().toString());
        if (!"--".equals(value.getText())) {
            now = Float.parseFloat(value.getText().toString());
            if (now > H) {
                if (mSwitch) {
                    if (!mSafeAlertMessage.contains(name + "过高"))
                        mSafeAlertMessage.add(name + "过高");
                }
            } else if (now < L) {
                if (mSwitch) {
                    if (!mSafeAlertMessage.contains(name + "过低"))
                        mSafeAlertMessage.add(name + "过低");
                }
            }
        }
    }


    //隐藏系统菜单
    private void hideSystemUIMenu() {
        //实现无标题栏（但有系统自带的任务栏）
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //}
        //实现全屏效果
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //应用运行时，保持屏幕高亮，不锁屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //隐藏虚拟按键，并且全屏
//        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
//            View v = this.getWindow().getDecorView();
//            v.setSystemUiVisibility(View.GONE);
//        } else if (Build.VERSION.SDK_INT >= 19) {
        //for new api versions.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
//        }
        getWindow().getDecorView().setSystemUiVisibility(4108);//这里的4108可防止从底部滑动调出底部导航栏


    }


}
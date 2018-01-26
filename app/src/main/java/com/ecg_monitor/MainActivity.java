package com.ecg_monitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.zaaach.toprightmenu.MenuItem;
import com.zaaach.toprightmenu.TopRightMenu;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import util.Directive;
import util.Global;
import util.Monitor;
import widgets.MySurfaceView;

public class MainActivity extends Activity {

    //<editor-fold desc="控件">
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
    @BindView(R.id.txt_nibp_h)
    TextView txtNibpH;
    @BindView(R.id.txt_nibp_l)
    TextView txtNibpL;
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
    //</editor-fold>

    private static final String TAG = "MainActivity";
    static SimpleDateFormat format;//系统时间
    private List<String> mSafeAlertMessage = new ArrayList<>();//生理参数报警
    private List<String> mTechnologyMessage = new ArrayList<>();//技术报警
    private Monitor monitor = new Monitor();
    MyHandler mHandler;
    TopRightMenu mTopRightMenu;

    //<editor-fold desc="播放报警音">
    static SoundPool soundPool;
    static int loadId;
    //</editor-fold>

    //<editor-fold desc="网络">
    //Socket套接字
    private Socket socket = null;
    public static String ip = "10.10.100.254";    //服务器IP
    //服务器端口
    private static final int SERVER_PORT = 8899;

    private static boolean socketStatus = false;
    public static List<String> cmd_list = new ArrayList<String>();
    private static OutputStream outputStream = null;
    private static InputStream inputStream = null;
    //</editor-fold>
    private View bp_stat_view;
    private LayoutInflater bp_stat_inflater;
    private TextView bp_stat_value_text;


    MonitorData monitorData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUIMenu();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initMenu();

        init();


    }

    //Activity彻底运行起来之后的回调
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        bp_stat_inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        bp_stat_view = bp_stat_inflater.inflate(R.layout.stat_bp_cali, null);
        bp_stat_value_text = bp_stat_view.findViewById(R.id.thre);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void init() {
        //实例化一个MyHandler对象
        mHandler = new MyHandler(MainActivity.this);

        ecgCurve1.setPen(Color.rgb(50, 255, 50));//设置曲线颜色
        ecgCurve1.setInfo("I");
        ecgCurve1.setAmplitude(15);
        ecgCurve1.setMax(4096);
        ecgCurve1.setTime(13);

        ecgCurve2.setPen(Color.rgb(50, 255, 50));//设置曲线颜色
        ecgCurve2.setInfo("II");
        ecgCurve2.setAmplitude(15);
        ecgCurve2.setMax(4096);

        ecgCurve3.setPen(Color.rgb(50, 255, 50));//设置曲线颜色
        ecgCurve3.setInfo("V1");
        ecgCurve3.setAmplitude(15);
        ecgCurve3.setMax(4096);

        spo2Curve.setPen(Color.rgb(50, 50, 250));//设置曲线颜色
        spo2Curve.setMax(100);

        respCurve.setPen(Color.rgb(255, 255, 50));//设置曲线颜色
        respCurve.setMax(4096);

        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 100);
        loadId = soundPool.load(getBaseContext(), R.raw.alarm, 1);

        //获取IP地址
        SharedPreferences read = getSharedPreferences("setting", MODE_PRIVATE);
        ip = read.getString("IP", ip);

        new Thread(new AlarmAndTime()).start();
        monitorData = new MonitorData();
        new Thread(monitorData).start();

        IntentFilter filter = new IntentFilter(AlertActivity.action);
        registerReceiver(broadcastReceiver, filter);
    }

    private void initMenu() {
        mTopRightMenu = new TopRightMenu(MainActivity.this);
        List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem(R.mipmap.ico, "报警设置"));
        menuItems.add(new MenuItem(R.mipmap.ico, "维护设置"));
        menuItems.add(new MenuItem(R.mipmap.ico, "心电设置"));
        menuItems.add(new MenuItem(R.mipmap.ico, "血压设置"));
        menuItems.add(new MenuItem(R.mipmap.ico, "启动血压测量"));
        mTopRightMenu
                .setHeight(270)     //默认高度480
                .setWidth(300)      //默认宽度wrap_content
                .setAnimationStyle(R.style.TRM_ANIM_STYLE)  //默认为R.style.TRM_ANIM_STYLE
                .addMenuList(menuItems)
                .setOnMenuItemClickListener(new TopRightMenu.OnMenuItemClickListener() {
                    @Override
                    public void onMenuItemClick(int position) {
                        switch (position) {
                            case 0:
                                List<MenuItem> menuItems = new ArrayList<>();
                                menuItems.add(new MenuItem(R.mipmap.ico, "心率报警限"));
                                menuItems.add(new MenuItem(R.mipmap.ico, "血压报警限"));
                                menuItems.add(new MenuItem(R.mipmap.ico, "血氧报警限"));
                                menuItems.add(new MenuItem(R.mipmap.ico, "呼吸率报警限"));
                                menuItems.add(new MenuItem(R.mipmap.ico, "体温报警限"));
                                showMenu(menuItems, 0);
                                break;
                            case 1:
                                menuItems = new ArrayList<>();
                                menuItems.add(new MenuItem(R.mipmap.ico, "漏气检测"));
                                menuItems.add(new MenuItem(R.mipmap.ico, "静态压力校准"));
                                menuItems.add(new MenuItem(R.mipmap.ico, "压力参数校准"));
                                menuItems.add(new MenuItem(R.mipmap.ico, "日期时间设置"));
                                menuItems.add(new MenuItem(R.mipmap.ico, "服务器IP设置"));
                                showMenu(menuItems, 1);
                                break;
                            case 2:
                                menuItems = new ArrayList<>();
                                menuItems.add(new MenuItem(R.mipmap.ico, "心电滤波模式"));
                                menuItems.add(new MenuItem(R.mipmap.ico, "心电波形增益"));
                                menuItems.add(new MenuItem(R.mipmap.ico, "呼吸波形增益"));
                                menuItems.add(new MenuItem(R.mipmap.ico, "心电导联切换"));
                                showMenu(menuItems, 2);
                                break;
                            case 3:
                                menuItems = new ArrayList<>();
                                menuItems.add(new MenuItem(R.mipmap.ico, "自动检测设置"));
                                menuItems.add(new MenuItem(R.mipmap.ico, "预充气压力"));
                                menuItems.add(new MenuItem(R.mipmap.ico, "血压模式设置"));
                                showMenu(menuItems, 3);
                                break;
                            case 4://启动血压测量
                                monitorData.send(Directive.Ecg_Handshake);
                                monitorData.send(Directive.Bp_Handshake);
                                monitorData.send(Directive.SpO2_Handshake);
                                monitorData.send(Directive.Bp_Start);
                                break;
                        }
                        Toast.makeText(MainActivity.this, "点击菜单:" + position, Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void showMenu(List<MenuItem> menuItems, final int index) {
        TopRightMenu mTopRightMenu = new TopRightMenu(MainActivity.this);
        mTopRightMenu
                .setHeight(270)     //默认高度480
                .setWidth(300)      //默认宽度wrap_content
                .setAnimationStyle(R.style.TRM_ANIM_STYLE)  //默认为R.style.TRM_ANIM_STYLE
                .addMenuList(menuItems)
                .setOnMenuItemClickListener(new TopRightMenu.OnMenuItemClickListener() {
                    @Override
                    public void onMenuItemClick(int position) {
                        Bundle bundle;
                        Intent intent;
                        switch (index) {
                            case 0://报警限设置
                                //<editor-fold desc="报警限设置">
                                switch (position) {
                                    case 0:
                                        intent = new Intent(MainActivity.this, AlertActivity.class);
                                        bundle = new Bundle();
                                        bundle.putString("title", "心率");
                                        bundle.putString("name", "Ecg");
                                        bundle.putString("limit_H", txtEcgHigh.getText().toString());
                                        bundle.putString("limit_L", txtEcgLow.getText().toString());
                                        intent.putExtras(bundle);
                                        startActivity(intent);
                                        break;
                                    case 1:
                                        intent = new Intent(MainActivity.this, AlertActivity.class);
                                        bundle = new Bundle();
                                        bundle.putString("title", "血压");
                                        bundle.putString("name", "Nibp");
                                        bundle.putString("limit_H", txtNibpHigh.getText().toString());
                                        bundle.putString("limit_L", txtNibpLow.getText().toString());
                                        intent.putExtras(bundle);
                                        startActivity(intent);
                                        break;
                                    case 2:
                                        intent = new Intent(MainActivity.this, AlertActivity.class);
                                        bundle = new Bundle();
                                        bundle.putString("title", "血氧");
                                        bundle.putString("name", "Spo2");
                                        bundle.putString("limit_H", txtSpo2High.getText().toString());
                                        bundle.putString("limit_L", txtSpo2Low.getText().toString());
                                        intent.putExtras(bundle);
                                        startActivity(intent);
                                        break;
                                    case 3:
                                        intent = new Intent(MainActivity.this, AlertActivity.class);
                                        bundle = new Bundle();
                                        bundle.putString("title", "脉率");
                                        bundle.putString("name", "Pulse");
                                        bundle.putString("limit_H", txtPulseHigh.getText().toString());
                                        bundle.putString("limit_L", txtPulseLow.getText().toString());
                                        intent.putExtras(bundle);
                                        startActivity(intent);
                                        break;
                                    case 4:
                                        intent = new Intent(MainActivity.this, AlertActivity.class);
                                        bundle = new Bundle();
                                        bundle.putString("title", "呼吸");
                                        bundle.putString("name", "Resp");
                                        bundle.putString("limit_H", txtRespHigh.getText().toString());
                                        bundle.putString("limit_L", txtRespLow.getText().toString());
                                        intent.putExtras(bundle);
                                        startActivity(intent);
                                        break;
                                    case 5:
                                        intent = new Intent(MainActivity.this, AlertActivity.class);
                                        bundle = new Bundle();
                                        bundle.putString("title", "体温");
                                        bundle.putString("name", "Temp");
                                        bundle.putString("limit_H", txtTempHigh.getText().toString());
                                        bundle.putString("limit_L", txtTempLow.getText().toString());
                                        intent.putExtras(bundle);
                                        startActivity(intent);
                                        break;
                                }
                                //</editor-fold>
                                break;
                            case 1://维护设置
                                //<editor-fold desc="维护设置">
                                switch (position) {
                                    case 0://漏气检测
                                        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                                        view = inflater.inflate(R.layout.leak_detect, null);
                                        final TextView thre = (TextView) view.findViewById(R.id.thre);
                                        final Button thre_up = (Button) view.findViewById(R.id.up);
                                        final Button thre_down = (Button) view.findViewById(R.id.down);
                                        final Button start_detect = (Button) view.findViewById(R.id.start_detect);

                                        AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
                                        ad.setView(view);
                                        ad.setTitle("漏气检测");


                                        thre_up.setOnClickListener(new Button.OnClickListener() {//创建监听
                                            public void onClick(View v) {
                                                thre.setText(Integer.parseInt(thre.getText().toString()) + 10 + "");
                                            }

                                        });

                                        thre_down.setOnClickListener(new Button.OnClickListener() {//创建监听
                                            public void onClick(View v) {
                                                thre.setText(Integer.parseInt(thre.getText().toString()) - 10 + "");
                                            }

                                        });

                                        start_detect.setOnClickListener(new Button.OnClickListener() {//创建监听
                                            public void onClick(View v) {
                                                send(new byte[0]);

                                            }

                                        });


                                        selfdialog = ad.create();
                                        selfdialog.setButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog, final int which) {
                                                send(new byte[0]);
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.setButton2("确定", new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                send(new byte[0]);
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.show();
                                        break;
                                    case 1://静态压力校准
                                        ad = new AlertDialog.Builder(MainActivity.this);
                                        ad.setView(bp_stat_view);
                                        ad.setTitle("静态压力校准");


                                        selfdialog = ad.create();
                                        send(new byte[0]);
                                        selfdialog.setButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog, final int which) {
                                                send(new byte[0]);
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.setButton2("确定", new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                send(new byte[0]);
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.show();
                                        break;
                                    case 2://压力参数校准
                                        inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                                        view = inflater.inflate(R.layout.bp_param_cali, null);


                                        ad = new AlertDialog.Builder(MainActivity.this);
                                        ad.setView(view);
                                        ad.setTitle("血压参数校准");


                                        selfdialog = ad.create();
                                        send(new byte[0]);
                                        selfdialog.setButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog, final int which) {
                                                send(new byte[0]);
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.setButton2("确定", new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                send(new byte[0]);
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.show();
                                        break;
                                    case 3://日期时间设置
                                        break;
                                    case 4://服务器IP设置
                                        //<editor-fold desc="服务器IP设置">
                                        inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                                        view = inflater.inflate(R.layout.ip_setting_win, null);

                                        final EditText ip_field = view.findViewById(R.id.editText1);
                                        ad = new AlertDialog.Builder(MainActivity.this);
                                        ad.setView(view);
                                        ad.setTitle("服务器IP设置");
                                        //获取IP地址
                                        SharedPreferences read = getSharedPreferences("setting", MODE_PRIVATE);
                                        ip = read.getString("IP", ip);
                                        ip_field.setText(ip);
                                        selfdialog = ad.create();
                                        selfdialog.setButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog, final int which) {
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.setButton2("设置", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String code = ip_field.getText().toString().trim();
                                                ip = code;
                                                SharedPreferences.Editor editor = getSharedPreferences("setting", MODE_PRIVATE).edit();
                                                editor.putString("IP", code);
                                                editor.apply();
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.show();
                                        //</editor-fold>
                                        break;
                                }
                                //</editor-fold>
                                break;
                            case 2://心电设置
                                //<editor-fold desc="心电设置">
                                switch (position) {
                                    case 0://心电滤波模式
                                        //<editor-fold desc="心电滤波模式">
                                        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                                        view = inflater.inflate(R.layout.hr_filter_setting, null);
                                        final RadioGroup hr_filter_setting_field = (RadioGroup) view.findViewById(R.id.radioGroup1);
                                        AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
                                        ad.setView(view);
                                        ad.setTitle("心电滤波模式设置");
                                        String ss = getParamSettings("ecg_filter_mode");
                                        if (ss != null)
                                            if (Integer.parseInt(ss) >= 0)
                                                ((RadioButton) (hr_filter_setting_field.getChildAt(Integer.parseInt(ss)))).setChecked(true);

                                        selfdialog = ad.create();
                                        selfdialog.setButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog, final int which) {
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.setButton2("设置", new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                for (int i = 0; i < hr_filter_setting_field.getChildCount(); i++)
                                                    if (((RadioButton) hr_filter_setting_field.getChildAt(i)).isChecked()) {
                                                        switch (i) {
                                                            case 0:
                                                                monitorData.send(Directive.Ecg_S_Filter4);
                                                                break;
                                                            case 1:
                                                                monitorData.send(Directive.Ecg_S_Filter2);
                                                                break;
                                                            case 2:
                                                                monitorData.send(Directive.Ecg_S_Filter1);
                                                                break;

                                                        }
                                                        setParamSetting("ecg_filter_mode", i + "");
                                                        break;
                                                    }
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.show();
                                        //</editor-fold>
                                        break;
                                    case 1://心电波形增益
                                        //<editor-fold desc="心电波形增益">
                                        inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                                        view = inflater.inflate(R.layout.hr_mag_setting, null);
                                        final RadioGroup ecg_mag_setting_field = (RadioGroup) view.findViewById(R.id.radioGroup1);
                                        ad = new AlertDialog.Builder(MainActivity.this);
                                        ad.setView(view);
                                        ad.setTitle("心电波形增益设置");
                                        ss = getParamSettings("ecg_mag");
                                        if (ss != null)
                                            if (Integer.parseInt(ss) >= 0)
                                                ((RadioButton) (ecg_mag_setting_field.getChildAt(Integer.parseInt(ss)))).setChecked(true);

                                        //ecg_mag_setting_field.setId(Integer.parseInt(ecg_mag));
                                        selfdialog = ad.create();
                                        selfdialog.setButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog, final int which) {
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.setButton2("设置", new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                for (int i = 0; i < ecg_mag_setting_field.getChildCount(); i++)
                                                    if (((RadioButton) ecg_mag_setting_field.getChildAt(i)).isChecked()) {
                                                        switch (i) {
                                                            case 0:
                                                                monitorData.send(Directive.Ecg_S_Gain_25);
                                                                break;
                                                            case 1:
                                                                monitorData.send(Directive.Ecg_S_Gain_50);
                                                                break;
                                                            case 2:
                                                                monitorData.send(Directive.Ecg_S_Gain_100);
                                                                break;
                                                            case 3:
                                                                monitorData.send(Directive.Ecg_S_Gain_200);
                                                                break;

                                                        }
                                                        setParamSetting("ecg_mag", String.valueOf(i));
                                                        break;
                                                    }
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.show();
                                        //</editor-fold>
                                        break;
                                    case 2://呼吸波形增益
                                        //<editor-fold desc="呼吸波形增益">
                                        inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                                        view = inflater.inflate(R.layout.resp_mag_setting, null);
                                        final RadioGroup resp_mag_setting_field = (RadioGroup) view.findViewById(R.id.radioGroup1);

                                        ad = new AlertDialog.Builder(MainActivity.this);
                                        ad.setView(view);
                                        ad.setTitle("呼吸波形增益设置");
                                        ss = getParamSettings("ecg_mag");
                                        if (ss != null)
                                            if (Integer.parseInt(ss) >= 0)
                                                ((RadioButton) (resp_mag_setting_field.getChildAt(Integer.parseInt(ss)))).setChecked(true);

                                        //resp_mag_setting_field.setId(Integer.parseInt(resp_curve_mag));
                                        selfdialog = ad.create();
                                        selfdialog.setButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog, final int which) {
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.setButton2("设置", new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //setParamSetting("resp_curve_mag",resp_mag_setting_field.getCheckedRadioButtonId()+"");
                                                for (int i = 0; i < resp_mag_setting_field.getChildCount(); i++)
                                                    if (((RadioButton) resp_mag_setting_field.getChildAt(i)).isChecked()) {
                                                        switch (i) {
                                                            case 0:
                                                                send(new byte[0]);
                                                                break;
                                                            case 1:
                                                                send(new byte[0]);
                                                                break;
                                                            case 2:
                                                                send(new byte[0]);
                                                                break;
                                                            case 3:
                                                                send(new byte[0]);
                                                                break;
                                                        }
                                                        setParamSetting("resp_curve_mag", i + "");
                                                        break;
                                                    }
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.show();
                                        //</editor-fold>
                                        break;
                                    case 3://心电导联切换
                                        //<editor-fold desc="心电导联切换">
                                        inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                                        view = inflater.inflate(R.layout.hr_ch_setting, null);
                                        final RadioGroup hr_channel_setting_field = (RadioGroup) view.findViewById(R.id.radioGroup1);
                                        ad = new AlertDialog.Builder(MainActivity.this);
                                        ad.setView(view);
                                        ad.setTitle("心电导联切换设置");
                                        ss = getParamSettings("ecg_channel");
                                        if (ss != null)
                                            if (Integer.parseInt(ss) >= 0)
                                                ((RadioButton) (hr_channel_setting_field.getChildAt(Integer.parseInt(ss)))).setChecked(true);
                                        selfdialog = ad.create();
                                        selfdialog.setButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog, final int which) {
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.setButton2("设置", new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                for (int i = 0; i < hr_channel_setting_field.getChildCount(); i++)
                                                    if (((RadioButton) hr_channel_setting_field.getChildAt(i)).isChecked()) {
                                                        switch (i) {
                                                            case 0:
                                                                monitorData.send(Directive.Ecg_3_LeadMode);
                                                                break;
                                                            case 1:
                                                                monitorData.send(Directive.Ecg_5_LeadMode);
                                                                break;
                                                            case 2:
                                                                break;
                                                            case 3:
                                                                break;

                                                        }
                                                        setParamSetting("ecg_channel", i + "");
                                                        break;
                                                    }
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.show();
                                        //</editor-fold>
                                        break;
                                }
                                //</editor-fold>
                                break;
                            case 3://血压设置
                                //<editor-fold desc="血压设置">
                                switch (position) {
                                    case 0://自动检测设置
                                        //<editor-fold desc="自动检测设置">
                                        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                                        view = inflater.inflate(R.layout.bp_auto_setting, null);
                                        final ToggleButton bp_auto_switch_field = view.findViewById(R.id.toggleButton1);
                                        final RadioGroup bp_auto_inter_field = view.findViewById(R.id.radioGroup1);
                                        AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
                                        ad.setView(view);
                                        ad.setTitle("血压自动测量设置");

                                        if (Global.bp_auto_switch.equals("on"))
                                            bp_auto_switch_field.setChecked(true);
                                        else
                                            bp_auto_switch_field.setChecked(false);
                                        String ss = getParamSettings("bp_auto_inter");
                                        if (ss != null)
                                            if (Integer.parseInt(ss) >= 0)
                                                ((RadioButton) (bp_auto_inter_field.getChildAt(Integer.parseInt(ss)))).setChecked(true);
                                        selfdialog = ad.create();
                                        selfdialog.setButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog, final int which) {
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.setButton2("设置", new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (bp_auto_switch_field.isChecked()) {
                                                    setParamSetting("bp_auto_switch", "on");
                                                    Global.bp_auto_switch = "on";
                                                } else {
                                                    setParamSetting("bp_auto_switch", "off");
                                                    Global.bp_auto_switch = "off";
                                                }
                                                for (int i = 0; i < bp_auto_inter_field.getChildCount(); i++)
                                                    if (((RadioButton) bp_auto_inter_field.getChildAt(i)).isChecked()) {
                                                        setParamSetting("bp_auto_inter", String.valueOf(i));
                                                        //发送血压命令
                                                        switch (i) {
                                                            case 0:
                                                                monitorData.send(Directive.Bp_S_Mode_5);
                                                                break;
                                                            case 1:
                                                                monitorData.send(Directive.Bp_S_Mode_10);
                                                                break;
                                                            case 2:
                                                                monitorData.send(Directive.Bp_S_Mode_30);
                                                                break;
                                                            case 3:
                                                                monitorData.send(Directive.Bp_S_Mode_60);
                                                                break;

                                                        }
                                                        break;
                                                    }


                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.show();
                                        //</editor-fold>
                                        break;
                                    case 1://预充气压力
                                        //<editor-fold desc="预充气压力">
                                        inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                                        view = inflater.inflate(R.layout.bp_prefill_setting, null);

                                        ad = new AlertDialog.Builder(MainActivity.this);
                                        ad.setView(view);
                                        ad.setTitle("预充气压力设置");

                                        final TextView cr_prefill_param = view.findViewById(R.id.cr);
                                        final TextView et_prefill_param = view.findViewById(R.id.et);
                                        final TextView ye_prefill_param = view.findViewById(R.id.ye);

                                        final Button cr_up = view.findViewById(R.id.crup);
                                        final Button cr_down = view.findViewById(R.id.crdown);

                                        final Button et_up = view.findViewById(R.id.etup);
                                        final Button et_down = view.findViewById(R.id.etdown);

                                        final Button ye_up = view.findViewById(R.id.yeup);
                                        final Button ye_down = view.findViewById(R.id.yedown);

                                        cr_up.setOnClickListener(new Button.OnClickListener() {//创建监听
                                            public void onClick(View v) {
                                                if (Integer.parseInt(cr_prefill_param.getText().toString()) + 10 <= 280)
                                                    cr_prefill_param.setText(Integer.parseInt(cr_prefill_param.getText().toString()) + 10 + "");
                                            }

                                        });

                                        cr_down.setOnClickListener(new Button.OnClickListener() {//创建监听
                                            public void onClick(View v) {
                                                if (Integer.parseInt(cr_prefill_param.getText().toString()) - 10 >= 80)
                                                    cr_prefill_param.setText(Integer.parseInt(cr_prefill_param.getText().toString()) - 10 + "");
                                            }

                                        });


                                        et_up.setOnClickListener(new Button.OnClickListener() {//创建监听
                                            public void onClick(View v) {
                                                if (Integer.parseInt(et_prefill_param.getText().toString()) + 20 <= 200)
                                                    et_prefill_param.setText(Integer.parseInt(et_prefill_param.getText().toString()) + 10 + "");
                                            }

                                        });

                                        et_down.setOnClickListener(new Button.OnClickListener() {//创建监听
                                            public void onClick(View v) {
                                                if (Integer.parseInt(et_prefill_param.getText().toString()) - 20 >= 80)
                                                    et_prefill_param.setText(Integer.parseInt(et_prefill_param.getText().toString()) - 10 + "");
                                            }

                                        });


                                        ye_up.setOnClickListener(new Button.OnClickListener() {//创建监听
                                            public void onClick(View v) {
                                                if (Integer.parseInt(ye_prefill_param.getText().toString()) + 20 <= 120)
                                                    ye_prefill_param.setText(Integer.parseInt(ye_prefill_param.getText().toString()) + 10 + "");
                                            }

                                        });

                                        ye_down.setOnClickListener(new Button.OnClickListener() {//创建监听
                                            public void onClick(View v) {
                                                if (Integer.parseInt(ye_prefill_param.getText().toString()) - 20 >= 60)
                                                    ye_prefill_param.setText(Integer.parseInt(ye_prefill_param.getText().toString()) - 10 + "");
                                            }

                                        });


                                        selfdialog = ad.create();
                                        selfdialog.setButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog, final int which) {
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.setButton2("设置", new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                byte[] data1 = Directive.Bp_S_Pressur;
                                                byte[] data2 = Directive.Bp_S_Pressur;
                                                byte[] data3 = Directive.Bp_S_Pressur;
                                                data1[8] = (byte) Integer.parseInt(cr_prefill_param.getText().toString());
                                                data1[9] = (byte) monitor.CheckNum(data1);
                                                data2[8] = (byte) Integer.parseInt(et_prefill_param.getText().toString());
                                                data2[9] = (byte) monitor.CheckNum(data2);
                                                data3[8] = (byte) Integer.parseInt(ye_prefill_param.getText().toString());
                                                data3[9] = (byte) monitor.CheckNum(data3);
                                                //发送命令
                                                monitorData.send(data1);
                                                monitorData.send(data2);
                                                monitorData.send(data3);
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.show();
                                        //</editor-fold>
                                        break;
                                    case 2://血压模式设置
                                        //<editor-fold desc="血压模式设置">
                                        inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                                        view = inflater.inflate(R.layout.bp_mode_setting, null);

                                        final RadioGroup bp_mode_field = (RadioGroup) view.findViewById(R.id.radioGroup1);
                                        ad = new AlertDialog.Builder(MainActivity.this);
                                        ad.setView(view);
                                        ad.setTitle("血压测量模式设置");
                                        ss = getParamSettings("bp_mode");
                                        if (ss != null)
                                            if (!ss.equals(""))
                                                if (Integer.parseInt(ss) >= 0)
                                                    ((RadioButton) (bp_mode_field.getChildAt(Integer.parseInt(ss)))).setChecked(true);

                                        selfdialog = ad.create();
                                        selfdialog.setButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(final DialogInterface dialog, final int which) {
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.setButton2("设置", new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                for (int i = 0; i < bp_mode_field.getChildCount(); i++)
                                                    if (((RadioButton) bp_mode_field.getChildAt(i)).isChecked()) {
                                                        switch (i) {
                                                            case 0:
                                                                monitorData.send(Directive.SpO2_S_Patient1);
                                                                break;
                                                            case 1:
                                                                monitorData.send(Directive.SpO2_S_Patient2);
                                                                break;
                                                            case 2:
                                                                monitorData.send(Directive.SpO2_S_Patient3);
                                                                break;
                                                        }
                                                        setParamSetting("bp_mode", String.valueOf(i));
                                                        break;
                                                    }
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.show();
                                        //</editor-fold>
                                        break;
                                }
                                //</editor-fold>
                                break;
                        }
                    }
                })
                .showAsDropDown(txtBioAlarm, -5, -100);
    }

    //设置参数
    private void setParamSetting(String name, String Value) {
        SharedPreferences.Editor editor = getSharedPreferences("setting", MODE_PRIVATE).edit();
        editor.putString(name, Value);
        editor.apply();
    }

    //获取参数设置
    private String getParamSettings(String name) {
        //获取IP地址
        SharedPreferences read = getSharedPreferences("setting", MODE_PRIVATE);
        String str = read.getString(name, null);
        return str;
    }

    private AlertDialog selfdialog;
    private View view;

    //<editor-fold desc="广播用于接收上下限设置">
    //广播用于接收上下限设置
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String name = intent.getExtras().getString("name");
            Log.e("TAG12", name);
            switch (name) {
                case "Ecg":
                    txtEcgHigh.setText(intent.getExtras().getString("limitH"));
                    txtEcgLow.setText(intent.getExtras().getString("limitL"));
                    break;
                case "Nibp":
                    txtNibpHigh.setText(intent.getExtras().getString("limitH"));
                    txtNibpLow.setText(intent.getExtras().getString("limitL"));
                    break;
                case "SpO2":
                    txtSpo2High.setText(intent.getExtras().getString("limitH"));
                    txtSpo2Low.setText(intent.getExtras().getString("limitL"));
                    break;
                case "Pulse":
                    txtPulseHigh.setText(intent.getExtras().getString("limitH"));
                    txtPulseLow.setText(intent.getExtras().getString("limitL"));
                    break;
                case "Resp":
                    txtRespHigh.setText(intent.getExtras().getString("limitH"));
                    txtRespLow.setText(intent.getExtras().getString("limitL"));
                    break;
                case "Temp":
                    txtTempHigh.setText(intent.getExtras().getString("limitH"));
                    txtTempLow.setText(intent.getExtras().getString("limitL"));
                    break;
            }
        }
    };

    //</editor-fold>

    @OnClick(R.id.txt_bio_alarm)
    public void onViewClicked() {
        mTopRightMenu.showAsDropDown(txtBioAlarm, -5, -100);

    }

    byte[] bytes = null;

    private class MonitorData implements Runnable {

        public void send(byte[] data) {
            try {
                outputStream.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(10);
                    if (!socketStatus) {
                        socket = new Socket(ip, SERVER_PORT);
                        outputStream = socket.getOutputStream();
                        inputStream = socket.getInputStream();
                        socketStatus = true;

                        send(Directive.Ecg_Handshake);
                        send(Directive.Bp_Handshake);
                        send(Directive.SpO2_Handshake);
                    }
                    if (inputStream.available() > 0) {
                        bytes = new byte[inputStream.available()];
                        int num = inputStream.read(bytes);

                        monitor.CmdParser(bytes);
                        Monitor.Hr_Curve hr_curve = monitor.getHrCurve();
                        if (hr_curve.HR_I.size() > 0) {
                            for (int i = 0; i < hr_curve.HR_I.size(); i+=1) {
                                ecgCurve1.setCurve(hr_curve.HR_I.get(i));
                                ecgCurve2.setCurve(hr_curve.HR_II.get(i));
                                ecgCurve3.setCurve(hr_curve.HR_V1.get(i));
                                respCurve.setCurve(hr_curve.HR_RESP.get(i));
                                //Log.e(i + " num", String.valueOf(hr_curve.HR_RESP.get(i)));
                            }

                        }

                        if (monitor.getSpo2_Curve().size() > 0) {
                            for (int i : monitor.getSpo2_Curve()) {
                                spo2Curve.setCurve(i);
                            }
                        }
                        String TmpStr = new String(bytes, "gb2312");

                        mHandler.sendEmptyMessage(0x02);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //发送命令
    public static void send(byte[] data) {

        if (data != null) {
            if (socketStatus) {
                try {
                    outputStream.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            Thread thread = new Thread() {
//                @Override
//                public void run() {
//                    super.run();
//                    if (socketStatus) {
//                        try {
//                            outputStream.write(finalData);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            };
//
//            thread.start();
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

    //<editor-fold desc="报警">

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

    private void Alarm() {
        //心电报警
        AddAlarmInfo(txtEcg, txtEcgHigh, txtEcgLow, Global.ecg_alarm, "心电");
        //血压报警
        AddAlarmInfo(txtNibpH, txtNibpHigh, txtNibpLow, Global.nibp_alarm, "收缩压");
        //血压报警
        AddAlarmInfo(txtNibpL, txtNibpHigh, txtNibpLow, Global.nibp_alarm, "舒张压");
        //血氧报警
        AddAlarmInfo(txtSpo2, txtSpo2High, txtSpo2Low, Global.spo2_alarm, "血氧");
        //脉率报警
        AddAlarmInfo(txtPulse, txtPulseHigh, txtPulseLow, Global.pulse_alarm, "脉率");
        //呼吸率报警
        AddAlarmInfo(txtResp, txtRespHigh, txtRespLow, Global.resp_alarm, "呼吸率");
        //体温报警
        AddAlarmInfo(txtTemp, txtTempHigh, txtTempLow, Global.temp_alarm, "体温");

        if (mSafeAlertMessage.size() > 0) {
            txtBioAlarm.setText(mSafeAlertMessage.get(0));
            //Log.e("生理",mSafeAlertMessage.get(0));
            mSafeAlertMessage.remove(0);
            Global.isAlarm1 = true;
        } else {
            txtBioAlarm.setText("");
            Global.isAlarm1 = false;
        }
        if (mTechnologyMessage.size() > 0) {
            txtTechAlarm.setText(mTechnologyMessage.get(0));
            //Log.e("技术",mTechnologyMessage.get(0));
            mTechnologyMessage.remove(0);
            Global.isAlarm2 = true;
        } else {
            txtTechAlarm.setText("");
            Global.isAlarm2 = false;
        }

        if (Global.isAlarm1 || Global.isAlarm2)
            AlarmVoice();

    }

    private static void AlarmVoice() {
        soundPool.play(loadId, 1, 1, 1, 0, 1f);
    }
    //</editor-fold>

    private class MyHandler extends Handler {
        //注意下面的“MainActivity”类是MyHandler类所在的外部类，即所在的activity
        WeakReference<MainActivity> mActivity;

        MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity theActivity = mActivity.get();
            switch (msg.what) { //此处可以根据what的值处理多条信息
                case 0x01://时间
                    //format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    format = new SimpleDateFormat("yyyy年MM月dd日HH时mm分", Locale.getDefault());
                    theActivity.txtTime.setText(format.format(new Date()));
                    Alarm();
                    if (socketStatus) {
                        monitorData.send(Directive.Bp_Q_State);
                    }
                    break;
                case 0x02://监护数据
                    Monitor.ECG_Message ecg_message = monitor.getEcg_message();

                    if (!(" 断开").equals(ecg_message.Lead_State)) {
                        //mTechnologyMessage.add("");
                        if (!mTechnologyMessage.contains(ecg_message.Lead_State))
                            mTechnologyMessage.add(ecg_message.Lead_State);
                    }
                    Monitor.Spo2_Data spo2_data = monitor.getSpo2_data();

                    if (!("").equals(spo2_data.State)) {
                        if (!mTechnologyMessage.contains(spo2_data.State))
                            mTechnologyMessage.add(spo2_data.State);
                    }
                    //心率
                    if (monitor.getEcg_data().Data_Hr != 65436) {
                        theActivity.txtEcg.setText(String.valueOf(monitor.getEcg_data().Data_Hr));
                    } else {
                        theActivity.txtEcg.setText("--");
                    }
                    //呼吸
                    if (monitor.getEcg_data().Data_Resp != 65436) {
                        theActivity.txtResp.setText(String.valueOf(monitor.getEcg_data().Data_Resp));
                    } else {
                        theActivity.txtResp.setText("--");
                    }
                    //体温
                    if (monitor.getEcg_data().Data_Temp1 != 55f) {
                        theActivity.txtTemp.setText(String.valueOf(String.valueOf(monitor.getEcg_data().Data_Temp1)));
                    } else {
                        theActivity.txtTemp.setText("--");
                    }
                    //收缩压
                    if (monitor.getBp_info().Bp_H < 300) {
                        theActivity.txtNibpH.setText(String.valueOf(monitor.getBp_info().Bp_H));
                    } else {
                        theActivity.txtNibpH.setText("--");
                    }
                    //舒张压
                    if (monitor.getBp_info().Bp_L < 300) {
                        theActivity.txtNibpL.setText(String.valueOf(monitor.getBp_info().Bp_L));
                    } else {
                        theActivity.txtNibpL.setText("--");
                    }
                    //平均压
                    if (monitor.getBp_info().Bp_Avg < 300) {
                        theActivity.txtBpAvg.setText(String.valueOf(monitor.getBp_info().Bp_Avg));
                    } else {
                        theActivity.txtBpAvg.setText("--");
                    }
                    //血氧
                    if (monitor.getSpo2_data().Spo2_Value != 127) {
                        theActivity.txtSpo2.setText(String.valueOf(monitor.getSpo2_data().Spo2_Value));
                    } else {
                        theActivity.txtSpo2.setText("--");
                    }
                    //脉率
                    if (monitor.getSpo2_data().Pulse_Value != 511) {
                        theActivity.txtPulse.setText(String.valueOf(monitor.getSpo2_data().Pulse_Value));
                    } else {
                        theActivity.txtPulse.setText("--");
                    }
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
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
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import util.Global;
import util.Monitor;
import util.MySurfaceView;

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
    public static String ip = "192.168.88.126";    //服务器IP
    //服务器端口
    private static final int SERVER_PORT = 8899;

    private boolean socketStatus = false;
    public static List<String> cmd_list = new ArrayList<String>();
    private OutputStream outputStream = null;
    private InputStream inputStream = null;
    //</editor-fold>


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUIMenu();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initMenu();

        init();
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
        read.getString("IP", ip);

        //new Thread(new AlarmAndTime()).start();

        //new Thread(new MonitorData()).start();
        IntentFilter filter = new IntentFilter(AlertActivity.action);
        registerReceiver(broadcastReceiver, filter);
    }

    private void initMenu() {
        mTopRightMenu = new TopRightMenu(MainActivity.this);
        List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem(R.mipmap.ico, "报警设置"));
        menuItems.add(new MenuItem(R.mipmap.ico, "维护设置"));
        menuItems.add(new MenuItem(R.mipmap.ico, "心电设置"));
        mTopRightMenu
                .setHeight(270)     //默认高度480
                .setWidth(300)      //默认宽度wrap_content
                .setAnimationStyle(R.style.TRM_ANIM_STYLE)  //默认为R.style.TRM_ANIM_STYLE
                .addMenuList(menuItems)
                .addMenuItem(new MenuItem(R.mipmap.ico, "血压设置"))
                .addMenuItem(new MenuItem(R.mipmap.ico, "启动血压测量"))
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
                            case 4:
                                break;
                        }
                        Toast.makeText(MainActivity.this, "点击菜单:" + position, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public static String hr_alert_switch="on";            //心率报警开关
    public static int hr_alert_high, hr_alert_low;    //心率报警上限 /心率报警下限
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
                        switch (index){
                            case 0:
                                //<editor-fold desc="上下限设置">
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
                            case 1:
                                switch (position){
                                    case 0://漏气检测
                                        //创建view从当前activity获取loginactivity
                                        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                                        view = inflater.inflate(R.layout.hr_alert_param, null);
                                        // Spinner spinner = (Spinner)view.findViewById(R.id.xdlb);
                                        final ToggleButton hr_alert_switch_field = (ToggleButton) view.findViewById(R.id.ToggleButton1);
                                        final TextView hr_alert_high_field = (TextView) view.findViewById(R.id.textView1);
                                        final TextView hr_alert_low_field = (TextView) view.findViewById(R.id.textView2);
                                        final Button hr_alert_high_field_up = (Button) view.findViewById(R.id.sxtj);
                                        final Button hr_alert_high_field_down = (Button) view.findViewById(R.id.sxzs);

                                        final Button hr_alert_low_field_up = (Button) view.findViewById(R.id.xxtj);
                                        final Button hr_alert_low_field_down = (Button) view.findViewById(R.id.xxzs);
                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, new String[]{"手术模式", "监护模式", "诊断模式"});
                                        //spinner.setAdapter(adapter);
                                        AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
                                        ad.setView(view);
                                        ad.setTitle("心率报警设置");

                                        if (hr_alert_switch.equals("on"))
                                            hr_alert_switch_field.setChecked(true);
                                        else
                                            hr_alert_switch_field.setChecked(false);
                                        //String s = (String) hr_alert_high_field.getText();
                                        hr_alert_high_field.setText(hr_alert_high + "");
                                        hr_alert_low_field.setText(hr_alert_low + "");

                                        hr_alert_high_field_up.setOnClickListener(new Button.OnClickListener() {//创建监听
                                            public void onClick(View v) {
                                                hr_alert_high_field.setText(Integer.parseInt(hr_alert_high_field.getText().toString()) + 1 + "");
                                            }
                                        });

                                        hr_alert_high_field_down.setOnClickListener(new Button.OnClickListener() {//创建监听
                                            public void onClick(View v) {
                                                if (Integer.parseInt(hr_alert_high_field.getText().toString()) - 1 >= Integer.parseInt(hr_alert_low_field.getText().toString()))
                                                    hr_alert_high_field.setText(Integer.parseInt(hr_alert_high_field.getText().toString()) - 1 + "");
                                            }
                                        });

                                        hr_alert_low_field_up.setOnClickListener(new Button.OnClickListener() {//创建监听
                                            public void onClick(View v) {
                                                if (Integer.parseInt(hr_alert_low_field.getText().toString()) + 1 <= Integer.parseInt(hr_alert_high_field.getText().toString()))
                                                    hr_alert_low_field.setText(Integer.parseInt(hr_alert_low_field.getText().toString()) + 1 + "");
                                            }
                                        });

                                        hr_alert_low_field_down.setOnClickListener(new Button.OnClickListener() {//创建监听
                                            public void onClick(View v) {
                                                if (Integer.parseInt(hr_alert_low_field.getText().toString()) - 1 >= 0)
                                                    hr_alert_low_field.setText(Integer.parseInt(hr_alert_low_field.getText().toString()) - 1 + "");
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
                                                txtEcgHigh.setText(hr_alert_high_field.getText());
                                                selfdialog.cancel();
                                            }
                                        });
                                        selfdialog.show();
                                        break;
                                    case 1://静态压力校准
                                        break;
                                    case 2://压力参数校准
                                        break;
                                    case 3://日期时间设置
                                        break;
                                    case 4://服务器IP设置
//                                        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
//                                        view = inflater.inflate(R.layout.ip_setting_win, null);
//
//                                        final EditText ip_field = (EditText) view.findViewById(R.id.editText1);
//                                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, new String[]{"开", "关"});
//
//                                        AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
//                                        ad.setView(view);
//                                        ad.setTitle("服务器IP设置");
//                                        ip_field.setText(ip);
//                                        selfdialog = ad.create();
//                                        selfdialog.setButton("取消", new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(final DialogInterface dialog, final int which) {
//                                                selfdialog.cancel();
//                                            }
//                                        });
//                                        selfdialog.setButton2("设置", new DialogInterface.OnClickListener() {
//
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which) {
//                                                String code = ip_field.getText().toString().trim();
//                                                SharedPreferences.Editor editor = getSharedPreferences("setting", MODE_PRIVATE).edit();
//                                                editor.putString("IP", code);
//                                                editor.apply();
//
//                                                selfdialog.cancel();
//                                            }
//                                        });
//                                        selfdialog.show();
                                        break;
                                }
                                break;
                            case 2:
                                switch (position){
                                    case 0://心电滤波模式
                                        break;
                                    case 1://心电波形增益
                                        break;
                                    case 2://心电导联切换
                                        break;
                                }
                                break;
                            case 3:
                                switch (position){
                                    case 0://自动检测设置
                                        break;
                                    case 1://预充气压力
                                        break;
                                    case 2://血压模式设置
                                        break;
                                }
                                break;
                        }


                    }
                })
                .showAsDropDown(txtBioAlarm, -5, -100);
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


    private class MonitorData implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(50);
                    if (!socketStatus) {
                        socket = new Socket(ip, 8234);
                        outputStream = socket.getOutputStream();
                        inputStream = socket.getInputStream();
                        socketStatus = true;
                    }
                    if (inputStream.available() > 0) {
                        byte[] bytes = new byte[inputStream.available()];
                        int num = inputStream.read(bytes);
                        List<Byte> buffer = new ArrayList<>();
                        for (int i = 0; i < bytes.length; i++) {
                            buffer.add(bytes[i]);
                        }
                        monitor.CmdParser(buffer);
                        Monitor.Hr_Curve hr_curve = monitor.getHrCurve();
                        if (hr_curve.HR_I.size() > 0) {
                            for (int i = 0; i < hr_curve.HR_I.size(); i++) {
                                ecgCurve1.setCurve(hr_curve.HR_I.get(i));
                                ecgCurve2.setCurve(hr_curve.HR_II.get(i));
                                ecgCurve3.setCurve(hr_curve.HR_V1.get(i));
                                respCurve.setCurve(hr_curve.HR_RESP.get(i));
                            }
                        }
                        if (monitor.getSpo2_Curve().size() > 0) {
                            for (int i = 0; i < monitor.getSpo2_Curve().size(); i++) {
                                spo2Curve.setCurve(monitor.getSpo2_Curve().get(i));
                            }
                        }
                        //String TmpStr = new String(bytes, "gb2312");
                        Log.e(TAG, num + "*" + hr_curve.HR_I.size());
                    }


                    ecgCurve1.setCurve(2048);
                    ecgCurve2.setCurve(2048);
                    ecgCurve3.setCurve(2048);
                    spo2Curve.setCurve(50);
                    respCurve.setCurve(2048);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //发送命令
    public void send(byte[] data) {
        if (data != null) {
            final byte[] finalData = data;
            Thread thread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    if (socketStatus) {
                        try {
                            outputStream.write(finalData);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            };

            thread.start();
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
                    //format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    format = new SimpleDateFormat("yyyy年MM月dd日HH时mm分", Locale.getDefault());
                    theActivity.txtTime.setText(format.format(new Date()));
                    Alarm();
                    //Log.e(TAG, format.format(new Date()) );
                    break;
                case 0x02://监护数据
                    theActivity.txtEcg.setText(monitor.getEcg_data().Data_Hr);
                    theActivity.txtResp.setText(monitor.getEcg_data().Data_Resp);
                    theActivity.txtTemp.setText(String.valueOf(monitor.getEcg_data().Data_Temp1));

                    theActivity.txtNibpH.setText(monitor.getBp_info().Bp_H);
                    theActivity.txtNibpL.setText(monitor.getBp_info().Bp_L);
                    theActivity.txtBpAvg.setText(monitor.getBp_info().Bp_Avg);

                    theActivity.txtSpo2.setText(monitor.getSpo2_data().Spo2_Value);
                    theActivity.txtPulse.setText(monitor.getSpo2_data().Pulse_Value);
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
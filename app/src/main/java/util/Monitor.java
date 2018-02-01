package util;

import android.util.Log;

import com.ecg_monitor.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Witleaf 协议解析类
 * 控制命令包（DC）01
 * 请求命令包（DR）02
 * 命令应答包（DA）03
 * 通用数据包（DD）04
 * <p>
 * Created by YF on 2017/11/17.
 */

public class Monitor {

    private String TAG = "Monitor_Message";

    //<editor-fold desc="心电测量部分（参数类型 0x01）">
    public static byte Ecg_Handshake = 0x01;    // DC 上电握手命令
    public static byte Ecg_Q_Modular = 0x02;    // DR 查询模块信息
    public static byte Ecg_Q_State = 0x03;      // DR 查询模块状态 
    public static byte Ecg_S_Patient = 0x10;    // DC 设置病人类型
    public static byte Ecg_S_LeadMode = 0x20;   // DC 设置心电导联模式
    public static byte Ecg_S_Lead = 0x21;       // DC 设置心电通道导联
    public static byte Ecg_S_Filter = 0x22;     // DC 设置心电滤波方式
    public static byte Ecg_S_TrapWave = 0x23;   // DC 设置心电50/60hz陷波
    public static byte Ecg_S_Gain = 0x24;       // DC 设置心电增益
    public static byte Ecg_S_ST = 0x25;         // DC 设置心电 ST 模板
    public static byte Ecg_S_Channel = 0x26;    // DC 设置心率计算/心律失常分析通道
    public static byte Ecg_PACE = 0x27;         // DC 启动/停止 PACE 检测
    public static byte Ecg_Cal = 0x28;          // DC 启动/停止心电校准
    public static byte Ecg_S_ApneaAlarm = 0x30; // DC 设置呼吸窒息报警时间
    public static byte Ecg_S_RespLead = 0x31;   // DC 设置呼吸导联
    public static byte Ecg_S_Sensitivity = 0x32;// DC 设置呼吸敏感度
    public static byte Ecg_Update = 0x7F;       // DC 启动在线升级
    //</editor-fold>

    //<editor-fold desc="血压测量部分（参数类型 0x02）">
    public static byte[] Bp_Handshake = new byte[]{0x02, 0x01, 0x01};     // DC 上电握手命令
    public static byte[] Bp_Q_Modular = new byte[]{0x02, 0x02, 0x02};     // DR 查询模块信息 0x82
    public static byte[] Bp_Q_State = new byte[]{0x02, 0x02, 0x03};       // DR 查询测试结果和状态 0x83
    public static byte[] Bp_Q_CuffPressure = new byte[]{0x02, 0x01, 0x04};// DC 查询实时袖带压 0x84
    public static byte[] Bp_S_Patient = new byte[]{0x02, 0x01, 0x10};     // DC 设置病人类型
    public static byte[] Bp_S_Pressur = new byte[]{0x02, 0x01, 0x11};     // DC 设置初始充气压力
    public static byte[] Bp_S_Mode = new byte[]{0x02, 0x01, 0x12};        // DC 设置测量模式
    public static byte[] Bp_S_VP = new byte[]{0x02, 0x01, 0x13};          // DC 设置静脉穿刺压力 Venipuncture Pressure
    public static byte[] Bp_Stop = new byte[]{0x02, 0x01, 0x20};          // DC 终止
    public static byte[] Bp_Start = new byte[]{0x02, 0x01, 0x21};         // DC 启动测量
    public static byte[] Bp_Cal = new byte[]{0x02, 0x01, 0x22};           // DC 启动压力校准
    public static byte[] Bp_Leak = new byte[]{0x02, 0x01, 0x23};          // DC 启动漏气检测
    public static byte[] Bp_Venipuncture = new byte[]{0x02, 0x01, 0x24};  // DC 启动静脉穿刺
    public static byte[] Bp_Reset = new byte[]{0x02, 0x01, 0x30};         // DC 复位模块
    public static byte[] Bp_dormancy = new byte[]{0x02, 0x01, 0x31};      // DC 进入休眠模式
    public static byte[] Bp_Watchdog = new byte[]{0x02, 0x01, 0x32};      // DC 启动看门狗自检
    public static byte[] Bp_S_CP = new byte[]{0x02, 0x01, 0x70};          // DC 设置袖带压计算参数 Calculation Parameters
    public static byte[] Bp_S_PP = new byte[]{0x02, 0x01, 0x71};          // DC 设置软件压力保护 Pressure Protection
    public static byte[] Bp_Update = new byte[]{0x02, 0x01, 0x7F};        // DC 在线升级命令
    //</editor-fold>

    //<editor-fold desc="血氧测量部分（参数类型 0x03）">
    public static byte SpO2_Handshake = 0x01;    // DC 上电握手命令
    public static byte SpO2_Q_Modular = 0x02;    // DR 软件、算法及通讯协议 版本查询
    public static byte SpO2_SelfCheck = 0x03;    // DR 模块自检结果查询 0x83
    public static byte SpO2_S_Patient = 0x04;    // DC 病人类型设置
    public static byte SpO2_S_Sensitivity = 0x05;// DC 计算灵敏度设置
    public static byte SpO2_Update = 0x7F;       // DC 在线升级命令
    //</editor-fold>

    private int[] data = new int[10];//解析数据包缓存

    public Hr_Curve getHrCurve() {
        return hrCurve;
    }

    private Hr_Curve hrCurve = new Hr_Curve();


    private List<Integer> buffer = new ArrayList<>();
    public static int id = 0;
    private static List<Integer> ids = new ArrayList<>();//发送过命令的上位机序列号

    private List<Integer> ST_Data = new ArrayList<>();//ST段偏移模版

    public List<Integer> getSpo2_Curve() {
        return spo2_Curve;
    }

    private List<Integer> spo2_Curve = new ArrayList<>();//血氧曲线数据

    public Spo2_Data getSpo2_data() {
        return spo2_data;
    }

    private Spo2_Data spo2_data = new Spo2_Data();

    public ECG_Data getEcg_data() {
        return ecg_data;
    }

    private ECG_Data ecg_data = new ECG_Data();

    public ECG_Module_State getEcg_ModuleState() {
        return ecg_ModuleState;
    }

    private ECG_Module_State ecg_ModuleState = new ECG_Module_State();

    public ECG_Message getEcg_message() {
        return ecg_message;
    }

    private ECG_Message ecg_message = new ECG_Message();

    public Bp_Info getBp_info() {
        return bp_info;
    }

    private Bp_Info bp_info = new Bp_Info();


    List<int[]> AllDaata = new ArrayList<>();

    List<Integer> allnum = new ArrayList<>();

    //将有符号数转换为整形
    public int getUnsignedByte(byte data) {
        return data & 0x0ff;
    }

    public void CmdParser(byte[] bytes) {
        if (bytes == null)
            return;
        hrCurve.Clear();
        spo2_Curve.clear();
        //buffer.clear();

        for (byte aByte : bytes) {
            buffer.add(getUnsignedByte(aByte));
        }
        for (int i = 0; i < buffer.size(); i++) {
            if (buffer.get(i) == 0xFA) {
                if (buffer.size() - i >= 10) {
                    data = GetData(i, buffer.get(i + 1) & 0x0ff, buffer);//buffer.get(i + 1) 此数据包的长度
                    if (data != null && data.length != 0) {
                        i--;
                        if (IsCheckCmd(data))
                            Parser(data);//解析
                    }
                }
            }
        }
        buffer.clear();
        //Log.e("buffer", "" + buffer.size());
    }

    /**
     * 获取命令数组
     *
     * @param i    截取数据包的起始位置
     * @param len  截取数据包的长度
     * @param list 被截取的数组
     * @return 截取到的数组
     */
    private int[] GetData(int i, int len, List<Integer> list) {
        int[] dataPackage = new int[len];
        if (list.size() - i >= len) {
            for (int index = 0; index < len; index++) {
                dataPackage[index] = list.get(i);
                list.remove(i);
            }
        } else {
            dataPackage = null;
        }
        return dataPackage;
    }

    /**
     * 验证命令校验和
     *
     * @param data
     * @return
     */
    private boolean IsCheckCmd(int[] data) {
        int sum = 0;
        for (int i = 1; i < data.length - 1; i++) {
            sum = sum + data[i];
        }
        if ((sum & 0x0ff) == data[data.length - 1]) {
            return true;
        }
        return false;
    }

    //获取校验和
    public int CheckNum(byte[] data) {
        int sum = 0;
        for (int i = 1; i < data.length - 1; i++) {
            sum = sum + data[i];
        }
        return sum & 0x0ff;
    }

    /**
     * 解析数据包
     *
     * @param data 需要解析的数据
     */
    private void Parser(int[] data) {
        //参数类型
        switch (data[2]) {
            case 0x01://ECG
                //<editor-fold desc="ECG">
                switch (data[4]) {
                    case 0x80://通用命令应答包
                        //<editor-fold desc="通用命令应答">
                        switch (data[9]) {
                            case 0x01:
                                //接收到的命令中参数类型错
                                break;
                            case 0x02:
                                //接收到的命令中数据类型错
                                break;
                            case 0x03:
                                //接收到的命令中数据 ID 错
                                break;
                            case 0x04:
                                //接收到的命令中数据段错
                                break;
                            case 0x05:
                                //接收到的命令中序列号段错
                                break;
                            case 0x06:
                                //接收到的命令中校验和错
                                break;
                            case 0x07:
                                //命令执行成功
                                break;
                            case 0x08:
                                //命令执行失败
                                break;
                            case 0x09:
                                //系统状态忙
                                break;
                        }
                        //</editor-fold>
                        break;
                    case 0x81://上电握手请求数据
                        MainActivity.send(Directive.Ecg_Handshake);
                        break;
                    case 0x82://模块信息应答数据

                        //<editor-fold desc="模块信息">
                        int a1 = data[9];//软件主版本号
                        int a2 = data[10];//软件子版本号
                        int a3 = data[11];//软件修订版本号
                        int a4 = data[12];//算法主版本号
                        int a5 = data[13];//算法子版本号
                        int a6 = data[14];//算法修订版本号
                        int a7 = data[15];//协议主版本号
                        int a8 = data[16];//协议子版本号
                        int a9 = data[17];//协议修订版本号
                        int a10 = data[18];//

                        int CPU = data[18] & 0x01;
                        int Register = data[18] >> 1 & 0x01;
                        int RAM = data[18] >> 2 & 0x01;
                        int FLASH = data[18] >> 3 & 0x01;
                        int TIM = data[18] >> 4 & 0x01;
                        int AD = data[18] >> 5 & 0x01;
                        int Watchdog = data[18] >> 6 & 0x01;
                        //</editor-fold>
                        break;
                    case 0x83://模块状态应答数据

                        //<editor-fold desc="模块状态信息">
                        switch (data[9])//病人类型
                        {
                            case 0x00://成人
                                ecg_ModuleState.Patient_Mode = "成人";
                                break;
                            case 0x01://新生儿
                                ecg_ModuleState.Patient_Mode = "新生儿";
                                break;
                            case 0x02://儿童
                                ecg_ModuleState.Patient_Mode = "儿童";
                                break;
                            default:
                                ecg_ModuleState.Patient_Mode = "";
                                break;
                        }
                        int e2 = data[10];
                        switch (data[10])//心电导联模式
                        {
                            case 0x00://3 导联模式

                                break;
                            case 0x01://5 导联模式

                                break;
                            case 0x02://12 导联模式（未使用）

                                break;
                        }
                        switch (data[11] & 0xF)//心电通道 I 的导联
                        {
                            case 0x01://导联 I
                                break;
                            case 0x02://导联 II
                                break;
                            case 0x03://导联 III
                                break;
                            case 0x04://导联 AVR
                                break;
                            case 0x05://导联 AVL
                                break;
                            case 0x06://导联 AVL
                                break;
                        }
                        switch (data[11] >> 3 & 0xF)//心电通道 II 的导联
                        {
                            case 0x01://导联 I
                                break;
                            case 0x02://导联 II
                                break;
                            case 0x03://导联 III
                                break;
                            case 0x04://导联 AVR
                                break;
                            case 0x05://导联 AVL
                                break;
                            case 0x06://导联 AVL
                                break;
                        }
                        int e5 = data[12];
                        switch (data[12])//心电滤波模式
                        {
                            case 0x00://诊断滤波模式
                                break;
                            case 0x01://监护滤波模式
                                break;
                            case 0x02://HARDEST 滤波模式
                                break;
                            case 0x04://手术滤波模式
                                break;
                        }
                        int e7 = data[13] & 0xF;
                        switch (data[13] & 0xF)//50/60hz 陷波方式
                        {
                            case 0x00://设置 50hz 陷波模式
                                break;
                            case 0x01://设置 50hz 陷波模式
                                break;
                            case 0x02://设置 50/60hz 陷波模式
                                break;
                        }
                        int e8 = data[13] >> 3 & 0xF;
                        switch (data[13] >> 3 & 0xF)//50/60hz 陷波方式是否打开
                        {
                            case 0x00://打开
                                break;
                            case 0x01://关闭
                                break;
                        }
                        int e9 = data[14] & 0xF;        //心电通道 I 的增益 
                        int e11 = data[14] >> 3 & 0xF;  //心电通道 II 的增益
                        int e12 = data[15];             //心电通道 V1 的增益
                        switch (data[14] & 0xF)//可以提取方法
                        {
                            case 0x00://x250
                                break;
                            case 0x01://x500
                                break;
                            case 0x02://x1000
                                break;
                            case 0x03://x2000
                                break;
                        }

                        //int e13 = data[16];             //ST 模板的ISO 位置的低 8 位
                        //int e14 = data[17];             //ST 模板的ISO 位置的高 8 位
                        int st_iso = data[16] | (data[17] << 8);
                        //int e15 = data[18];             //ST 模板的 ST 位置的低 8 位
                        //int e16 = data[19];             //ST 模板的 ST 位置的高 8 位
                        int st_st = data[18] | (data[19] << 8);

                        int e17 = data[20] & 0xF;
                        switch (data[20] & 0xF) //心律失常分析通道
                        {
                            case 0x00://通道 I
                                break;
                            case 0x01://通道 II
                                break;
                            case 0x02://通道 V1
                                break;
                        }
                        int e18 = data[20] >> 3 & 0xF;
                        switch (data[20] >> 3 & 0xF)//心律失常分析通道自主选择标志
                        {
                            case 0x00://非自主选择
                                break;
                            case 0x01://自主选择
                                break;
                        }
                        int e19 = data[21];
                        switch (data[21])//PACE 检测标志
                        {
                            case 0x00://停止 PACE 检测
                                break;
                            case 0x01://启动 PACE 检测
                                break;
                        }
                        int e20 = data[22];             //PACE 检测导联
                        switch (data[22])//可以提取方法
                        {
                            case 0x00://导联 I
                                break;
                            case 0x01://导联 II
                                break;
                            case 0x02://导联 III
                                break;
                            case 0x03://导联 AVR
                                break;
                            case 0x04://导联 AVL
                                break;
                            case 0x05://导联 AVL
                                break;
                            case 0x06://导联 V1
                                break;

                        }
                        int e21 = data[23];
                        switch (data[23])//心电校准状态
                        {
                            case 0x00://启动心电校准
                                break;
                            case 0x01://停止心电校准
                                break;
                        }
                        int e22 = data[24];             //呼吸窒息报警时间 （单位：秒）
                        int e23 = data[25];
                        switch (data[25])//呼吸导联
                        {
                            case 0x00://导联 I
                                break;
                            case 0x01://导联 II
                                break;
                        }
                        int e24 = data[26];
                        switch (data[26])//呼吸敏感度
                        {
                            case 0x00://敏感度 1（最不敏感）
                                break;
                            case 0x01://敏感度 2
                                break;
                            case 0x02://敏感度 3
                                break;
                            case 0x03://敏感度 4
                                break;
                            case 0x04://敏感度 5（最敏感）
                                break;
                        }
                        //</editor-fold>
                        break;
                    case 0x90://心电呼吸波形数据  500hz 的频率
                        //9,10,11,12,13,14,15
                        //1,2 ,3 ,4 ,5 ,6 ,7
                        switch (data[9] & 0x01) {
                            case 0x00://未检测到 PACE 标志
                                ecg_message.PACE = false;
                                break;
                            case 0x01://检测到 PACE 标志
                                ecg_message.PACE = true;
                                break;
                        }
                        switch (data[9] >> 4 & 0x01) {
                            case 0x00://未检测到 R 波标志
                                ecg_message.R = false;
                                break;
                            case 0x01://检测到 R 波标志
                                ecg_message.R = true;
                                break;
                        }

                        int hr_i = data[10] | ((data[11] & 0xF) << 8);
                        int hr_ii = (data[11] >> 4) | (data[12] << 4);
                        int hr_v = data[13] | ((data[14] & 0xF) << 8);
                        int hr_r = (data[14] >> 4) | (data[15] << 4);
                        hrCurve.HR_I.add(hr_i);             //心电通道 I 的波形
                        hrCurve.HR_II.add(hr_ii);           //心电通道 II 的波形
                        hrCurve.HR_V1.add(hr_v);            //心电通道 V1 的波形
                        hrCurve.HR_RESP.add(hr_r);          //呼吸波形
                        break;
                    case 0x91://心率/呼吸率数据 如果上传结果为-100，表示未计算得到结果，为无效值 FF9C
                        ecg_data.Data_Hr = data[9] | (data[10] << 8);
                        ecg_data.Data_Resp = data[11] | (data[12] << 8);
                        break;
                    case 0x92://心电导联状态数据包 '1' 有效
                        //<editor-fold desc="心电导联状态">
                        int lead = data[9] & 0x01; //5 导联模式
                        if (lead == 1) {
                            int RL = data[9] >> 1 & 0x01;  //RL 电极断开
                            int V1 = data[9] >> 2 & 0x01;  //V1 电极断开
                            int LL = data[9] >> 3 & 0x01;  //LL 电极断开
                            int LA = data[9] >> 4 & 0x01;  //LA 电极断开
                            int RA = data[9] >> 5 & 0x01;  //RA 电极断开
                            String Lead_State = "";

                            if (RL == 1)
                                Lead_State += " RL";
                            if (V1 == 1)
                                Lead_State += " V1";
                            if (LL == 1)
                                Lead_State += " LL";
                            if (LA == 1)
                                Lead_State += " LA";
                            if (RA == 1)
                                Lead_State += " RA";
                            ecg_message.Lead_State = Lead_State + " 断开";//脱落
                        }


                        lead = data[10] & 0x01; //12 导联模式
                        if (lead == 1) {
                            int V2 = data[10] >> 1 & 0x01; //V2 电极断开
                            int V3 = data[10] >> 2 & 0x01; //V3 电极断开
                            int V4 = data[10] >> 3 & 0x01; //V4 电极断开
                            int V5 = data[10] >> 4 & 0x01; //V5 电极断开
                            int V6 = data[10] >> 5 & 0x01; //V6 电极断开

                            int channel_I = data[11] & 0x01;       //通道 I 信号不存在
                            int channel_II = data[11] >> 1 & 0x01; //通道 II 信号不存在
                            int channel_V1 = data[11] >> 2 & 0x01; //通道 V1 信号不存在
                            int channel_V2 = data[11] >> 3 & 0x01; //通道 V2 信号不存在
                            int channel_V3 = data[11] >> 4 & 0x01; //通道 V3 信号不存在
                            int channel_V4 = data[11] >> 5 & 0x01; //通道 V4 信号不存在
                            int channel_V5 = data[11] >> 6 & 0x01; //通道 V5 信号不存在
                            int channel_V6 = data[11] >> 7 & 0x01; //通道 V6 信号不存在
                        }
                        //</editor-fold>
                        break;
                    case 0x93://心电通道过载标志数据包
                        int overload_I = data[9] & 0x01;      //通道 I 过载
                        int overload_II = data[9] >> 1 & 0x01;//通道 II 过载
                        int overload_V1 = data[9] >> 2 & 0x01;//通道 V1 过载
                        break;
                    case 0x94://心率计算/ 心律失常分析通道数据包
                        switch (data[9]) {
                            case 0x00://通道 I
                                break;
                            case 0x01://通道 II
                                break;
                            case 0x02://通道 V1
                                break;
                            case 0x03://通道 V2
                                break;
                            case 0x04://通道 V3
                                break;
                            case 0x05://通道 V4
                                break;
                            case 0x06://通道 V5
                                break;
                            case 0x07://通道 V6
                                break;
                        }
                        break;
                    case 0x95://心律失常分析起始标志数据包
                        if (data[9] == 0 & data[10] == 0x9C & data[11] == 0xFF) {
                            //开始发送
                        }
                        break;
                    case 0x96://心律失常分析结果数据包
                        //<editor-fold desc="心律失常代码">
                        String result;
                        switch (data[9]) {
                            case 0:
                                result = "ASY";//停搏 asystole
                                break;
                            case 1:
                                result = "VF";//纤维性颤动
                                break;
                            case 2:
                                result = "VTA";//室性心动过速 ventricular tachycardia
                                break;
                            case 3:
                                result = "ROT";//R on T
                                break;
                            case 4:
                                result = "RUN";//多连发室性早搏 Multiple VPB
                                break;
                            case 6:
                                result = "CPT";//二连室性早搏 couple VPB
                                break;
                            case 7:
                                result = "VPB";//偶发室性早搏 accidental VPB
                                break;
                            case 8:
                                result = "BGM";//室早二联律 bigeminy
                                break;
                            case 9:
                                result = "TGM";//室早三联律 trigeminy
                                break;
                            case 10:
                                result = "TAC";//室上性心动过速 supraventricular tachycaridia
                                break;
                            case 11:
                                result = "BRD";//室上心动过缓 supraventricular bradycaridia
                                break;
                            case 12:
                                result = "multiform VPB";//多形 PVC
                                break;
                            case 13:
                                result = "PNC";//起搏器未俘获 pace not capture
                                break;
                            case 14:
                                result = "PNP";//起搏器未起搏 pacer not paced
                                break;
                            case 15:
                                result = "Irregular rhythm";//不规则节律
                                break;
                            case 16:
                                result = "MIS";//漏搏 missed beat
                                break;
                            case 20:
                                //result = "正在学习（LRN）";
                                result = "";
                                break;
                            case 21:
                                //result = "No arrhythmia test";// "未进行心律失常检测";//
                                result = "";
                                break;
                            case 22:
                                result = "正常窦性心律（NML）";
                                break;
                            case 24:
                                result = "NOS";//噪声过大
                                break;
                            default:
                                result = "";
                                break;
                        }
                        ecg_message.Arrhythmia_Result = result;

                        ecg_message.Arrhythmia_Old = data[10] | (data[11] << 8);//上次心律失常位置
                        ecg_message.Arrhythmia_Now = data[10] | (data[11] << 8);//当次心律失常位置
                        //</editor-fold>
                        break;
                    case 0x97://心律失常分析状态数据包
                        //<editor-fold desc="心律失常分析状态">
                        String state = "";
                        switch (data[9]) {
                            case 0x00:
                                state = "正常状态";
                                break;
                            case 0x01:
                                state = "QRS 学习状态";
                                break;
                            case 0x02:
                                state = "心律失常分析学习状态";
                                break;
                            case 0x03:
                                state = "噪声状态";
                                break;
                            case 0x04:
                                state = "未检测状态";
                                break;
                            case 0x05:
                                state = "无信号状态";
                                break;
                            default:
                                state = "";
                                break;
                        }
                        ecg_message.Arrhythmia_State = state;
                        //</editor-fold>
                        break;
                    case 0x98:// 心电 ST  值数据包 //ST 值扩大 100 倍上传，如果上传结果为-100，表示未计算得到结果，为无效值。
                        int Num = data[9];//ST 值的组别
                        float ST1 = (data[10] | (data[11] << 8)) / 100;//数据1
                        float ST2 = (data[12] | (data[13] << 8)) / 100;//数据2
                        float ST3 = (data[14] | (data[15] << 8)) / 100;//数据3
                        break;
                    case 0x99:// 心电 ST  模板数据包

                        switch (data[9])//当前 ST 模板的通道
                        {
                            case 0x00://通道 I
                                break;
                            case 0x01://通道 II
                                break;
                            case 0x02://通道 V1
                                break;
                        }
                        int ST_Num = data[10];//当前 ST 模板的序号，数值为 0~49
                        if (data[1] != 0x0c)//当程序运行时 电源板重新上电则会发送长度为11的数据包 其中不包含ST段数据
                        {
                            ST_Data.add((int) data[11]);
                            ST_Data.add((int) data[12]);
                            ST_Data.add((int) data[13]);
                            ST_Data.add((int) data[14]);
                            ST_Data.add((int) data[15]);
                        }

                        if (ST_Num == 49) {
                            //生成ST模版 并清除ST数据缓存数组
                            ST_Data.clear();
                        }
                        break;
                    case 0xA0://呼吸窒息数据包
                        switch (data[9]) {
                            case 0x00:
                                ecg_message.apnea = false;//无窒息
                                break;
                            case 0x01:
                                ecg_message.apnea = true;//窒息报警
                                break;
                        }
                        break;
                    case 0xA1://  呼吸 CVA  标志数据包
                        switch (data[9]) {
                            case 0x00:
                                ecg_message.CVA = false;//
                                break;
                            case 0x01:
                                ecg_message.CVA = true;//"呼吸 CVA 标志";//
                                break;
                        }
                        break;
                    case 0xA2://PVCs  统计个数
                        ecg_message.PVCs = data[9] | (data[10] << 8);
                        break;
                    case 0xB0://体温数据包
                        float tempData1 = (data[9] | (data[10] << 8)) / 10f;
                        float tempData2 = (data[11] | (data[12] << 8)) / 10f;
                        ecg_data.Data_Temp1 = tempData1;
                        ecg_data.Data_Temp2 = tempData2;
                        break;
                    default:
                        break;
                }
                //</editor-fold>
                break;
            case 0x02://NiBP
                //<editor-fold desc="NiBP">
                switch (data[4]) {
                    case 0x80://通用命令应答包
                        //<editor-fold desc="通用命令应答">
                        switch (data[9]) {
                            case 0x01:
                                //接收到的命令中参数类型错
                                break;
                            case 0x02:
                                //接收到的命令中数据类型错
                                break;
                            case 0x03:
                                //接收到的命令中数据 ID 错
                                break;
                            case 0x04:
                                //接收到的命令中数据段错
                                break;
                            case 0x05:
                                //接收到的命令中序列号段错
                                break;
                            case 0x06:
                                //接收到的命令中校验和错
                                break;
                            case 0x07:
                                //命令执行成功
                                break;
                            case 0x08:
                                //命令执行失败
                                break;
                            case 0x09:
                                //系统状态忙
                                break;
                        }
                        //</editor-fold>
                        break;
                    case 0x81://上电握手请求数据
                        MainActivity.send(Directive.Bp_Handshake);
                        //Log.e("bp","bpp");
                        break;
                    case 0x82://模块信息应答数据
                        //<editor-fold desc="模块信息">
                        int a1 = data[9];//软件主版本号
                        int a2 = data[10];//软件子版本号
                        int a3 = data[11];//软件修订版本号
                        int a4 = data[12];//算法主版本号
                        int a5 = data[13];//算法子版本号
                        int a6 = data[14];//算法修订版本号
                        int a7 = data[15];//协议主版本号
                        int a8 = data[16];//协议子版本号
                        int a9 = data[17];//协议修订版本号
                        int a10 = data[18];//

                        int CPU = data[18] & 0x01;
                        int Register = data[18] >> 1 & 0x01;
                        int RAM = data[18] >> 2 & 0x01;
                        int FLASH = data[18] >> 3 & 0x01;
                        int TIM = data[18] >> 4 & 0x01;
                        int AD = data[18] >> 5 & 0x01;
                        int Watchdog = data[18] >> 6 & 0x01;
                        int watchdogis = data[19] >> 7 & 0x01; //1 表示看门狗自检结果有效，0 表示看门狗自检结果无效
                        //</editor-fold>
                        break;
                    case 0x83://测试结果和状态应答数据包
                        //<editor-fold desc="测试结果和状态">
                        bp_info.Bp_H = (data[9] | (data[10] << 8));//收缩压
                        bp_info.Bp_L = (data[11] | (data[12] << 8));//舒张压
                        bp_info.Bp_Avg = (data[13] | (data[14] << 8));//平均压
                        bp_info.Bp_Pulse = (data[15] | (data[16] << 8));//脉率

                        //<editor-fold desc="病人类型">
                        bp_info.Bp_Mode = "--";//病人类型
                        switch (data[17]) {
                            case 0x00:
                                bp_info.Bp_Mode = "成人模式";
                                break;
                            case 0x01:
                                bp_info.Bp_Mode = "新生儿模式";
                                break;
                            case 0x02:
                                bp_info.Bp_Mode = "新生儿模式";
                                break;
                        }
                        //</editor-fold>

                        //<editor-fold desc="错误代码">
                        String bp_Error = "--";//错误代码
                        switch (data[18]) {
                            case 0x00:
                                bp_Error = "结果正常";
                                break;
                            case 0x01:
                                bp_Error = "袖带过松，可能是袖带缠绕过松，或未接袖带";
                                break;
                            case 0x02:
                                bp_Error = "漏气，可能是阀门或气路中漏气";
                                break;
                            case 0x03:
                                bp_Error = "气压错误，可能是阀门无法正常打开";
                                break;
                            case 0x04:
                                bp_Error = "弱信号，可能是测量对象脉搏太弱或袖带过松";
                                break;
                            case 0x05:
                                bp_Error = "超范围，可能是测量对象的血压值超过了测量范围";
                                break;
                            case 0x06:
                                bp_Error = "过分运动，可能是测量时，信号中含有运动伪差或太多干扰";
                                break;
                            case 0x07:
                                bp_Error = "过压";//，成人模式下袖带压力超过 290mmHg，儿童模式下袖带压力超过 247mmHg，新生儿模式下袖带压力超过 145mmHg";
                                break;
                            case 0x08:
                                bp_Error = "信号饱和，由于运动或其他原因使信号幅度太大";
                                break;
                            case 0x09:
                                bp_Error = "超时";//，成人/儿童模式下测量时间超过 120 秒，新生儿模式下测量时间超过 90 秒";
                                break;
                            case 0x0A:
                                bp_Error = "人工停止";
                                break;
                            case 0x0B:
                                bp_Error = "系统错误";
                                break;
                            default:
                                bp_Error = "";
                                break;
                        }
                        bp_info.Bp_Error = bp_Error;
                        //</editor-fold>

                        //<editor-fold desc="测量状态">
                        String bp_State = "--";//测量状态
                        switch (data[19]) {
                            case 0x00:
                                bp_State = "手动模式";
                                break;
                            case 0x01:
                                bp_State = "自动模式（1 分钟）";
                                break;
                            case 0x02:
                                bp_State = "自动模式（2 分钟）";
                                break;
                            case 0x03:
                                bp_State = "自动模式（3 分钟）";
                                break;
                            case 0x04:
                                bp_State = "自动模式（4 分钟）";
                                break;
                            case 0x05:
                                bp_State = "自动模式（5 分钟）";
                                break;
                            case 0x06:
                                bp_State = "自动模式（10 分钟）";
                                break;
                            case 0x07:
                                bp_State = "自动模式（15 分钟）";
                                break;
                            case 0x08:
                                bp_State = "自动模式（30 分钟）";
                                break;
                            case 0x09:
                                bp_State = "自动模式（60 分钟）";
                                break;
                            case 0x0A:
                                bp_State = "自动模式（90 分钟）";
                                break;
                            case 0x0B:
                                bp_State = "自动模式（2 小时）";
                                break;
                            case 0x0C:
                                bp_State = "自动模式（3 小时）";
                                break;
                            case 0x0D:
                                bp_State = "自动模式（4 小时）";
                                break;
                            case 0x0E:
                                bp_State = "自动模式（8 小时）";
                                break;
                            case 0x0F:
                                bp_State = "5 分钟连续模式";
                                break;
                        }
                        bp_info.Bp_State = bp_State;
                        //</editor-fold>

                        //<editor-fold desc="测量结果状态">
                        String bp_Result_type = "--";//测量结果状态
                        switch (data[20]) {
                            case 0x00:
                                bp_Result_type = "血压测量结果";
                                break;
                            case 0x01:
                                bp_Result_type = "压力校准结果";
                                break;
                            case 0x02:
                                bp_Result_type = "漏气检测结果";
                                break;
                            case 0x03:
                                bp_Result_type = "静脉穿刺结果";
                                break;
                        }
                        bp_info.Result_type = bp_Result_type;
                        //</editor-fold>

                        //Console.WriteLine(bp_H + " " + bp_L + " " + bp_Avg + " " + bp_Pulse + " " + bp_Mode + " "
                        //+ bp_Error + " " + bp_State + " " + bp_Result);

                        //</editor-fold>
                        break;
                    case 0x84://实时袖带压数据包
                        bp_info.Cuff_Pressure = (data[9] | (data[10] << 8));//袖带压力值
                        //Console.WriteLine(cuff_Pressure);
                        switch (data[11])//袖带类型错误标志
                        {
                            case 0x00:
                                bp_info.Type_error = "袖带使用正常";
                                break;
                            case 0x01:
                                bp_info.Type_error = "在成人模式下，检测到新生儿袖带";
                                break;
                        }
                        String system;//系统不同操作类型
                        switch (data[11]) {
                            case 0x00:
                                system = "在血压测量过程中";
                                break;
                            case 0x01:
                                system = "在校准方式下";
                                break;
                            case 0x02:
                                system = "在漏气检测中";
                                break;
                            case 0x03:
                                system = "在静脉穿刺过程中";
                                break;
                            default:
                                system = "";
                                break;
                        }
                        bp_info.System_state = system;
                        break;
                    case 0x86://测量开始或停止通知数据包
                        String bp_test_type = "";
                        switch (data[9]) {
                            case 0x00:
                                bp_test_type = "血压测量";
                                break;
                            case 0x01:
                                bp_test_type = "压力校准";
                                break;
                            case 0x02:
                                bp_test_type = "漏气检测";
                                break;
                            case 0x03:
                                bp_test_type = "静脉穿刺";
                                break;
                            case 0x04:
                                bp_test_type = "看门狗自检";
                                break;
                        }
                        bp_info.Test_type = bp_test_type;
                        String bp_test_state = "结束";
                        switch (data[10]) {
                            case 0x00:
                                bp_test_state = "结束";
                                //当测量结束时 发送查询状态命令
                                //SendCmd(Bp_Q_State, cmdPort);
                                break;
                            case 0x01:
                                bp_test_state = "开始";
                                break;
                        }
                        bp_info.Test_state = bp_test_state;
                        break;
                    case 0x87://心跳标志数据包
                        //当检测到一次完整的脉搏波时发送心跳标志数据包
                        break;
                }
                //</editor-fold>
                break;
            case 0x03://SpO2
                //<editor-fold desc="SpO2">
                switch (data[4]) {
                    case 0x80://通用命令应答包
                        //<editor-fold desc="通用命令应答">
                        switch (data[9]) {
                            case 0x01:
                                //接收到的命令中参数类型错
                                break;
                            case 0x02:
                                //接收到的命令中数据类型错
                                break;
                            case 0x03:
                                //接收到的命令中数据 ID 错
                                break;
                            case 0x04:
                                //接收到的命令中数据段错
                                break;
                            case 0x05:
                                //接收到的命令中序列号段错
                                break;
                            case 0x06:
                                //接收到的命令中校验和错
                                break;
                            case 0x07:
                                //命令执行成功
                                break;
                            case 0x08:
                                //命令执行失败
                                break;
                            case 0x09:
                                //系统状态忙
                                break;
                        }
                        //</editor-fold>
                        break;
                    case 0x81://上电握手请求数据
                        MainActivity.send(Directive.SpO2_Handshake);
                        break;
                    case 0x82://模块信息应答数据
                        //<editor-fold desc="模块信息">
                        int a1 = data[9];//软件主版本号
                        int a2 = data[10];//软件子版本号
                        int a3 = data[11];//软件修订版本号
                        int a4 = data[12];//算法主版本号
                        int a5 = data[13];//算法子版本号
                        int a6 = data[14];//算法修订版本号
                        int a7 = data[15];//协议主版本号
                        int a8 = data[16];//协议子版本号
                        int a9 = data[17];//协议修订版本号
                        //</editor-fold>
                        break;
                    case 0x83://模块自检结果
                        int ROM = data[9] & 0x01;
                        int RAM = data[9] >> 1 & 0x01;
                        int CPU = data[9] >> 2 & 0x01;
                        int AD = data[9] >> 3 & 0x01;
                        int WD = data[9] >> 4 & 0x01;
                        break;
                    case 0x84://实时波形数据包
                        if (data[9] != 0xFF)
                            spo2_Curve.add(data[9]);//脉搏波形数据 数据范围为 0～100，无效值为 0xFF
//                        else
//                            spo2_Curve.add(2);
                        switch (data[10])//脉搏音标记
                        {
                            case 0x00://无脉搏音
                                spo2_data.Pulse_Voice = 0;
                                break;
                            case 0x01://有脉搏音
                                spo2_data.Pulse_Voice = 1;
                                break;
                        }
                        spo2_data.Pi = data[11]; //棒图数据 数据范围为 0～15
                        break;
                    case 0x85://计算结果及状态信息数据包
                        spo2_data.Pulse_Value = data[9] | (data[10] << 8);//脉率  脉率值有效范围为 18～300，无效值为 0x1FF
                        spo2_data.Spo2_Value = data[11];//动脉氧饱和度 动脉氧饱和度有效范围为 0～100，无效值为 0x7F
                        spo2_data.Signal = (data[12] | (data[13] << 8)) / 1000f; //信号强度指数 信号强度指数范围为 0～20，上传时数据扩大了 1000 倍

                        if ((data[14] & 0x1) == 1) {
                            spo2_data.State = "低灌注";
                        } else if ((data[14] >> 1 & 0x1) == 1) {
                            spo2_data.State = "运动干扰";
                        } else if ((data[14] >> 2 & 0x1) == 1) {
                            spo2_data.State = "过度运动干扰";
                        } else if ((data[14] >> 3 & 0x1) == 1) {
                            spo2_data.State = "搜索脉搏波";
                        } else if ((data[14] >> 4 & 0x1) == 1) {
                            spo2_data.State = "搜索脉搏波时间过长/无脉搏波";
                        } else if ((data[14] >> 5 & 0x1) == 1) {
                            spo2_data.State = "血氧探头未接";
                        } else if ((data[14] >> 6 & 0x1) == 1) {
                            spo2_data.State = "血氧指夹空";
                        } else if ((data[14] >> 7 & 0x1) == 1) {
                            spo2_data.State = "血氧探头故障";
                        } else if ((data[15] & 0x1) == 1) {
                            spo2_data.State = "血氧硬件故障";
                        } else if ((data[15] >> 1 & 0x1) == 1) {
                            spo2_data.State = "血氧背景光太强";
                        } else if ((data[15] >> 2 & 0x1) == 1) {
                            spo2_data.State = "血氧探头不匹配";
                        } else {
                            spo2_data.State = "";
                        }

                        break;
                }
                //</editor-fold>
                break;
        }

        ////数据包类型
        //switch (data[3])
        //{
        //    case 0x01://控制命令包（DC）
        //        break;
        //    case 0x02://请求命令包（DR）
        //        break;
        //    case 0x03://命令应答包（DA）
        //        break;
        //    case 0x04://通用数据包（DD）
        //        break;
        //}

        ////数据包 ID
        //int id = data[4];

        ////数据包序列号
        //int id2 = data[5];

    }

    /// <summary>
    /// 发送数据包
    /// </summary>
    /// <param name="cmd">命令数组</param>
    public static void SendCmd(byte[] cmd) {
        byte[] data = new byte[10];
        data[0] = (byte) 0xFA;
        data[1] = 0x0A;
        data[2] = cmd[0];
        data[3] = cmd[1];
        data[4] = cmd[2];
        data[5] = (byte) id;
        data[6] = 0x00;
        data[7] = 0x00;
        data[8] = 0x00;
        byte sum = 0;
        for (int i = 1; i < data.length - 1; i++) {
            sum = (byte) (sum + data[i]);
        }
        data[9] = sum;
        //cmdPort.Write(data, 0, data.length);
        id++;
        ids.add(id);
    }

    public static void SendCmd(byte[] cmd, int num) {
        byte[] data = new byte[cmd[1]];
        for (int i = 0; i < cmd.length; i++) {
            if (i != 5)
                data[i] = cmd[i];
            else
                data[i] = (byte) id;
        }

        byte sum = 0;
        for (int i = 1; i < data.length - 1; i++) {
            sum = (byte) (sum + data[i]);
        }
        data[data.length - 1] = sum;
        //cmdPort.Write(data, 0, data.length);
        id++;
        ids.add(id);
    }

    //查找应答包package
    private void LookupResponseData() {
        try {
            Thread.sleep(1000);

            while (true) {
                Thread.sleep(10);//延时2毫秒 位置不能变
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 血氧信息类
     */
    public class Spo2_Data {
        public int Spo2_Value;   //血氧值
        public int Pulse_Value;  //脉率值
        public String State;     //血氧状态
        public int Pi;           //灌注指数
        public float Signal;     //信号强度
        public int Pulse_Voice;  //脉搏声
    }

    /**
     * 心电导联数据
     */
    public class Hr_Curve {
        public List<Integer> HR_I = new ArrayList<>();
        public List<Integer> HR_II = new ArrayList<>();
        public List<Integer> HR_V1 = new ArrayList<>();
        public List<Integer> HR_RESP = new ArrayList<>();

        void Clear() {
            HR_I.clear();
            HR_II.clear();
            HR_V1.clear();
            HR_RESP.clear();
        }
    }

    /**
     * 心电数据类
     */
    public class ECG_Data {
        public int Data_Hr;          //心率
        public int Data_Resp;        //呼吸率
        public float Data_Temp1;     //体温1
        public float Data_Temp2;     //体温2
        public String Data_Exception;//异常
    }

    /**
     * 心电模块状态类
     */
    public class ECG_Module_State {
        public String Patient_Mode; //病人类型
        public String Lead_Mode;    //心电导联模式
        public String Channel_I;    //心电通道I的导联
        public String Channel_II;   //心电通道II的导联
        public String Filter_Mode;  //心电滤波模式
        public String Notch_Filter; //陷波方式
        public boolean Notch_Switch;   //陷波开关 50/60 陷波方式是否打开
        public String Gain_I;       //心电通道I的增益
        public String Gain_II;      //心电通道II的增益
        public String Gain_V1;      //心电通道V1的增益
        public int ST_ISO;          //ST模板的ISO位置
        public int ST_ST;           //ST模板的ST位置
        public String Arrhythmia_Channel; //心率失常分析通道
        public boolean Arrhythmia_A;   //心率失常分析通道自主选择标志
        public boolean PACE;           //PACE检测标志
        public String PACE_Lead;    //PACE检测导联
        public boolean ECG_Cal;        //心电校准状态
        public int asphyxia;        //呼吸窒息报警时间
        public String Resp_Lead;    //呼吸导联
        public String Resp_Sensitivity; //呼吸导联
    }

    /**
     * 心电信息类
     */
    public class ECG_Message {
        public boolean PACE;           //PACE检测标志
        public boolean R;              //R检测标志
        public String Lead_State;   //导联状态
        public String Arrhythmia_State;   //心率失常分析状态
        public String Arrhythmia_Result;  //心律失常分析结果
        public int Arrhythmia_Old;  //上次心率失常位置
        public int Arrhythmia_Now;  //现在心率失常位置
        public boolean apnea;          //窒息报警标志
        public boolean CVA;            //呼吸CVA标志
        public int PVCs;            //PVCs每分钟统计个数
    }

    /**
     * 血压信息类
     */
    public class Bp_Info {
        public int Bp_H;        //收缩压
        public int Bp_L;        //舒张压
        public int Bp_Avg;      //平均压
        public int Cuff_Pressure;//袖带压
        public int Bp_Pulse;    //脉率
        public String Bp_Mode;  //袖带模式
        public String Bp_Error; //错误代码
        public String Bp_State; //测量状态
        public String Result_type;//测量结果类型
        public String Test_type; //操作类型
        public String Test_state;//启停状态
        public String Type_error;//袖带类型错误标志
        public String System_state;//系统状态
    }

}

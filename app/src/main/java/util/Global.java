package util;

/**
 * Created by YF on 2017/11/24.
 */

public class Global {
    public static boolean ecg_alarm = true;    //报警开关
    public static boolean nibp_alarm = true;   //报警开关
    public static boolean spo2_alarm = true;    //报警开关
    public static boolean pulse_alarm = true;   //报警开关
    public static boolean resp_alarm = true;    //报警开关
    public static boolean temp_alarm = true;   //报警开关

    public static boolean isAlarm1 = false;     //是否存在生理报警
    public static boolean isAlarm2 = false;     //是否存在技术报警

    public static String bp_auto_switch = "off";    //血压自动测量开关
    public static int hr_alert_high, hr_alert_low;    //心率报警上限 /心率报警下限
}

package com.lwf.ytlivechatanalyse.util;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Calendar;

public class DateUtil {

    public static void main(String[] args) {
        System.out.printf(getNowDateTime());
    }

    public static String getNowDate(){
        return DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd");
    }

    public static String getNowTime(){
        return DateFormatUtils.format(Calendar.getInstance(), "HH:mm:ss");
    }

    public static String getNowDateTime(){
        return DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd_HH-mm-ss");
    }
    /**
     * xxx秒转x时x分x秒
     */
    public static String secondToString(int second){
        boolean isNegative = second < 0;
        if(isNegative){
            second = - second;
        }
        int minute = second / 60;
        int seconds = second % 60;
        int hour = minute / 60;
        minute = minute % 60;
        String result = minute + ":" + (seconds < 10 ? "0" : "") + seconds;
        if (hour > 0){
            return hour + ":" + (minute < 10 ? "0" : "") + result;
        }
        if(isNegative){
            result = "-" + result;
        }
        return result;
    }
}

package com.lwf.ytlivechatanalyse.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

    private static final Logger logger = LoggerFactory.getLogger(DateUtil.class);

    public static void main(String[] args) {
        System.out.println(getTimestamp("2023-09-23 20:42:40"));
        System.out.println(getDateTime(1696859711l));
    }

    /**
     * 时间转时间戳
     */
    public static long getTimestamp(String dateTime){
        int length = StringUtils.length(dateTime);
        if(length < 12){
            return 0;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        int index = dateTime.indexOf("+");
        if(index > -1){
            format.setTimeZone(TimeZone.getTimeZone("GMT+" + dateTime.substring(index)));
        }else{
            format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        }
        dateTime = dateTime.replaceAll("[^0-9]", "");
        dateTime = String.format("%-14s", dateTime).replace(" ", "0");
        if(dateTime.length() > 14){
            dateTime = dateTime.substring(0, 14);
        }
        try {
            Date date = format.parse(dateTime);
            return date.getTime() * 1000;
        } catch (ParseException e) {
            logger.error("时间转换失败", e);
        }
        return 0;
    }

    public static String getNowDate(){
        return DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd", TimeZone.getTimeZone("GMT+8"));
    }

    public static String getNowTime(){
        return DateFormatUtils.format(Calendar.getInstance(), "HH:mm:ss", TimeZone.getTimeZone("GMT+8"));
    }

    public static String getNowDateTime(){
        return DateFormatUtils.format(Calendar.getInstance(), "yyyy-MM-dd_HH-mm-ss", TimeZone.getTimeZone("GMT+8"));
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
            result = hour + ":" + (minute < 10 ? "0" : "") + result;
        }
        if(isNegative){
            result = "-" + result;
        }
        return result;
    }
    /**
     * xxx秒转x时x分x秒
     */
    public static String secondToString(double second){
        boolean isNegative = second < 0;
        if(isNegative){
            second = - second;
        }
        int minute = (int) second / 60;
        double seconds = second % 60;
        int hour = minute / 60;
        minute = minute % 60;
        String result = minute + ":" + (seconds < 10 ? "0" : "") + String.format("%.2f", seconds);
        result = hour + ":" + (minute < 10 ? "0" : "") + result;
        if(isNegative){
            result = "-" + result;
        }
        return result;
    }

    /**
     * xx天前
     * @return
     */
    public static String getDayBefore(String str){
        Calendar calendar = Calendar.getInstance();
        int dayIndex = str.indexOf("前");
        if(dayIndex > -1){
            String numString = str.substring(0, dayIndex - 1);
            try {
                if(str.contains("天")){
                    int number = Integer.parseInt(numString);
                    calendar.add(Calendar.DATE, - number);
                }else if(str.contains("周")){
                    int number = Integer.parseInt(numString);
                    calendar.add(Calendar.DATE, - number * 7);
                }else if(str.contains("个月")){
                    numString = str.substring(0, dayIndex - 2);
                    int number = Integer.parseInt(numString);
                    calendar.add(Calendar.MONTH, - number);
                }else if(str.contains("年")){
                    int number = Integer.parseInt(numString);
                    calendar.add(Calendar.YEAR, - number);
                }
            }catch (Exception e){
                logger.error("数字转换失败", e);
            }
        }
        Date date = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    public static String getDateTime(Long timestamp) {
        String value = String.valueOf(timestamp);
        int length = value.length();
        if(length == 10){
            return DateFormatUtils.format(timestamp * 1000, "yyyy-MM-dd_HH-mm-ss", TimeZone.getTimeZone("GMT+8"));
        }else if(length == 13){
            return DateFormatUtils.format(timestamp, "yyyy-MM-dd_HH-mm-ss", TimeZone.getTimeZone("GMT+8"));
        }else if(length > 13){
            return DateFormatUtils.format(Long.parseLong(value.substring(0, 13)), "yyyy-MM-dd_HH-mm-ss", TimeZone.getTimeZone("GMT+8"));
        }
        return null;
    }

    public static Long getNowTimestamp() {
        return new Date().getTime() * 1000;
    }

    public static String getDateString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    public static Date parseDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            logger.error("日期转换失败：{}", dateString);
        }
        return null;
    }
}

package com.lwf.ytlivechatanalyse.util;

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
        logger.info(getTimestamp("2023-03-27_23:54:40") + "");
    }

    /**
     * 时间转时间戳
     * @param dateTime
     * @return
     */
    public static long getTimestamp(String dateTime){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            char[] chars = dateTime.toCharArray();
            if(chars.length > 19){
                if(chars[19] == '+'){
                    format.setTimeZone(TimeZone.getTimeZone("GMT" + String.valueOf(chars, 19, chars.length > 21 ? 2 : 1)));
                }else {
                    format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                }
            }
            chars[10] = ' ';
            chars[13] = ':';
            chars[16] = ':';
            dateTime = String.valueOf(chars, 0, 19);
            Date date = format.parse(dateTime);
            long time = date.getTime();
            return time;
        } catch (ParseException e) {
            logger.error("时间转换失败", e);
        }
        return 0;
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
}

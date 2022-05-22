package com.lwf.ytlivechatanalyse.util;

import org.apache.commons.lang3.StringUtils;

public class MessageUtil {

    /**
     * 获取某个字符 在 字符串中出现的比率
     * @param message
     * @param word
     * @return
     */
    public static int getProportion(String message,char word){
        if (StringUtils.isEmpty(message) || word == 0){
            return 0;
        }
        int same = 0;
        for (char c : message.toCharArray()){
            if (c == word){
                same ++;
            }
        }
        return same * 100 / message.length();
    }
}

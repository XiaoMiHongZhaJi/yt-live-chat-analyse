package com.lwf.ytlivechatanalyse.util;

import com.lwf.ytlivechatanalyse.bean.SrtData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SrtUtil {

    private static final Logger logger = LoggerFactory.getLogger(SrtUtil.class);

    public static List<SrtData> fileToSrt(MultipartFile file) {
        byte[] bytes = null;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            logger.error("srt文件读取失败");
            return null;
        }
        String srt = new String(bytes);
        String[] lines = srt.split("\r\n");
        List<SrtData> srtList = new ArrayList<>();
        for(int i = 0; i < lines.length; i++){
            SrtData srtData = new SrtData();
            String number = lines[i];
            if(StringUtils.isNotBlank(number)){
                try {
                    srtData.setSerial(Integer.parseInt(number));
                    String time = lines[++i];
                    String content = lines[++i];
                    srtData.setContent(content);
                    String[] split = time.split(" --> ");
                    srtData.setStartTime(split[0]);
                    srtData.setEndTime(split[1]);
                    srtList.add(srtData);
                }catch (NumberFormatException e){
                    logger.error("数字转换失败：" + number);
                }
            }
        }
        return srtList;
    }
}

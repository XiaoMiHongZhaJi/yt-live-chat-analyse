package com.lwf.ytlivechatanalyse.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class YtDlpUtil {
    private static final Logger logger = LoggerFactory.getLogger(YtDlpUtil.class);

    public static void main(String[] args){
        YtDlpUtil.proxy = "http://127.0.0.1:7890";
        downMp3("https://www.youtube.com/watch?v=_aUAKf5TjDw", "aaa.mp3", "E:\\Music\\2022直播歌曲集");
    }

    public static String proxy;

    @Value("${proxy}")
    public void setConfig1(String proxy) {
        YtDlpUtil.proxy = proxy;
    }

    public static String downMp3(String url, String fileName, String filePath){
        return execYtDlp(url, "-x --audio-format mp3 --audio-quality 0", fileName, filePath);
    }

    public static String execYtDlp(String url, String param, String fileName, String filePath){
        List<String> cmd = new ArrayList<>();
        cmd.add("yt-dlp");
        if(StringUtils.isNotBlank(proxy)){
            cmd.add("--proxy");
            cmd.add(proxy);
        }
        if(StringUtils.isNotBlank(param)){
            cmd.addAll(Arrays.asList(param.split(" ")));
        }
        if(StringUtils.isNotBlank(fileName)){
            cmd.add("--output");
            cmd.add(fileName + ".mp3");
        }
        if(StringUtils.isNotBlank(filePath)){
            File path = new File(filePath);
            if(!path.exists()){
                path.mkdirs();
            }
            cmd.add("--paths");
            cmd.add(filePath);
        }
        cmd.add(url);
        String[] array = cmd.toArray(new String[0]);
        return CmdUtil.execCmd(array, false, true);
    }
}

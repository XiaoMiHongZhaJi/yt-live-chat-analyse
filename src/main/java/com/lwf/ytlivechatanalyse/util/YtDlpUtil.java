package com.lwf.ytlivechatanalyse.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
        StringBuffer cmd = new StringBuffer("yt-dlp ");
        if(StringUtils.isNotBlank(proxy)){
            cmd.append("--proxy ");
            cmd.append(proxy);
            cmd.append(" ");
        }
        if(StringUtils.isNotBlank(param)){
            cmd.append(param);
            cmd.append(" ");
        }
        if(StringUtils.isNotBlank(fileName)){
            cmd.append("--output ");
            cmd.append("\"");
            cmd.append(fileName);
            cmd.append(".%(ext)s");
            cmd.append("\"");
            cmd.append(" ");
        }
        if(StringUtils.isNotBlank(filePath)){
            cmd.append("--paths ");
            cmd.append(filePath);
            cmd.append(" ");
        }
        cmd.append(url);
        return CmdUtil.execCmd(cmd.toString(), false, true, "UTF-8");
    }
}

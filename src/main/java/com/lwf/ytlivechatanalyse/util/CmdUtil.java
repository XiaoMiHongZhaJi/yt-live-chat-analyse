package com.lwf.ytlivechatanalyse.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

@Component
public class CmdUtil {

    private static final Logger logger = LoggerFactory.getLogger(CmdUtil.class);
    public static void main(String[] args){
        String ping = "ping 127.0.0.1";
        logger.info(execCmd(ping));
    }

    public static String proxy;

    public static String rename(String filePath, String oldName, String newName) {
        String osName = System.getProperty("os.name");
        if(osName.startsWith("Win")){
            if(StringUtils.isNoneBlank(filePath)){
                if(!filePath.endsWith("\\")){
                    filePath += "\\";
                }
                oldName = filePath + oldName;
                newName = filePath + newName;
            }
            return execCmd("move " + oldName + " " + newName, true, true, "GBK");
        }else{
            if(StringUtils.isNoneBlank(filePath)) {
                if(!filePath.endsWith("/")){
                    filePath += "/";
                }
                oldName = filePath + oldName;
                newName = filePath + newName;
            }
            return execCmd("mv " + oldName + " " + newName, true, true, "UTF8");
        }
    }

    @Value("${proxy}")
    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public static String execCmd(String cmd){
        return execCmd(cmd, true, true);
    }

    public static String execCmd(String cmd, boolean print, boolean result){
        String osName = System.getProperty("os.name");
        if(osName.startsWith("Win")){
            return execCmd(cmd, print, result, "GBK");
        }else{
            return execCmd(cmd, print, result, "UTF8");
        }
    }
    public static String execCmd(String cmd, boolean print, boolean result, String charset){
        logger.info("执行命令：" + cmd);
        Runtime run = Runtime.getRuntime();
        try {
            Process process = run.exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName(charset)));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null ){
                if(print){
                    logger.info(line);
                }
                if(result){
                    builder.append(line);
                    builder.append(System.getProperty("line.separator"));
                }
            }
            try {
                process.waitFor();
            } catch (InterruptedException e){
                logger.error("执行命令出错", e);
            }
            process.destroy();
            return builder.toString();
        } catch (IOException e){
            logger.error("执行命令出错", e);
        }
        return "";
    }

    public static String ps(String keyWord){
        return execCmd("ps -ef | grep -v grep | grep " + keyWord);
    }

    public static String chatDownloaderPs(String keyWord){
        return execCmd("ps -ef | grep -v grep | grep chat_downloader | grep " + keyWord);
    }

    public static String chatDownloader(String url, String fileName){
        String cmd = "chat_downloader ";
        if(StringUtils.isNotBlank(proxy)){
            cmd += "--proxy " + proxy + " ";
        }
        if(StringUtils.isNotBlank(fileName)){
            cmd += "--output output/" + fileName + " ";
        }

        return CmdUtil.execCmd(cmd + url, true, false);
    }
    public static String kill(String... keywords){
        if(keywords == null || keywords.length == 0 || StringUtils.isBlank(keywords[0])){
            return "";
        }
        String cmd = "kill9";
        for(String keyword : keywords){
            if(StringUtils.isNotBlank(keyword)){
                cmd += " " + keyword;
            }
        }
        return execCmd(cmd, true, true);
    }
}

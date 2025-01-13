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
import java.util.Arrays;

import static com.lwf.ytlivechatanalyse.util.CurlUtil.cookie;

@Component
public class CmdUtil {

    private static final Logger logger = LoggerFactory.getLogger(CmdUtil.class);
    public static void main(String[] args){
        logger.info(kill("chat_downloader"));
    }

    public static String proxy;

    public static String rename(String filePath, String oldName, String newName) {
        if(isWin()){
            if(StringUtils.isNotBlank(filePath)){
                if(!filePath.endsWith("\\")){
                    filePath += "\\";
                }
                oldName = filePath + oldName;
                newName = filePath + newName;
            }
            return execCmd("move " + oldName + " " + newName, true, true, "GBK");
        }else{
            if(StringUtils.isNotBlank(filePath)) {
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
        CmdUtil.proxy = proxy;
    }

    public static String cookie;

    @Value("${cookie}")
    public void setCookie(String cookie) {
        CmdUtil.cookie = cookie;
    }

    private static boolean isWin(){
        String osName = System.getProperty("os.name");
        return osName.startsWith("Win");
    }

    private static boolean isMac(){
        String osName = System.getProperty("os.name");
        return osName.startsWith("Mac");
    }

    public static String execCmd(String cmd){
        return execCmd(cmd, true, true);
    }

    // Mac
    public static String execCmd(String[] cmd, boolean print, boolean result){
        return execCmd(cmd, print, result, "UTF8");
    }

    public static String execCmd(String cmd, boolean print, boolean result){
        if(isWin()){
            return execCmd(cmd, print, result, "GBK");
        }else{
            return execCmd(cmd, print, result, "UTF8");
        }
    }

    public static String execCmd(String[] cmd, boolean print, boolean result, String charset){
        logger.info("执行命令：" + Arrays.toString(cmd).replace(",", ""));
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
        if(isWin()){
            String allPs = execCmd("tasklist", false, true);
            if(StringUtils.isNotBlank(allPs)){
                int index = allPs.indexOf(keyWord);
                if(index > 0){
                    int start = allPs.lastIndexOf("\n", index);
                    int end = allPs.indexOf("\n", index);
                    String psInfo = allPs.substring(start, end - 1);
                    return "发现进程信息：" + psInfo;
                }
            }
            return "未发现：" + keyWord;
        }
        String result = execCmd("pskw " + keyWord);
        return result.trim();
    }

    public static String chatDownloaderPs(String keyWord){
        if(isWin()){
            return ps("chat_downloader");
        }
        String result = execCmd("pskw chat_downloader " + keyWord);
        return result.trim();
    }

    public static String chatDownloader(String url, String liveDate, String fileName){
        String cmd = "chat_downloader ";
        if(StringUtils.isNotBlank(proxy)){
            cmd += String.format("--proxy %s ", proxy);
        }
        if(StringUtils.isNotBlank(cookie)){
            cmd += String.format("--cookies %s ", cookie);
        }
        if(StringUtils.isNotBlank(liveDate)){
            cmd += String.format("--live_date %s ", liveDate);
        }
        if(StringUtils.isNotBlank(fileName)){
            cmd += String.format("--output output/%s ", fileName);
        }

        return CmdUtil.execCmd(cmd + url, true, false);
    }
    public static String kill(String... keywords){
        if(keywords == null || keywords.length == 0 || StringUtils.isBlank(keywords[0])){
            return "";
        }
        if(isWin()){
            String result = execCmd("taskkill /f /im " + keywords[0] + ".exe");
            return result;
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

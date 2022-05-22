package com.lwf.ytlivechatanalyse.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;

public class CmdUtil {

    private static final Logger logger = LoggerFactory.getLogger(CmdUtil.class);
    public static void main(String[] args){
        String[] ping = {"ping","127.0.0.1"};
        String ping2 = "ping 127.0.0.1";
        execCmds(ping);
        execCmd(ping2);
    }

    public static String execCmds(String[] cmds){
        String osName = System.getProperty("os.name");
        if(osName.startsWith("Win")){
            return execCmds(cmds,"GBK", true, true);
        }else{
            return execCmds(cmds,"UTF8", true, true);
        }
    }

    public static String execCmds(String[] cmds, String charset, boolean print, boolean ret){
        logger.info("执行命令：" + Arrays.toString(cmds));
        ProcessBuilder processBuilder = new ProcessBuilder(cmds);
        try {
            Process process = processBuilder.start();
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName(charset)));
            String line;
            while ((line = reader.readLine()) != null ){
                if(print){
                    logger.info(line);
                }
                if(ret){
                    builder.append(line);
                    builder.append(System.getProperty("line.separator"));
                }
            }
            return builder.toString();
        } catch (IOException e){
            logger.error(e.getMessage());
        }
        return null;
    }

    public static String execCmd(String cmd){
        String osName = System.getProperty("os.name");
        if(osName.startsWith("Win")){
            return execCmd(cmd, "GBK", true, true);
        }else{
            return execCmd(cmd, "UTF8", true, true);
        }
    }
    public static String execCmd(String cmd, String charset, boolean print, boolean ret){
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
                if(ret){
                    builder.append(line);
                    builder.append(System.getProperty("line.separator"));
                }
            }
            try {
                process.waitFor();
            } catch (InterruptedException e){
                logger.error(e.getMessage());
            }
            process.destroy();
            return builder.toString();
        } catch (IOException e){
            logger.error(e.getMessage());
        }
        return null;
    }
}

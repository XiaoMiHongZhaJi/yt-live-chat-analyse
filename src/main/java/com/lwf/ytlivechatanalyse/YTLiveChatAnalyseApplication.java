package com.lwf.ytlivechatanalyse;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lwf.ytlivechatanalyse.dao")
public class YTLiveChatAnalyseApplication {

    public static void main(String[] args){
        SpringApplication.run(YTLiveChatAnalyseApplication.class, args);
    }

}

package com.lwf.ytlivechatanalyse.bean;

import lombok.Data;

import java.util.List;

@Data
public class HotList {
    private int count;//条数
    private int totalCount;//累计条数
    private int startSecond;//开始秒数
    private String startTime;//开始时间
    private String endTime;//结束时间
    private List<Object[]> messages;//相关弹幕
}

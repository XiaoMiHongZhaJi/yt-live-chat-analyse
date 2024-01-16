package com.lwf.ytlivechatanalyse.bean;

import lombok.Data;

@Data
public class BulletConfig {

    private int duringSecond = 15; //弹幕持续时间

    private int bulletBlock = 3; //每行弹幕数

    private int fontSize = 25; //字体大小

    private int lineSpace = 5; //行间距

    private int offset = 0; //弹幕时间偏移

    private boolean blocked = false; //弹幕是否分块
}

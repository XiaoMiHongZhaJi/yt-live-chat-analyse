package com.lwf.ytlivechatanalyse.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class LiveInfoLog {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String liveDate;
    private String url;
    private String title;
    private Integer viewCount; //播放数
    private Integer livingViewCount;//正在观看人数
    private String likeCount; //点赞数
    private Integer livingChatCount; //直播弹幕数
    private String platform;
    private String liveStatus;
    private Long updateTimestamp;//更新时间戳
    private Date updateTime;//最后更新时间
}

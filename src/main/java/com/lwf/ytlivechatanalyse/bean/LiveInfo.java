package com.lwf.ytlivechatanalyse.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class LiveInfo {

    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField(exist = false)
    private String year;
    private String liveDate;
    private String url;
    private String title;
    private String img;
    private String timeline;
    private Integer viewCount; //播放数
    private String likeCount; //点赞数
    private Integer liveChatCount; //录像弹幕数
    private Integer livingChatCount; //直播弹幕数
    private Integer srtCount; //字幕数
    private String platform;
    private Long startTimestamp;
    private String durationTime;
    private String liveStatus;
    private String downloadStatus;
    private Date updateTime;//最后更新时间

    //直播状态
    public static final String LIVE_STATUS_PREVIEW = "0";//直播预告
    public static final String LIVE_STATUS_LIVEING = "1";//直播中
    public static final String LIVE_STATUS_DONE = "2";//直播结束
    public static final String LIVE_STATUS_DISABLE = "4";//关闭

    //弹幕下载状态
    public static final String DOWNLOAD_STATUS_NONE = "0";//未下载
    public static final String DOWNLOAD_STATUS_DOWNLOADING = "1";//下载中
    public static final String DOWNLOAD_STATUS_DONE = "2";//已下载
    public static final String DOWNLOAD_STATUS_FILURE = "4";//下载失败


}

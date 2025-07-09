package com.lwf.ytlivechatanalyse.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class AuthorInfo {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String authorId;
    private String firstLiveDate;
    private String firstAuthorName;
    private String firstMessage;
    private String firstTimeText;
    private Long firstTimestamp;
    private String lastLiveDate;
    private String lastAuthorName;
    private String lastMessage;
    private String lastTimeText;
    private Long lastTimestamp;
    private String authorImage;
    private Integer messageCount;
    private Date updateTime;//最后更新时间
    private Integer blocked;
    private String allAuthorNames;
    private Integer authorNameCount;

}

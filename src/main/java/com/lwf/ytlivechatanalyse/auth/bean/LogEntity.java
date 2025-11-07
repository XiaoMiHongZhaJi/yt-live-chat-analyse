package com.lwf.ytlivechatanalyse.auth.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.Instant;
import java.util.Date;

@Data
public class LogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;

    private String userName;

    private String ip;

    private String method;

    private String url;

    private String params;

    private Integer returnCount;

    private Long spendTime;

    private String returnContent;

    private Date createTime;
}

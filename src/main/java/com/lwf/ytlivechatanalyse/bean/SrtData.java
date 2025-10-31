package com.lwf.ytlivechatanalyse.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class SrtData {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String liveDate;
    private Integer serial;
    private String startTime;
    private String endTime;
    private String content;

    @TableField(exist = false)
    private String schema;
}

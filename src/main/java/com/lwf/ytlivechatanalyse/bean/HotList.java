package com.lwf.ytlivechatanalyse.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import lombok.Data;

import java.util.List;

@Data
@TableName(autoResultMap = true)
public class HotList {

    private String liveDate;//开播日期
    private Integer intervalSeconds;//间隔时间
    private Integer count;//条数
    private Integer totalCount;//累计条数
    private Integer startSecond;//开始秒数
    private String startTime;//开始时间
    private String endTime;//结束时间
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<Object> messages;//相关弹幕
}

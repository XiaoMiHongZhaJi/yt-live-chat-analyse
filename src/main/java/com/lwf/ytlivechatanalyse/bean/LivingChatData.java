package com.lwf.ytlivechatanalyse.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LivingChatData {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String liveDate;
    private String authorImage;
    private String authorName;
    private String authorId;
    private String message;
    private BigDecimal timeInSeconds;
    private String timeText;
    private Long timestamp;
    private Integer emotesCount;

    @TableField(exist = false)
    private Integer page;

    @TableField(exist = false)
    private Integer limit;

    @TableField(exist = false)
    private String keyWords;

    @TableField(exist = false)
    private Integer count;

    public String showDetail() {
        return authorName + "：" + message;
    }
}

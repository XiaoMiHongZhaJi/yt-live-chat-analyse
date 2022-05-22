package com.lwf.ytlivechatanalyse.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class EmotesData {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String emotesId;
    private String images;
    private Boolean isCustomEmoji;
    private String name;
}

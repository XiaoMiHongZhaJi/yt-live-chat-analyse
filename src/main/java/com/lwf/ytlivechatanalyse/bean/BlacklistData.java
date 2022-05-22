package com.lwf.ytlivechatanalyse.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class BlacklistData {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String authorImage;
    private String authorName;
    private String authorId;
    private String message;
    private String remark;
    private String handelName;
    private String handelTime;
}

package com.lwf.ytlivechatanalyse.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lwf.ytlivechatanalyse.bean.LiveChatData;
import com.lwf.ytlivechatanalyse.bean.LivingChatData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LivingChatDataMapper extends BaseMapper<LivingChatData> {

    @Select("<script> select * from living_chat_data where live_date = #{liveChatData.liveDate} " +
            "<if test='liveChatData.message != null and liveChatData.message != \"\" '>" +
            "  and message like concat('%',#{liveChatData.message},'%') " +
            "</if>" +
            "<if test='liveChatData.authorName != null and liveChatData.authorName != \"\" '>" +
            "  and author_name like concat('%',#{liveChatData.authorName},'%') " +
            "</if>" +
            "order by timestamp <if test='!isAsc'> desc </if></script>")
    List<LiveChatData> selectLivingList(LiveChatData liveChatData, @Param("isAsc") boolean isAsc);
}

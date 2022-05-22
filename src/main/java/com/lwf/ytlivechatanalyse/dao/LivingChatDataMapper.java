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

    @Select({"<script> select * from living_chat_data where live_date = #{liveChatData.liveDate} " +
            "<if test='liveChatData.keyWords != null and liveChatData.keyWords != \"\" '>" +
            "  and message like concat('%',#{liveChatData.keyWords},'%') " +
            "</if>" +
            "<if test='liveChatData.authorName != null and liveChatData.authorName != \"\" '>" +
            "  and author_name like concat('%',#{liveChatData.authorName},'%') " +
            "</if>" +
            "order by timestamp <if test='!isAsc'> desc </if></script>"})
    List<LiveChatData> selectLivingList(LiveChatData liveChatData, @Param("isAsc") boolean isAsc);

    @Select("select id,live_date,author_image,author_name,message,time_in_seconds,time_text,timestamp,emotes_count " +
            "  from living_chat_data where live_date = #{liveDate} " +
            "   and time_in_seconds >= #{startSecond} and time_in_seconds < #{endSecond}")
    List<LiveChatData> selectHotListDeail(String liveDate, Integer startSecond, Integer endSecond);
}

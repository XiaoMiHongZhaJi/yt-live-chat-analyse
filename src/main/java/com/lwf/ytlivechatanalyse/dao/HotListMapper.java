package com.lwf.ytlivechatanalyse.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lwf.ytlivechatanalyse.bean.HotList;
import com.lwf.ytlivechatanalyse.bean.LiveChatData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HotListMapper extends BaseMapper<HotList> {


    @Select("select id,live_date,author_image,author_name,message,time_in_seconds,time_text,timestamp,emotes_count " +
            "  from live_chat_data where live_date = #{liveDate} " +
            "   and time_in_seconds >= #{startSecond} and time_in_seconds < #{endSecond} ")
    List<LiveChatData> selectLiveHotListDeail(String liveDate, Integer startSecond, Integer endSecond);

    @Select("select id,live_date,author_image,author_name,message,time_in_seconds,time_text,timestamp,emotes_count " +
            "  from living_chat_data where live_date = #{liveDate} " +
            "   and time_in_seconds >= #{startSecond} and time_in_seconds < #{endSecond}")
    List<LiveChatData> selectLivingHotListDeail(String liveDate, Integer startSecond, Integer endSecond);
}

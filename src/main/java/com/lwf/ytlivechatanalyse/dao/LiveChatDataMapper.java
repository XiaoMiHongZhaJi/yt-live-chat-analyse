package com.lwf.ytlivechatanalyse.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lwf.ytlivechatanalyse.bean.LiveChatData;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LiveChatDataMapper extends BaseMapper<LiveChatData> {

    @Select(" insert into live_chat_data(live_date, author_image, author_name, author_id, message, time_in_seconds, time_text, timestamp, emotes_count) select #{liveDate}, #{authorImage}, #{authorName}, #{authorId}, #{message}, #{timeInSeconds}, #{timeText}, #{timestamp}, #{emotesCount} from dual " +
            "  where not exists(select 1 from live_chat_data a where a.timestamp = #{timestamp})")
    void insertNotExists(LiveChatData liveChatData);

    @Select("select id,live_date,author_image,author_name,message,time_in_seconds,time_text,timestamp,emotes_count " +
            "from live_chat_data where live_date = #{liveDate} and message like CONCAT('%',#{message},'%') ")
    List<LiveChatData> selectByKeyWords(@Param("liveDate") String liveDate, @Param("message") String message);

    @Select("select * from live_chat_data where live_date = #{liveDate} and time_in_seconds > 0 order by id limit 1")
    LiveChatData selectStartMessage(@Param("liveDate") String liveDate);

    @Select("select count(1) from live_chat_data where live_date = #{liveDate}")
    Integer queryChatCount(@Param("liveDate") String liveDate);

    @Insert("insert into live_chat_data(live_date, author_image, author_name, author_id, message, sc_info, sc_amount, TIMESTAMP, emotes_count) " +
            "select live_date, author_image, author_name, author_id, message, sc_info, sc_amount, TIMESTAMP, emotes_count " +
            "  from living_chat_data a " +
            " where live_date like CONCAT(#{liveDate},'%') " +
            "   and not exists (select 1 from live_chat_data b where b.timestamp = a.TIMESTAMP) " +
            " order by TIMESTAMP desc")
    Integer asyncLivingChatData(@Param("liveDate") String liveDate);
}

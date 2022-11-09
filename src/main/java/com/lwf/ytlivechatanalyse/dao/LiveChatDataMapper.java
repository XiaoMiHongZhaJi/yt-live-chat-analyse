package com.lwf.ytlivechatanalyse.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lwf.ytlivechatanalyse.bean.LiveChatData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface LiveChatDataMapper extends BaseMapper<LiveChatData> {

    @Select(" insert into live_chat_data(live_date, author_image, author_name, author_id, message, time_in_seconds, time_text, timestamp, emotes_count) select #{liveDate}, #{authorImage}, #{authorName}, #{authorId}, #{message}, #{timeInSeconds}, #{timeText}, #{timestamp}, #{emotesCount} from dual " +
            "  where not exists(select 1 from live_chat_data a where a.timestamp = #{timestamp} and a.live_date = #{liveDate})")
    Integer insertNotExists(LiveChatData liveChatData);

    @Select("select id,live_date,author_image,author_name,message,time_in_seconds,time_text,timestamp,emotes_count " +
            "from live_chat_data where live_date = #{liveDate} and message like CONCAT('%',#{message},'%') ")
    List<LiveChatData> selectByKeyWords(@Param("liveDate") String liveDate, @Param("message") String message);

    @Select(" select min(timestamp) from " +
            " (select min(timestamp) timestamp from live_chat_data where live_date = #{liveDate} and message like '开了%' " +
            "  union select min(timestamp) timestamp from live_chat_data where live_date = #{liveDate} and message like '来了%' )a ")
    Long selectStartTimestampByMessage(@Param("liveDate")String liveDate);

    @Select(" select min(start_timestamp) start_timestamp from live_info where live_date = #{liveDate} ")
    Long selectStartTimestamp(@Param("liveDate")String liveDate);

    @Update(" update yt_live_chat_analyse.live_chat_data set \n" +
            "  time_text = date_format(date_sub(from_unixtime((timestamp - #{timestamp}) / 1000000), interval 8 hour), '%H:%i:%s'), " +
            "  time_in_seconds = (timestamp - #{timestamp}) / 1000000 " +
            "  where time_in_seconds is null and live_date = #{liveDate} ")
    Integer updateTimestamp(@Param("liveDate")String liveDate, @Param("timestamp")Long timestamp);

    @Select("select count(1) from live_chat_data where live_date = #{liveDate}")
    Integer queryChatCount(String liveDate);

}

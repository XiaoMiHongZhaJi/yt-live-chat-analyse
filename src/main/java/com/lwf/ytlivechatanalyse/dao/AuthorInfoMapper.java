package com.lwf.ytlivechatanalyse.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lwf.ytlivechatanalyse.bean.AuthorInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AuthorInfoMapper extends BaseMapper<AuthorInfo> {

    @Update("WITH affected_authors AS ( " +
            "    SELECT DISTINCT author_id " +
            "    FROM live_chat_data " +
            "    WHERE live_date like #{liveDate} || '%' AND author_id IS NOT NULL " +
            "), target_data AS ( " +
            "    SELECT d.* " +
            "    FROM live_chat_data d " +
            "    JOIN affected_authors a ON d.author_id = a.author_id " +
            "), first_msg AS ( " +
            "    SELECT DISTINCT ON (author_id) " +
            "        author_id, " +
            "        author_name AS first_author_name, " +
            "        message     AS first_message, " +
            "        time_text   AS first_time_text, " +
            "        timestamp   AS first_timestamp, " +
            "        live_date   AS first_live_date " +
            "    FROM target_data " +
            "    ORDER BY author_id, timestamp ASC " +
            "), last_msg AS ( " +
            "    SELECT DISTINCT ON (author_id) " +
            "        author_id, " +
            "        author_name AS last_author_name, " +
            "        message     AS last_message, " +
            "        time_text   AS last_time_text, " +
            "        timestamp   AS last_timestamp, " +
            "        live_date   AS last_live_date, " +
            "        author_image " +
            "    FROM target_data " +
            "    ORDER BY author_id, timestamp DESC " +
            "), count_msg AS ( " +
            "    SELECT " +
            "        author_id, " +
            "        COUNT(*) AS message_count, " +
            "        STRING_AGG(DISTINCT author_name, ', ') AS all_author_names, " +
            "        COUNT(DISTINCT author_name) AS author_name_count " +
            "    FROM target_data " +
            "    GROUP BY author_id " +
            ") " +
            "INSERT INTO author_info (author_id, message_count, all_author_names, author_name_count, author_image, " +
            "    first_live_date, first_author_name, first_message, first_time_text, first_timestamp, " +
            "    last_live_date, last_author_name, last_message, last_time_text, last_timestamp, update_time) " +
            "SELECT " +
            "    c.author_id, c.message_count, c.all_author_names, c.author_name_count, l.author_image, " +
            "    f.first_live_date, f.first_author_name, f.first_message, f.first_time_text, f.first_timestamp, " +
            "    l.last_live_date, l.last_author_name, l.last_message, l.last_time_text, l.last_timestamp, NOW() " +
            "FROM count_msg c " +
            "JOIN first_msg f ON c.author_id = f.author_id " +
            "JOIN last_msg  l ON c.author_id = l.author_id " +
            "ON CONFLICT (author_id) DO UPDATE SET " +
            "    message_count     = EXCLUDED.message_count, " +
            "    all_author_names  = EXCLUDED.all_author_names, " +
            "    author_name_count = EXCLUDED.author_name_count, " +
            "    author_image      = EXCLUDED.author_image, " +
            "    first_live_date   = EXCLUDED.first_live_date, " +
            "    first_author_name = EXCLUDED.first_author_name, " +
            "    first_message     = EXCLUDED.first_message, " +
            "    first_time_text   = EXCLUDED.first_time_text, " +
            "    first_timestamp   = EXCLUDED.first_timestamp, " +
            "    last_live_date    = EXCLUDED.last_live_date, " +
            "    last_author_name  = EXCLUDED.last_author_name, " +
            "    last_message      = EXCLUDED.last_message, " +
            "    last_time_text    = EXCLUDED.last_time_text, " +
            "    last_timestamp    = EXCLUDED.last_timestamp, " +
            "    update_time       = NOW()")
    Integer updateAuthorInfo(@Param("liveDate") String liveDate);
}

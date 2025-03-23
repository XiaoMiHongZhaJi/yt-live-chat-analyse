
truncate table yt_live_chat_analyse.author_info;

INSERT INTO yt_live_chat_analyse.author_info (
    author_id,
    message_count,
    first_live_date, first_author_name, first_message, first_time_text, first_timestamp,
    last_live_date, last_author_name, last_message, last_time_text, last_timestamp, author_image,
    update_time
)
SELECT
    author_id,
    COUNT(*) AS message_count,
    MIN(live_date) AS first_live_date,
    (SELECT author_name FROM yt_live_chat_analyse.live_chat_data d
     WHERE d.author_id = t.author_id ORDER BY TIMESTAMP ASC LIMIT 1) AS first_author_name,
    (SELECT message FROM yt_live_chat_analyse.live_chat_data d
WHERE d.author_id = t.author_id ORDER BY TIMESTAMP ASC LIMIT 1) AS first_message,
    (SELECT time_text FROM yt_live_chat_analyse.live_chat_data d
WHERE d.author_id = t.author_id ORDER BY TIMESTAMP ASC LIMIT 1) AS first_time_text,
    MIN(TIMESTAMP) AS first_timestamp,

    MAX(live_date) AS last_live_date,
    (SELECT author_name FROM yt_live_chat_analyse.live_chat_data d
WHERE d.author_id = t.author_id ORDER BY TIMESTAMP DESC LIMIT 1) AS last_author_name,
    (SELECT message FROM yt_live_chat_analyse.live_chat_data d
WHERE d.author_id = t.author_id ORDER BY TIMESTAMP DESC LIMIT 1) AS last_message,
    (SELECT time_text FROM yt_live_chat_analyse.live_chat_data d
WHERE d.author_id = t.author_id ORDER BY TIMESTAMP DESC LIMIT 1) AS last_time_text,
    MAX(TIMESTAMP) AS last_timestamp,
    (SELECT author_image FROM yt_live_chat_analyse.live_chat_data d
WHERE d.author_id = t.author_id ORDER BY TIMESTAMP DESC LIMIT 1) AS author_image,

    NOW() AS update_time -- 记录插入时间
FROM yt_live_chat_analyse.live_chat_data t
GROUP BY author_id;

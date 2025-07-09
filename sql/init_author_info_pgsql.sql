truncate table author_info;

WITH
-- 每个作者的第一条消息
first_msg AS (
    SELECT DISTINCT ON (author_id)
    author_id,
    author_name AS first_author_name,
    message     AS first_message,
    time_text   AS first_time_text,
    timestamp   AS first_timestamp,
    live_date   AS first_live_date
FROM live_chat_data
WHERE author_id IS NOT NULL
ORDER BY author_id, timestamp ASC
    ),

-- 每个作者的最后一条消息
    last_msg AS (
SELECT DISTINCT ON (author_id)
    author_id,
    author_name AS last_author_name,
    message     AS last_message,
    time_text   AS last_time_text,
    timestamp   AS last_timestamp,
    live_date   AS last_live_date,
    author_image
FROM live_chat_data
WHERE author_id IS NOT NULL
ORDER BY author_id, timestamp DESC
    ),

-- 消息数量 + 所有 author_name + 名字数量
    count_msg AS (
SELECT
    author_id,
    COUNT(*) AS message_count,
    STRING_AGG(DISTINCT author_name, ', ') AS all_author_names,
    COUNT(DISTINCT author_name) AS author_name_count
FROM live_chat_data
WHERE author_id IS NOT NULL
GROUP BY author_id
    )

-- 插入最终结果
INSERT INTO author_info (
    author_id,
    message_count,
    all_author_names,
    author_name_count,
    author_image,
    first_live_date,
    first_author_name,
    first_message,
    first_time_text,
    first_timestamp,
    last_live_date,
    last_author_name,
    last_message,
    last_time_text,
    last_timestamp,
    update_time
)
SELECT
    c.author_id,
    c.message_count,
    c.all_author_names,
    c.author_name_count,
    l.author_image,
    f.first_live_date,
    f.first_author_name,
    f.first_message,
    f.first_time_text,
    f.first_timestamp,
    l.last_live_date,
    l.last_author_name,
    l.last_message,
    l.last_time_text,
    l.last_timestamp,
    NOW() AS update_time
FROM count_msg c
         JOIN first_msg f ON c.author_id = f.author_id
         JOIN last_msg  l ON c.author_id = l.author_id;

select * from author_info;

# clear table
truncate table author_info;

# insert data
INSERT INTO author_info (
    author_id, message_count, author_image,
    first_live_date, first_author_name, first_message, first_time_text, first_timestamp,
    last_live_date, last_author_name, last_message, last_time_text, last_timestamp,
    update_time
)
SELECT
    author_id,
    COUNT(1) AS message_count,
    (SELECT author_image FROM live_chat_data d
     WHERE d.author_id = t.author_id ORDER BY TIMESTAMP DESC LIMIT 1) AS author_image,

    MIN(live_date) AS first_live_date,
    (SELECT author_name FROM live_chat_data d
WHERE d.author_id = t.author_id ORDER BY TIMESTAMP ASC LIMIT 1) AS first_author_name,
    (SELECT message FROM live_chat_data d
WHERE d.author_id = t.author_id ORDER BY TIMESTAMP ASC LIMIT 1) AS first_message,
    (SELECT time_text FROM live_chat_data d
WHERE d.author_id = t.author_id ORDER BY TIMESTAMP ASC LIMIT 1) AS first_time_text,
    MIN(TIMESTAMP) AS first_timestamp,

    MAX(live_date) AS last_live_date,
    (SELECT author_name FROM live_chat_data d
WHERE d.author_id = t.author_id ORDER BY TIMESTAMP DESC LIMIT 1) AS last_author_name,
    (SELECT message FROM live_chat_data d
WHERE d.author_id = t.author_id ORDER BY TIMESTAMP DESC LIMIT 1) AS last_message,
    (SELECT time_text FROM live_chat_data d
WHERE d.author_id = t.author_id ORDER BY TIMESTAMP DESC LIMIT 1) AS last_time_text,
    MAX(TIMESTAMP) AS last_timestamp,

    NOW() AS update_time
FROM live_chat_data t
GROUP BY author_id;


# query result
select * from author_info;
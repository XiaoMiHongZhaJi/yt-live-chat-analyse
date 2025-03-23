
# sync chat_data
INSERT INTO live_chat_data (
    live_date,
    author_image,
    author_name,
    author_id,
    message,
    sc_info,
    sc_amount,
    time_in_seconds,
    time_text,
    TIMESTAMP,
    emotes_count,
    blocked
)
select live_date,
       author_image,
       author_name,
       author_id,
       message,
       sc_info,
       sc_amount,
       time_in_seconds,
       time_text,
       TIMESTAMP,
       emotes_count,
       blocked
 from living_chat_data a
where live_date like '2025-03-22%'
  and not exists(select 1 from live_chat_data b where a.TIMESTAMP = b.TIMESTAMP)
order by TIMESTAMP


-- update author_info blocked by author_name
update author_info a set blocked = 2
 where exists(select 1 from blocked_keywords k where k.blocked_type != 1 and a.first_author_name like concat('%',k.keywords,'%'))
    or exists(select 1 from blocked_keywords k where k.blocked_type != 1 and a.last_author_name like concat('%',k.keywords,'%'))
    or exists(select 1 from blocked_keywords k where k.blocked_type != 1 and a.all_author_names like concat('%',k.keywords,'%'));

-- update author_info blocked by message
update author_info a set blocked = 1
 where exists(select 1 from blocked_keywords k where k.blocked_type != 2 and a.first_message like concat('%',k.keywords,'%'))
    or exists(select 1 from blocked_keywords k where k.blocked_type != 2 and a.last_message like concat('%',k.keywords,'%'));


-- update live_chat_data blocked by author_name
update live_chat_data a set blocked = 2
 where exists(select 1 from blocked_keywords k where k.blocked_type != 1 and a.author_name like concat('%',k.keywords,'%'));

-- update live_chat_data blocked by message
update live_chat_data a set blocked = 1
 where exists(select 1 from blocked_keywords k where k.blocked_type != 2 and a.message like concat('%',k.keywords,'%'));


-- update living_chat_data blocked by author_name
update living_chat_data a set blocked = 2
where exists(select 1 from blocked_keywords k where k.blocked_type != 1 and a.author_name like concat('%',k.keywords,'%'));

-- update living_chat_data blocked by message
update living_chat_data a set blocked = 1
where exists(select 1 from blocked_keywords k where k.blocked_type != 2 and a.message like concat('%',k.keywords,'%'));


-- query result
select * from author_info where blocked != 0;
select * from live_chat_data where blocked != 0;
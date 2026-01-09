
-- living_chat_data 去重
-- 查询
select *
from living_chat_data
where live_date like '2026-01-07%'
  and id in (select id
             from living_chat_data a
             where live_date like '2026-01-07%'
               and exists (select 1 from living_chat_data b where a.TIMESTAMP = b.TIMESTAMP and a.id < b.id)
);
-- 删除
delete from living_chat_data
where live_date like '2026-01-07%'
  and id in (select id
             from living_chat_data a
             where live_date like '2026-01-07%'
               and exists (select 1 from living_chat_data b where a.TIMESTAMP = b.TIMESTAMP and a.id < b.id)
);


-- live_chat_data 去重
-- 查询
select *
from live_chat_data
where live_date like '2026-01-07%'
  and id in (select id
             from live_chat_data a
             where live_date like '2026-01-07%'
               and exists (select 1 from live_chat_data b where a.TIMESTAMP = b.TIMESTAMP and a.id < b.id)
);
-- 删除
delete from live_chat_data
where live_date like '2026-01-07%'
  and id in (select id
             from live_chat_data a
             where live_date like '2026-01-07%'
               and exists (select 1 from live_chat_data b where a.TIMESTAMP = b.TIMESTAMP and a.id < b.id)
);

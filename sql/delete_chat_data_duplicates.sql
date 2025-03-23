
# living_chat_data 去重
# 查询
select *
from living_chat_data
where live_date like '2025-03-22%'
  and id in (select id
             from living_chat_data a
             where live_date like '2025-03-22%'
               and exists (select 1 from living_chat_data b where a.TIMESTAMP = b.TIMESTAMP and a.id < b.id)
);
# 删除
delete from living_chat_data
where live_date like '2025-03-22%'
  and id in (select id
             from living_chat_data a
             where live_date like '2025-03-22%'
               and exists (select 1 from living_chat_data b where a.TIMESTAMP = b.TIMESTAMP and a.id < b.id)
);


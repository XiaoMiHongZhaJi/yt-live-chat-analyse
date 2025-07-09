
#sudo -u postgres psql
CREATE USER root WITH PASSWORD 'root';

CREATE DATABASE yt_live_chat_analyse;
GRANT ALL PRIVILEGES ON DATABASE yt_live_chat_analyse TO root;

create table author_info
(
    id                bigserial   not null,
    author_id         varchar(30) not null,
    first_live_date   varchar(30)  default NULL::character varying,
    first_author_name varchar(60)  default NULL::character varying,
    first_message     varchar(600) default NULL::character varying,
    first_time_text   varchar(30)  default NULL::character varying,
    first_timestamp   bigint,
    last_live_date    varchar(30)  default NULL::character varying,
    last_author_name  varchar(60)  default NULL::character varying,
    last_message      varchar(600) default NULL::character varying,
    last_time_text    varchar(30)  default NULL::character varying,
    last_timestamp    bigint,
    author_image      varchar(200) default NULL::character varying,
    message_count     integer,
    all_author_names  varchar(600) default NULL::character varying,
    author_name_count     integer,
    update_time       timestamp with time zone,
    blocked           smallint     default '0'::smallint
);

comment on table author_info is '用户信息统计';

comment on column author_info.id is '主键id';

comment on column author_info.author_id is '用户id';

comment on column author_info.first_live_date is '首次发言所在的直播日期';

comment on column author_info.first_author_name is '首次发言时的名称';

comment on column author_info.first_message is '首次发言的内容';

comment on column author_info.first_time_text is '首次发言的时间文本';

comment on column author_info.first_timestamp is '首次发言的时间戳';

comment on column author_info.last_live_date is '最近一次发言所在的直播日期';

comment on column author_info.last_author_name is '最近一次发言时的名称';

comment on column author_info.last_message is '最近一次发言的内容';

comment on column author_info.last_time_text is '最近一次发言的时间文本';

comment on column author_info.last_timestamp is '最近一次发言的时间戳';

comment on column author_info.author_image is '用户头像';

comment on column author_info.message_count is '用户总发言次数';

comment on column author_info.update_time is '最后更新时间';

comment on column author_info.blocked is '是否删除';

alter table author_info owner to root;

create unique index idx_17094_primary on author_info (id);

create table blocked_keywords
(
    id           bigserial   not null,
    keywords     varchar(30) not null,
    blocked_type smallint                 default '0'::smallint,
    update_time  timestamp with time zone default CURRENT_TIMESTAMP
);

comment on table blocked_keywords is '屏蔽词信息';

comment on column blocked_keywords.id is '主键id';

comment on column blocked_keywords.keywords is '屏蔽词关键字';

comment on column blocked_keywords.blocked_type is '屏蔽类型，-1：不启用，0：消息和用户名，1：仅消息，2：仅用户名';

comment on column blocked_keywords.update_time is '最后更新时间';

alter table blocked_keywords owner to root;

create unique index idx_17113_primary on blocked_keywords (id);

create table emotes_data
(
	id bigserial not null
		constraint idx_17789_primary
			primary key,
	emotes_id varchar(60) default NULL::character varying,
	images varchar(120) default NULL::character varying,
	is_custom_emoji boolean,
	name varchar(60) default NULL::character varying,
	in_use boolean default false
);

comment on column emotes_data.id is '主键id';

comment on column emotes_data.emotes_id is '颜文字id';

comment on column emotes_data.images is '颜文字图片';

comment on column emotes_data.is_custom_emoji is '是否yt自定义表情';

comment on column emotes_data.name is '颜文字名称';

alter table emotes_data owner to root;

create unique index idx_17121_primary on emotes_data (id);

create table hot_list
(
    id               bigserial not null,
    live_date        varchar(30) default NULL::character varying,
    interval_seconds bigint,
    count            bigint,
    total_count      bigint,
    start_timestamp  bigint,
    start_second     bigint,
    start_time       varchar(30) default NULL::character varying,
    end_time         varchar(30) default NULL::character varying,
    messages         text
);

comment on column hot_list.id is '主键id';

comment on column hot_list.live_date is '开播日期';

comment on column hot_list.interval_seconds is '间隔时间';

comment on column hot_list.count is '条数';

comment on column hot_list.total_count is '累计条数';

comment on column hot_list.start_timestamp is '开始时间戳';

comment on column hot_list.start_second is '开始秒数';

comment on column hot_list.start_time is '开始时间';

comment on column hot_list.end_time is '结束时间';

comment on column hot_list.messages is '相关弹幕';

alter table hot_list owner to root;

create unique index idx_17131_primary on hot_list (id);

create table live_chat_data
(
    id              bigserial not null,
    live_date       varchar(30)    default NULL::character varying,
    author_image    varchar(200)   default NULL::character varying,
    author_name     varchar(60)    default NULL::character varying,
    author_id       varchar(30)    default NULL::character varying,
    message         varchar(600)   default NULL::character varying,
    sc_info         varchar(30)    default NULL::character varying,
    sc_amount       varchar(30)    default NULL::character varying,
    time_in_seconds numeric(11, 3) default NULL::numeric,
    time_text       varchar(30)    default NULL::character varying,
    timestamp       bigint,
    emotes_count    bigint         default '0'::bigint,
    blocked         smallint       default '0'::smallint
);

comment on column live_chat_data.id is '主键id';

comment on column live_chat_data.live_date is '开播日期';

comment on column live_chat_data.author_image is '用户头像';

comment on column live_chat_data.author_name is '用户名';

comment on column live_chat_data.author_id is '用户id';

comment on column live_chat_data.message is '消息';

comment on column live_chat_data.sc_info is 'super_chat 信息';

comment on column live_chat_data.sc_amount is 'super_chat 数量';

comment on column live_chat_data.time_in_seconds is '发送秒数';

comment on column live_chat_data.time_text is '发送时刻';

comment on column live_chat_data.timestamp is '发送时间戳';

comment on column live_chat_data.emotes_count is '颜文字个数';

comment on column live_chat_data.blocked is '是否删除';

alter table live_chat_data owner to root;

create index idx_17811_timestamp
	on live_chat_data (timestamp);

create index idx_17811_live_date
	on live_chat_data (live_date);

create index idx_17811_message
	on live_chat_data using gin (to_tsvector('simple'::regconfig, message::text));

create index idx_17811_author_name
	on live_chat_data (author_name);

create index idx_17811_author_id
	on live_chat_data (author_id);

create table live_info
(
    id                bigserial not null,
    live_date         varchar(30)  default NULL::character varying,
    url               varchar(200) default NULL::character varying,
    title             varchar(200) default NULL::character varying,
    img               varchar(200) default NULL::character varying,
    timeline          text,
    summary           text,
    mind_map          text,
    view_count        bigint,
    like_count        varchar(10)  default NULL::character varying,
    live_chat_count   bigint,
    living_chat_count bigint,
    srt_count         bigint,
    platform          varchar(1)   default NULL::character varying,
    start_timestamp   bigint,
    duration_time     varchar(10)  default NULL::character varying,
    live_status       varchar(1)   default '0'::character varying,
    download_status   varchar(1)   default '0'::character varying,
    update_time       timestamp with time zone
);

comment on column live_info.id is '主键id';

comment on column live_info.live_date is '开播日期';

comment on column live_info.url is '开播地址';

comment on column live_info.title is '标题';

comment on column live_info.img is '标题图片地址';

comment on column live_info.timeline is '时间线';

comment on column live_info.summary is 'AI总结';

comment on column live_info.mind_map is 'AI思维导图';

comment on column live_info.view_count is '观看人数';

comment on column live_info.like_count is '点赞人数';

comment on column live_info.live_chat_count is '录像弹幕数';

comment on column live_info.living_chat_count is '直播弹幕数';

comment on column live_info.srt_count is '字幕数';

comment on column live_info.platform is '开播平台，y：YouTube，t：twitch';

comment on column live_info.start_timestamp is '开播时间戳，默认取第一个“开了”、“来了”的时间';

comment on column live_info.duration_time is '持续时长';

comment on column live_info.live_status is '开播状态，0：直播预告，1：直播中，2：直播结束，4：已删除';

comment on column live_info.download_status is '弹幕下载状态，0：未下载，1：正在下载，2：已下载，4：下载失败';

comment on column live_info.update_time is '更新时间';

alter table live_info owner to root;

create unique index idx_17163_primary on live_info (id);

create table live_info_log
(
    id                bigserial not null,
    live_date         varchar(30)  default NULL::character varying,
    url               varchar(200) default NULL::character varying,
    title             varchar(200) default NULL::character varying,
    view_count        bigint,
    living_view_count bigint,
    like_count        varchar(10)  default NULL::character varying,
    living_chat_count bigint,
    platform          varchar(1)   default NULL::character varying,
    update_timestamp  bigint,
    live_status       varchar(1)   default '0'::character varying,
    update_time       timestamp with time zone
);

comment on column live_info_log.id is '主键id';

comment on column live_info_log.live_date is '开播日期';

comment on column live_info_log.url is '开播地址';

comment on column live_info_log.title is '标题';

comment on column live_info_log.view_count is '观看人数';

comment on column live_info_log.like_count is '点赞人数';

comment on column live_info_log.living_chat_count is '直播弹幕数';

comment on column live_info_log.platform is '开播平台，y：YouTube，t：twitch';

comment on column live_info_log.update_timestamp is '开播时间戳，默认取第一个“开了”、“来了”的时间';

comment on column live_info_log.live_status is '开播状态，0：直播预告，1：直播中，2：直播结束，4：已删除';

comment on column live_info_log.update_time is '更新时间';

alter table live_info_log owner to root;

create table living_chat_data
(
    id              bigserial not null,
    live_date       varchar(30)    default NULL::character varying,
    author_image    varchar(200)   default NULL::character varying,
    author_name     varchar(60)    default NULL::character varying,
    author_id       varchar(30)    default NULL::character varying,
    message         varchar(600)   default NULL::character varying,
    sc_info         varchar(30)    default NULL::character varying,
    sc_amount       varchar(30)    default NULL::character varying,
    time_in_seconds numeric(11, 3) default NULL::numeric,
    time_text       varchar(30)    default NULL::character varying,
    timestamp       bigint,
    emotes_count    bigint         default '0'::bigint,
    blocked         smallint       default '0'::smallint
);

comment on column living_chat_data.id is '主键id';

comment on column living_chat_data.live_date is '开播日期';

comment on column living_chat_data.author_image is '用户头像';

comment on column living_chat_data.author_name is '用户名';

comment on column living_chat_data.author_id is '用户id';

comment on column living_chat_data.message is '消息';

comment on column living_chat_data.sc_info is 'super_chat 信息';

comment on column living_chat_data.sc_amount is 'super_chat 数量';

comment on column living_chat_data.time_in_seconds is '发送秒数';

comment on column living_chat_data.time_text is '发送时刻';

comment on column living_chat_data.timestamp is '发送时间戳';

comment on column living_chat_data.emotes_count is '颜文字个数';

comment on column living_chat_data.blocked is '是否删除';

alter table living_chat_data owner to root;

create index idx_17861_live_date
	on living_chat_data (live_date);

create index idx_17861_message
	on living_chat_data using gin (to_tsvector('simple'::regconfig, message::text));

create index idx_17861_timestamp
	on living_chat_data (timestamp);

create index idx_17861_author_name
	on living_chat_data (author_name);

create index idx_17861_author_id
	on living_chat_data (author_id);

create table srt_data
(
    id         bigserial not null,
    live_date  varchar(30)  default NULL::character varying,
    serial     bigint,
    start_time varchar(30)  default NULL::character varying,
    end_time   varchar(30)  default NULL::character varying,
    content    varchar(600) default NULL::character varying
);

comment on column srt_data.id is '主键id';

comment on column srt_data.live_date is '开播日期';

comment on column srt_data.serial is '序号';

comment on column srt_data.start_time is '开始时间';

comment on column srt_data.end_time is '结束时间';

comment on column srt_data.content is '内容';

alter table srt_data owner to root;

create table video_info
(
    id               bigserial not null,
    author_id        varchar(30)  default NULL::character varying,
    publish_date     varchar(30)  default NULL::character varying,
    url              varchar(200) default NULL::character varying,
    title            varchar(200) default NULL::character varying,
    img              varchar(200) default NULL::character varying,
    view_count       bigint,
    like_count       varchar(10)  default NULL::character varying,
    platform         varchar(1)   default NULL::character varying,
    duration_time    varchar(10)  default NULL::character varying,
    update_time      timestamp with time zone,
    comment_count    bigint,
    down_song_status varchar(1)   default '0'::character varying
);

comment on column video_info.id is '主键id';

comment on column video_info.author_id is '创作者id';

comment on column video_info.publish_date is '发布日期';

comment on column video_info.url is '开播地址';

comment on column video_info.title is '标题';

comment on column video_info.img is '标题图片地址';

comment on column video_info.view_count is '观看人数';

comment on column video_info.like_count is '点赞人数';

comment on column video_info.platform is '开播平台，y：YouTube，t：twitch';

comment on column video_info.duration_time is '持续时长';

comment on column video_info.update_time is '最后更新时间';

comment on column video_info.comment_count is '评论数';

comment on column video_info.down_song_status is '下歌曲下载状态，0：未下载 1：已下载';

alter table video_info owner to root;


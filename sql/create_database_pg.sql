
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
    message_count     bigint,
    update_time       timestamp with time zone,
    blocked           smallint     default '0'::smallint
);

alter table author_info owner to root;

create unique index idx_17094_primary on author_info (id);

create table blocked_keywords
(
    id           bigserial   not null,
    keywords     varchar(30) not null,
    blocked_type smallint                 default '0'::smallint,
    update_time  timestamp with time zone default CURRENT_TIMESTAMP
);

alter table blocked_keywords owner to root;

create unique index idx_17113_primary on blocked_keywords (id);

create table emotes_data
(
    id              bigserial not null,
    emotes_id       varchar(60)  default NULL::character varying,
    images          varchar(120) default NULL::character varying,
    is_custom_emoji boolean,
    name            varchar(60)  default NULL::character varying,
    in_use          smallint     default '0'::smallint
);

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

alter table live_chat_data owner to root;

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

alter table living_chat_data owner to root;

create table srt_data
(
    id         bigserial not null,
    live_date  varchar(30)  default NULL::character varying,
    serial     bigint,
    start_time varchar(30)  default NULL::character varying,
    end_time   varchar(30)  default NULL::character varying,
    content    varchar(600) default NULL::character varying
);

alter table srt_data owner to root;

create table tmp_image_author_id
(
    author_name varchar(200) default NULL::character varying,
    author_id   varchar(30)  default NULL::character varying
);

alter table tmp_image_author_id owner to root;

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

alter table video_info owner to root;


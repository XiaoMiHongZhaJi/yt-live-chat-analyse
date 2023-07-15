#建立数据库
CREATE DATABASE yt_live_chat_analyse DEFAULT CHARACTER SET utf8mb4;
USE yt_live_chat_analyse;

#颜文字数据表
CREATE TABLE yt_live_chat_analyse.emotes_data (
  id INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  emotes_id VARCHAR(60) DEFAULT NULL COMMENT '颜文字id',
  images VARCHAR(100) DEFAULT NULL COMMENT '颜文字图片',
  is_custom_emoji TINYINT(1) DEFAULT NULL COMMENT '是否yt自定义表情',
  name VARCHAR(60) DEFAULT NULL COMMENT '颜文字名称',
  PRIMARY KEY (id)
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

#字幕表
CREATE TABLE yt_live_chat_analyse.srt_data (
  id INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  live_date VARCHAR(30) DEFAULT NULL COMMENT '开播日期',
  serial INT(11) DEFAULT NULL COMMENT '序号',
  start_time VARCHAR(30) DEFAULT NULL COMMENT '开始时间',
  end_time VARCHAR(30) DEFAULT NULL COMMENT '结束时间',
  content VARCHAR(600) DEFAULT NULL COMMENT '内容',
  PRIMARY KEY (id)
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

#弹幕数据表
CREATE TABLE yt_live_chat_analyse.live_chat_data (
  id INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  live_date VARCHAR(30) DEFAULT NULL COMMENT '开播日期',
  author_image VARCHAR(200) DEFAULT NULL COMMENT '用户头像',
  author_name VARCHAR(60) DEFAULT NULL COMMENT '用户名',
  author_id VARCHAR(30) DEFAULT NULL COMMENT '用户id',
  message VARCHAR(600) DEFAULT NULL COMMENT '消息',
  sc_info VARCHAR(30) DEFAULT NULL comment 'super_chat 信息',
  sc_amount VARCHAR(30) DEFAULT NULL comment 'super_chat 数量',
  time_in_seconds DECIMAL(11,3) DEFAULT NULL COMMENT '发送秒数',
  time_text VARCHAR(30) DEFAULT NULL COMMENT '发送时刻',
  TIMESTAMP BIGINT(20) DEFAULT NULL COMMENT '发送时间戳',
  emotes_count INT(11) DEFAULT 0 COMMENT '颜文字个数',
  PRIMARY KEY (id)
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

#弹幕数据表-直播中
CREATE TABLE yt_live_chat_analyse.living_chat_data (
  id INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  live_date VARCHAR(30) DEFAULT NULL COMMENT '开播日期',
  author_image VARCHAR(200) DEFAULT NULL COMMENT '用户头像',
  author_name VARCHAR(60) DEFAULT NULL COMMENT '用户名',
  author_id VARCHAR(30) DEFAULT NULL COMMENT '用户id',
  message VARCHAR(600) DEFAULT NULL COMMENT '消息',
  sc_info VARCHAR(30) DEFAULT NULL comment 'super_chat 信息',
  sc_amount VARCHAR(30) DEFAULT NULL comment 'super_chat 数量',
  time_in_seconds DECIMAL(11,3) DEFAULT NULL COMMENT '发送秒数',
  time_text VARCHAR(30) DEFAULT NULL COMMENT '发送时刻',
  TIMESTAMP BIGINT(20) DEFAULT NULL COMMENT '发送时间戳',
  emotes_count INT(11) DEFAULT 0 COMMENT '颜文字个数',
  PRIMARY KEY (id)
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

#分析表
CREATE TABLE yt_live_chat_analyse.hot_list (
  id INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  live_date VARCHAR(30) DEFAULT NULL COMMENT '开播日期',
  interval_seconds INT(11) DEFAULT NULL COMMENT '间隔时间',
  count INT(11) DEFAULT NULL COMMENT '条数',
  total_count INT(11) DEFAULT NULL COMMENT '累计条数',
  start_timestamp BIGINT(20) DEFAULT NULL COMMENT '开始时间戳',
  start_second INT(11) DEFAULT NULL COMMENT '开始秒数',
  start_time VARCHAR(30) DEFAULT NULL COMMENT '开始时间',
  end_time VARCHAR(30) DEFAULT NULL COMMENT '结束时间',
  messages json DEFAULT NULL COMMENT '相关弹幕',
  PRIMARY KEY (id)
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

#直播信息表
CREATE TABLE yt_live_chat_analyse.live_info (
    id INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
    live_date VARCHAR(30) DEFAULT NULL COMMENT '开播日期',
    url VARCHAR(200) DEFAULT NULL COMMENT '开播地址',
    title VARCHAR(200) DEFAULT NULL COMMENT '标题',
    img VARCHAR(200) DEFAULT NULL COMMENT '标题图片地址',
    timeline VARCHAR(4000) DEFAULT NULL COMMENT '时间线',
    view_count int(11) DEFAULT NULL COMMENT '观看人数',
    like_count varchar(10) DEFAULT NULL COMMENT '点赞人数',
    live_chat_count INT(11) DEFAULT NULL COMMENT '录像弹幕数',
    living_chat_count INT(11) DEFAULT NULL COMMENT '直播弹幕数',
    srt_count INT(11) DEFAULT NULL COMMENT '字幕数',
    platform VARCHAR(1) DEFAULT NULL COMMENT '开播平台，y：YouTube，t：twitch',
    start_timestamp BIGINT(20) DEFAULT NULL COMMENT '开播时间戳，默认取第一个“开了”、“来了”的时间',
    duration_time varchar(10) DEFAULT NULL COMMENT '持续时长',
    live_status varchar(1) default '0' null comment '开播状态，0：直播预告，1：直播中，2：直播结束，4：已删除',
    download_status varchar(1) default '0' null comment '弹幕下载状态，0：未下载，1：正在下载，2：已下载，4：下载失败',
    update_time datetime null comment '更新时间',
    PRIMARY KEY (id)
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

#直播信息日志表
CREATE TABLE `live_info_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `live_date` varchar(30) DEFAULT NULL COMMENT '开播日期',
  `url` varchar(200) DEFAULT NULL COMMENT '开播地址',
  `title` varchar(200) DEFAULT NULL COMMENT '标题',
  `view_count` int(11) DEFAULT NULL COMMENT '观看人数',
  `living_view_count` int(11) DEFAULT NULL,
  `like_count` varchar(10) DEFAULT NULL COMMENT '点赞人数',
  `living_chat_count` int(11) DEFAULT NULL COMMENT '直播弹幕数',
  `platform` varchar(1) DEFAULT NULL COMMENT '开播平台，y：YouTube，t：twitch',
  `update_timestamp` bigint(20) DEFAULT NULL COMMENT '开播时间戳，默认取第一个“开了”、“来了”的时间',
  `live_status` varchar(1) DEFAULT '0' COMMENT '开播状态，0：直播预告，1：直播中，2：直播结束，4：已删除',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

CREATE TABLE video_info (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  author_id varchar(30) DEFAULT NULL COMMENT '创作者id',
  publish_date varchar(30) DEFAULT NULL COMMENT '发布日期',
  url varchar(200) DEFAULT NULL COMMENT '开播地址',
  title varchar(200) DEFAULT NULL COMMENT '标题',
  img varchar(200) DEFAULT NULL COMMENT '标题图片地址',
  view_count int(11) DEFAULT NULL COMMENT '观看人数',
  like_count varchar(10) DEFAULT NULL COMMENT '点赞人数',
  platform varchar(1) DEFAULT NULL COMMENT '开播平台，y：YouTube，t：twitch',
  duration_time varchar(10) DEFAULT NULL COMMENT '持续时长',
  update_time datetime DEFAULT NULL COMMENT '最后更新时间',
  comment_count int(11) DEFAULT NULL COMMENT '评论数',
  down_song_status varchar(1) DEFAULT '0' COMMENT '下歌曲下载状态，0：未下载 1：已下载',
  PRIMARY KEY (id)
) ENGINE=InnoDB AUTO_INCREMENT=1235 DEFAULT CHARSET=utf8mb4;


#添加索引
ALTER TABLE yt_live_chat_analyse.live_chat_data
    ADD INDEX author_name (author_name);
ALTER TABLE yt_live_chat_analyse.live_chat_data
    ADD INDEX author_id (author_id);
ALTER TABLE yt_live_chat_analyse.live_chat_data
    ADD INDEX TIMESTAMP (TIMESTAMP);
ALTER TABLE yt_live_chat_analyse.live_chat_data
    ADD INDEX live_date (live_date);
ALTER TABLE yt_live_chat_analyse.live_chat_data
    ADD fulltext INDEX message (message);


ALTER TABLE yt_live_chat_analyse.living_chat_data
    ADD INDEX author_name (author_name);
ALTER TABLE yt_live_chat_analyse.living_chat_data
    ADD INDEX author_id (author_id);
ALTER TABLE yt_live_chat_analyse.living_chat_data
    ADD INDEX TIMESTAMP (TIMESTAMP);
ALTER TABLE yt_live_chat_analyse.living_chat_data
    ADD INDEX live_date (live_date);
ALTER TABLE yt_live_chat_analyse.living_chat_data
    ADD fulltext INDEX message (message);


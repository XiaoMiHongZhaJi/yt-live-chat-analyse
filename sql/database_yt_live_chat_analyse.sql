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

#弹幕数据表
CREATE TABLE yt_live_chat_analyse.live_chat_data (
  id INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  live_date VARCHAR(30) DEFAULT NULL COMMENT '开播日期',
  author_image VARCHAR(200) DEFAULT NULL COMMENT '用户头像',
  author_name VARCHAR(60) DEFAULT NULL COMMENT '用户名',
  author_id VARCHAR(30) DEFAULT NULL COMMENT '用户id',
  message VARCHAR(600) DEFAULT NULL COMMENT '消息',
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
  time_in_seconds DECIMAL(11,3) DEFAULT NULL COMMENT '发送秒数',
  time_text VARCHAR(30) DEFAULT NULL COMMENT '发送时刻',
  TIMESTAMP BIGINT(20) DEFAULT NULL COMMENT '发送时间戳',
  emotes_count INT(11) DEFAULT 0 COMMENT '颜文字个数',
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
    chat_count INT(11) DEFAULT NULL COMMENT '弹幕数量',
    platform VARCHAR(1) DEFAULT NULL COMMENT '开播平台，y：YouTube，t：twitch',
    start_timestamp BIGINT(20) DEFAULT NULL COMMENT '开播时间戳，默认取第一个“开了”、“来了”的时间',
    duration_time varchar(10) DEFAULT NULL COMMENT '持续时长',
    live_status varchar(1) default '0' null comment '开播状态，0：直播预告，1：直播中，2：直播结束，4：已删除',
    download_status varchar(1) default '0' null comment '弹幕下载状态，0：未下载，1：正在下载，2：已下载，4：下载失败',
    update_time datetime null comment '更新时间',
    PRIMARY KEY (id)
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;

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


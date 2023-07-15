#!/bin/sh
#启动 chat_downloader_twitch_t

if [ ! -d "/root/json" ];then
  mkdir /root/json
fi

chat_downloader https://www.twitch.tv/thebs_chen --live_date $(date +%Y-%m-%d_t) --output /root/json/$(date +%Y-%m-%d_%H_%M_%S_t).json 2>&1 | tee /root/json/json.log

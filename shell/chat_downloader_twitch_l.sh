#!/bin/sh
#启动 chat_downloader_twitch_l

if [ ! -d "/root/json" ];then
  mkdir /root/json
fi

chat_downloader https://www.twitch.tv/luoshushu0 --live_date $(date +%Y-%m-%d_l) --output /root/json/$(date +%Y-%m-%d_%H_%M_%S_l).json 2>&1 | tee -a /root/json/json.log

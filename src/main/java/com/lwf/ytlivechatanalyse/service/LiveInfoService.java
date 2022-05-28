package com.lwf.ytlivechatanalyse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lwf.ytlivechatanalyse.bean.LiveChatData;
import com.lwf.ytlivechatanalyse.bean.LiveInfo;
import com.lwf.ytlivechatanalyse.bean.LivingChatData;
import com.lwf.ytlivechatanalyse.dao.LiveChatDataMapper;
import com.lwf.ytlivechatanalyse.dao.LiveInfoMapper;
import com.lwf.ytlivechatanalyse.dao.LivingChatDataMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class LiveInfoService {

    @Autowired
    LiveInfoMapper liveInfoMapper;

    @Autowired
    LiveChatDataMapper liveChatDataMapper;

    @Autowired
    LivingChatDataMapper livingChatDataMapper;

    public void insertOrUpdate(LiveInfo liveInfo){
        QueryWrapper<LiveInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url", liveInfo.getUrl());
        Long count = liveInfoMapper.selectCount(queryWrapper);
        if(count > 0){
            liveInfoMapper.delete(queryWrapper);
        }
        liveInfoMapper.insert(liveInfo);
    }

    public List<LiveInfo> queryListBySelector(){
        QueryWrapper<LiveInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.notIn("live_status", LiveInfo.LIVE_STATUS_DISABLE);
        queryWrapper.orderByDesc("live_date");
        queryWrapper.select("live_date", "title", "url", "id", "live_status");
        return liveInfoMapper.selectList(queryWrapper);
    }

    public List<LiveInfo> queryListById(String ids){
        QueryWrapper<LiveInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.notIn("live_status", LiveInfo.LIVE_STATUS_DISABLE);
        queryWrapper.orderByDesc("live_date");
        if(StringUtils.isNotBlank(ids)){
            queryWrapper.in("id", ids.split(","));
        }
        return liveInfoMapper.selectList(queryWrapper);
    }

    public LiveInfo selectOne(LiveInfo liveInfo){
        QueryWrapper<LiveInfo> queryWrapper = new QueryWrapper<>();
        if(liveInfo.getId() != null){
            queryWrapper.eq("id", liveInfo.getId());
        }else if(StringUtils.isNotBlank(liveInfo.getLiveDate())){
            queryWrapper.eq("live_date", liveInfo.getLiveDate());
        }else if(StringUtils.isNotBlank(liveInfo.getUrl())){
            queryWrapper.eq("url", liveInfo.getUrl());
        }
        return liveInfoMapper.selectOne(queryWrapper);
    }

    public List<LiveInfo> selectList(){
        QueryWrapper<LiveInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.notIn("live_status", LiveInfo.LIVE_STATUS_DISABLE);
        queryWrapper.orderByDesc("live_date");
        List<LiveInfo> liveInfoList = liveInfoMapper.selectList(queryWrapper);
        //更新弹幕数量
        for(LiveInfo liveInfo : liveInfoList){
            Integer chatCount = liveInfo.getChatCount();
            String liveStatus = liveInfo.getLiveStatus();
            LiveInfo updateLiveInfo = new LiveInfo();
            updateLiveInfo.setId(liveInfo.getId());
            updateLiveInfo.setUpdateTime(new Date());
            if(LiveInfo.LIVE_STATUS_LIVEING.equals(liveStatus)){
                //直播中，取living_chat_data表的数量，并判断1分钟内是否增长，若不再增长，则说明直播已结束
                QueryWrapper<LivingChatData> queryChatDataWrapper = new QueryWrapper<>();
                queryChatDataWrapper.eq("live_date", liveInfo.getLiveDate());
                int newCount = Math.toIntExact(livingChatDataMapper.selectCount(queryChatDataWrapper));
                if(chatCount != null && newCount == chatCount){
                    if(new Date().getTime() - liveInfo.getUpdateTime().getTime() > 60 * 1000){
                        liveInfo.setChatCount(0);
                        updateLiveInfo.setChatCount(0);
                        liveInfo.setLiveStatus(LiveInfo.LIVE_STATUS_DONE);
                        updateLiveInfo.setLiveStatus(LiveInfo.LIVE_STATUS_DONE);
                        liveInfoMapper.updateById(updateLiveInfo);
                    }
                }else if(newCount > 0){
                    liveInfo.setChatCount(newCount);
                    updateLiveInfo.setChatCount(newCount);
                    liveInfoMapper.updateById(updateLiveInfo);
                }
            }else if(LiveInfo.LIVE_STATUS_DONE.equals(liveStatus)){
                //直播已结束
                if(chatCount == null || chatCount <= 0){
                    QueryWrapper<LiveChatData> queryChatDataWrapper = new QueryWrapper<>();
                    queryChatDataWrapper.eq("live_date", liveInfo.getLiveDate());
                    int count = Math.toIntExact(liveChatDataMapper.selectCount(queryChatDataWrapper));
                    if(count > 0){
                        liveInfo.setChatCount(count);
                        updateLiveInfo.setChatCount(count);
                        liveInfoMapper.updateById(updateLiveInfo);
                    }
                }
            }
        }
        return liveInfoList;
    }

    public int updateLiveInfoById(LiveInfo liveInfo){
        liveInfo.setUpdateTime(new Date());
        return liveInfoMapper.updateById(liveInfo);
    }
}

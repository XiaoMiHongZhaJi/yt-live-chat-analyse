package com.lwf.ytlivechatanalyse.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lwf.ytlivechatanalyse.bean.LiveInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LiveInfoMapper extends BaseMapper<LiveInfo> {

    @Select("select live_date from yt_live_chat_analyse.live_info where open_status = '1' order by live_date desc ")
    List<String> selectLiveDateList();
}

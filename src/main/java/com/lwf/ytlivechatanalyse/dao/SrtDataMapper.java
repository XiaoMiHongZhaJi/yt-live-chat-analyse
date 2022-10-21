package com.lwf.ytlivechatanalyse.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lwf.ytlivechatanalyse.bean.EmotesData;
import com.lwf.ytlivechatanalyse.bean.SrtData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SrtDataMapper extends BaseMapper<SrtData> {

}

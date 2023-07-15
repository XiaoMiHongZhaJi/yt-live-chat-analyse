package com.lwf.ytlivechatanalyse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lwf.ytlivechatanalyse.dao.EmotesDataMapper;
import com.lwf.ytlivechatanalyse.bean.EmotesData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmotesDataService {

    @Autowired
    EmotesDataMapper emotesDataMapper;

    public List<EmotesData> selectAll(){
        // name is not null
        QueryWrapper<EmotesData> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("name");
        queryWrapper.eq("in_use", 1);
        return emotesDataMapper.selectList(queryWrapper);
    }

    public List<EmotesData> selectEmoji(){
        // is_custom_emoji = 0 and name is not null
        QueryWrapper<EmotesData> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("name");
        queryWrapper.eq("in_use", 1);
        queryWrapper.eq("is_custom_emoji", 0);
        return emotesDataMapper.selectList(queryWrapper);
    }

    public Integer insertNotExists(EmotesData emotesData){
        return emotesDataMapper.insertNotExists(emotesData);
    }
}

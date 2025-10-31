package com.lwf.ytlivechatanalyse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lwf.ytlivechatanalyse.dao.EmotesDataMapper;
import com.lwf.ytlivechatanalyse.bean.EmotesData;
import com.lwf.ytlivechatanalyse.util.SchemaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmotesDataService {

    @Autowired
    EmotesDataMapper emotesDataMapper;

    public List<EmotesData> selectAll(String schema){
        // name is not null
        QueryWrapper<EmotesData> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("name");
        queryWrapper.eq("in_use", true);
        SchemaUtil.setSchema(schema);
        return emotesDataMapper.selectList(queryWrapper);
    }

    public List<EmotesData> selectEmoji(String schema){
        // is_custom_emoji = 0 and name is not null
        QueryWrapper<EmotesData> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("name");
        queryWrapper.eq("in_use", true);
        queryWrapper.eq("is_custom_emoji", false);
        SchemaUtil.setSchema(schema);
        return emotesDataMapper.selectList(queryWrapper);
    }

    public Integer insertNotExists(EmotesData emotesData, String schema){
        SchemaUtil.setSchema(schema);
        return emotesDataMapper.insertNotExists(emotesData);
    }
}

package com.lwf.ytlivechatanalyse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lwf.ytlivechatanalyse.dao.EmotesDataMapper;
import com.lwf.ytlivechatanalyse.bean.EmotesData;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmotesDataService {

    @Autowired
    EmotesDataMapper emotesDataMapper;

    public List<EmotesData> selectAll(){
        QueryWrapper<EmotesData> queryWrapper = new QueryWrapper<>();
        return emotesDataMapper.selectList(queryWrapper);
    }

    public Integer insertNotExists(EmotesData emotesData){
        return emotesDataMapper.insertNotExists(emotesData);
    }
}

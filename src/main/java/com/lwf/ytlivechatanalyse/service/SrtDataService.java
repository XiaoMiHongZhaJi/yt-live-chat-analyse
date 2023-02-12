package com.lwf.ytlivechatanalyse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lwf.ytlivechatanalyse.bean.SrtData;
import com.lwf.ytlivechatanalyse.dao.SrtDataMapper;
import com.lwf.ytlivechatanalyse.util.SrtUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class SrtDataService {

    private final Logger logger = LoggerFactory.getLogger(SrtDataService.class);

    @Autowired
    SrtDataMapper srtDataMapper;

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    public Long selectCount(String liveDate){
        QueryWrapper<SrtData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("live_date", liveDate);
        Long count = srtDataMapper.selectCount(queryWrapper);
        return count;
    }

    public void batchInsert(String liveDate, List<SrtData> srtList){
        SqlSession sqlSession = null;
        try{
            sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            for (SrtData srtData : srtList){
                srtData.setLiveDate(liveDate);
                sqlSession.insert("com.lwf.ytlivechatanalyse.dao.SrtDataMapper.insert", srtData);
            }
            sqlSession.flushStatements();
        }catch (Exception e){
            for (SrtData srtData : srtList){
                srtData.setLiveDate(liveDate);
                try {
                    srtDataMapper.insert(srtData);
                }catch (Exception e1){
                    logger.error("批量插入出错，已改为单笔插入，错误数据：", e1);
                    logger.error(srtData.toString());
                }
            }
        }finally {
            if(sqlSession != null)
                sqlSession.close();
        }
    }

    public Long importSrt(String liveDate, MultipartFile file) {
        List<SrtData> srtList = SrtUtil.fileToSrt(file);
        batchInsert(liveDate, srtList);
        QueryWrapper<SrtData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("live_date", liveDate);
        return srtDataMapper.selectCount(queryWrapper);
    }

    public List<SrtData> selectSrtInfo(SrtData srtData) {
        QueryWrapper<SrtData> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(srtData.getLiveDate())){
            queryWrapper.eq("live_date", srtData.getLiveDate());
        }
        if(StringUtils.isNotBlank(srtData.getContent())){
            queryWrapper.like("content", srtData.getContent());
        }
        queryWrapper.orderByDesc("live_date");
        queryWrapper.orderByAsc("id");
        return srtDataMapper.selectList(queryWrapper);
    }
}

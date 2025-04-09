package com.lwf.ytlivechatanalyse.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageInfo;
import com.lwf.ytlivechatanalyse.bean.SrtData;
import com.lwf.ytlivechatanalyse.dao.SrtDataMapper;
import com.lwf.ytlivechatanalyse.interceptor.DynamicSchemaInterceptor;
import com.lwf.ytlivechatanalyse.util.Constant;
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

    public void batchInsert(String liveDate, List<SrtData> srtList){
        SqlSession sqlSession = null;
        try{
            sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            if(StringUtils.isNotBlank(liveDate) && !liveDate.startsWith(Constant.DEFAULT_YEAR)){
                DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
            }
            for (SrtData srtData : srtList){
                srtData.setLiveDate(liveDate);
                sqlSession.insert("com.lwf.ytlivechatanalyse.dao.SrtDataMapper.insert", srtData);
            }
            sqlSession.commit();
        }catch (Exception e){
            for (SrtData srtData : srtList){
                srtData.setLiveDate(liveDate);
                if(StringUtils.isNotBlank(liveDate) && !liveDate.startsWith(Constant.DEFAULT_YEAR)){
                    DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
                }
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
        if(StringUtils.isNotBlank(liveDate) && !liveDate.startsWith(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
        }
        List<SrtData> srtList = SrtUtil.fileToSrt(file);
        batchInsert(liveDate, srtList);
        QueryWrapper<SrtData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("live_date", liveDate);
        return srtDataMapper.selectCount(queryWrapper);
    }

    public List<SrtData> selectSrtInfo(SrtData srtData) {
        String liveDate = srtData.getLiveDate();
        QueryWrapper<SrtData> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(liveDate)){
            queryWrapper.likeRight("live_date", liveDate);
        }
        if(StringUtils.isNotBlank(srtData.getContent())){
            queryWrapper.like("content", srtData.getContent());
        }
        Integer serial = srtData.getSerial();
        if(serial != null && serial > 0){
            queryWrapper.ge("serial", serial);
        }
        queryWrapper.orderByDesc("live_date");
        queryWrapper.orderByAsc("id");
        if(StringUtils.isNotBlank(liveDate) && !liveDate.startsWith(Constant.DEFAULT_YEAR)){
            DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
        }
        return srtDataMapper.selectList(queryWrapper);
    }

    public List<SrtData> selectSrtDetail(SrtData srtData, int limit) {
        String liveDate = srtData.getLiveDate();
        QueryWrapper<SrtData> queryWrapper = new QueryWrapper<>();
        Integer serial = srtData.getSerial();
        if(serial == null){
            serial = 0;
        }
        int startSerial = serial - limit / 2;
        int endSerial = serial + limit / 2;
        queryWrapper.ge("serial", startSerial);
        queryWrapper.lt("serial", endSerial);
        queryWrapper.orderByAsc("id");
        queryWrapper.last("limit " + limit);
        if(StringUtils.isNotBlank(liveDate)){
            queryWrapper.likeRight("live_date", liveDate);
            if(!liveDate.startsWith(Constant.DEFAULT_YEAR)){
                DynamicSchemaInterceptor.setSchema(Constant.DEFAULT_SCHEMA + "_" + liveDate.substring(0, 4));
            }
        }
        return srtDataMapper.selectList(queryWrapper);
    }
}

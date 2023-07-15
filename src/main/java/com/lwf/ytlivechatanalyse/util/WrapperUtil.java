package com.lwf.ytlivechatanalyse.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;

public class WrapperUtil {

    public static void keyWordsLike(QueryWrapper<?> queryWrapper, String keywords, String colume) {
        if(keywords.contains(" ")){
            String[] andKeywordsArray = keywords.split(" ");
            for(String andKeywords : andKeywordsArray){
                if(andKeywords.contains("|")){
                    addOrCondition(queryWrapper, andKeywords, colume);
                }else{
                    queryWrapper.like(colume, andKeywords.trim());
                }
            }
        }else if(keywords.contains("|")){
            addOrCondition(queryWrapper, keywords, colume);
        }else{
            queryWrapper.like(colume, keywords.trim());
        }
    }

    public static void addOrCondition(QueryWrapper<?> queryWrapper, String keywords, String colume) {
        String[] orKeywordsArray = keywords.split("\\|");
        queryWrapper.and((orQueryWrapper) -> {
            for(String orKeywords : orKeywordsArray){
                if(StringUtils.isNotBlank(orKeywords)){
                    orQueryWrapper.or().like(colume, orKeywords.trim());
                }
            }
        });
    }
}

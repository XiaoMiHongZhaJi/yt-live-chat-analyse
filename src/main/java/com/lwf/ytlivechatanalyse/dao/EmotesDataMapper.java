package com.lwf.ytlivechatanalyse.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lwf.ytlivechatanalyse.bean.EmotesData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmotesDataMapper extends BaseMapper<EmotesData> {

    @Select(" insert into emotes_data ( emotes_id, images, is_custom_emoji, name ) " +
            " select #{emotesId}, #{images}, #{isCustomEmoji}, #{name} from dual where not exists (select 1 from emotes_data where name = #{name}) ")
    Integer insertNotExists(EmotesData emotesData);
}

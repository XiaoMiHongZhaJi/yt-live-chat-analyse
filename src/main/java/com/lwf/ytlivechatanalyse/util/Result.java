package com.lwf.ytlivechatanalyse.util;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 返回结果数据
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {

    private int code;
    private String msg;
    private long count;
    private List<T> data;

    public Result(){
        this.code = 0;
    }

    public Result(List<T> list){
        this.code = 0;
        this.data = list;
        this.count = list.size();
    }

    public Result(PageInfo<T> page){
        this.code = 0;
        this.data = page.getList();
        this.count = page.getTotal();
    }

}

package com.youlexuan.entity;

import java.io.Serializable;
import java.util.List;

public class PageResult implements Serializable {

    /**
     * 封装一个分页的实体类
     * 需要的属性有:
     * 总记录数 total
     * 当前页结果  rows
     */
    private long total;
    private List rows;

    public PageResult(long total, List rows) {
        this.total = total;
        this.rows = rows;
    }

    public PageResult() {
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }
}

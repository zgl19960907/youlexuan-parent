package com.youlexuan.sellergoods.service;

import com.youlexuan.entity.PageResult;
import com.youlexuan.pojo.TbBrand;

import java.util.List;
import java.util.Map;

public interface BrandService {
    //显示所有品牌商品
    public List<TbBrand> findAll();

    //分页+模糊查询
    PageResult findPage(TbBrand brand, int pageNum,int pageSize);

    //添加品牌
    public void add(TbBrand brand);

    //修改品牌
    public void update(TbBrand brand);

    //修改品牌前需要先查出来
    TbBrand findOne(Long id);

    //批量删除
    public void delet(Long[] ids);

    //下拉列表从数据库中获取
    List<Map> selectOption();
}

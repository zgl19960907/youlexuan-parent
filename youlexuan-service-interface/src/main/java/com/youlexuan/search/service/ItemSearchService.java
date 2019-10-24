package com.youlexuan.search.service;

import com.youlexuan.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
    /**
     * 搜索

     * @return
     */
    public Map<String,Object> search(Map searchMap);

    /**
     * 导入数据
     */
    public void importList(List<TbItem> list);



    /**
     * 删除数据
     * @param
     */
    void deleteByGoodsIds(Long[] ids);
}

package com.youlexuan.page.service;

/**
 * 商品详细页接口
 */
public interface IItemPageService {
    /**
     * 生成商品详细页
     * @param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId);

    /**
     * 删除商品详细页
     * @return
     */
    public boolean deleteItemHtml(Long[] goodsIds);
}

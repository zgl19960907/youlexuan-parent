package com.youlexuan.cart;

import com.youlexuan.pojogroup.Cart;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {

    /**
     * 添加商品到购物车
     * @param cartList  原来的购物车列表
     * @param itemId  商品的SKU  id
     * @param num  加了几个商品
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num );

    //从Redis中获取数据
    public List<Cart> findCartListFromRedis(String username);

    //将购物车数据存入Redis中
    public void saveCartListToRedis(String username,List<Cart> cartList);

    //合并购物车
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}

package com.youlexuan.cart.controller;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.youlexuan.cart.CartService;
import com.youlexuan.entity.Result;
import com.youlexuan.pojogroup.Cart;
import com.youlexuan.util.CONSTANT;
import com.youlexuan.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    /**
     * （1）从cookie中取出购物车
     * （2）向购物车添加商品
     * （3）将购物车存入cookie
     */

    @Reference(timeout = 60000)
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    /**
     * 登陆过,从Redis中获取,没登录从cookie中取
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String cartListString = CookieUtil.getCookieValue(request, CONSTANT.CART_LIST_COOKIE_KEY, "UTF-8");
        if (StringUtils.isEmpty(cartListString)){
            cartListString="[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString,Cart.class);
        if ("anonymousUser".equals(username)){//用户未登录
            return cartList_cookie;
        }else{//用户已登录,从Redis中获取
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            if (cartList_cookie.size()>0){//如果本地存在购物车
                //合并购物车
                cartList_redis = cartService.mergeCartList(cartList_redis,cartList_cookie);
                //清除本地cookie的数据
                CookieUtil.deleteCookie(request,response,CONSTANT.CART_LIST_COOKIE_KEY);
                //将合并后的数据存入Redis
                cartService.saveCartListToRedis(username,cartList_redis);
            }
            return cartList_redis;
        }
    }

    /**
     * 添加商品到购物车
     * 1.用户未登录,从cookie中取旧的购物车列表,计算出新购物车列表后,再将新购物车列表写到cookie中
     * 2.用户已登录,从Redis中取购物车列表,计算出新的购物车列表后,再将新列表写到Redis中
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
    public Result addGoodsToCartList(Long itemId, Integer num){

        //如果不用springmvc的注解写上下面两行完成跨域请求
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("当前登录用户:"+username);
            List<Cart> cartList = findCartList();//获取购物车列表
            cartList = cartService.addGoodsToCartList(cartList,itemId,num);
            if ("anonymousUser".equals(username)){//如果未登录,保存到cookie中
                CookieUtil.setCookie(request,response, CONSTANT.CART_LIST_COOKIE_KEY,JSON.toJSONString(cartList),3600*24,"UTF-8");
                System.out.println("向cookie中存入数据");
            }else{//如果已登录,保存到Redis中
                cartService.saveCartListToRedis(username,cartList);
            }
            return new Result(true,"添加成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }


}

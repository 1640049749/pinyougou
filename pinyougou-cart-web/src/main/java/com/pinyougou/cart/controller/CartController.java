package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.util.CookieUtil;
import entity.Cart;
import entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping(value = "/cart")
public class CartController {

    @Reference
    private CartService cartService;

    /**
     * 添加商品到已有的购物车的列表中
     *
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
//    @CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
    public Result addGoodsToCartList(Long itemId, Integer num, HttpServletRequest request, HttpServletResponse response) {
//        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");//统一指定的域访问我的服务器资源
//        response.setHeader("Access-Control-Allow-Credentials", "true");//同意客户端携带cookie
//        springmvc.xml已经配置了跨域请求
        try {
            //1.获取用户名(先设置一个匿名用户，如果未登录则会使用匿名用户)
            String name = SecurityContextHolder.getContext().getAuthentication().getName();

            //2.判断用户是否登录，如果没登录，操作cookie
            if ("anonymousUser".equals(name)) { //未登录

                //2.1从cookie中获取已有的购物车列表数据：List<Cart>
                String cartList = CookieUtil.getCookieValue(request, "cartList","UTF-8");
                if (StringUtils.isEmpty(cartList)) {
                    cartList = "[]";
                }
                List<Cart> cookieList = JSON.parseArray(cartList, Cart.class);

                //2.2向已有的的购物车列表中，添加商品 返回一个最新的购物车列表（写一个方法 向已有的购物车添加商品）
                List<Cart> newestList = cartService.addGoodsToCartList(cookieList, itemId, num);

                //2.3将最新的购物车数据设置回cookie中
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(newestList), 3600 * 24, "UTF-8");
            } else {
                //3.如果登录，操作redis
                //3.1从redis中获取已有的购物车列表数据
                List<Cart> redisList = cartService.findCartListFromRedis(name);
                //3.2向已有的购物车列表中添加商品 返回一个最新的购物车列表
                List<Cart> newestList = cartService.addGoodsToCartList(redisList, itemId, num);
                //3.3将最新的购物车列表数据存储回redis中
                cartService.saveCartListToRedis(name, newestList);
            }
            return new Result(true, "成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "失败");
        }
    }

    @RequestMapping(value = "/findCartList")
    public List<Cart> findCartList(HttpServletRequest request, HttpServletResponse response) {
        //1.获取用户名(先设置一个匿名用户，如果未登录则会使用匿名用户)
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        //2.判断
        if ("anonymousUser".equals(name)) {
            //展示cookie中的购物车数据
            String cartList = CookieUtil.getCookieValue(request, "cartList","UTF-8");
            if (!StringUtils.isNotBlank(cartList)) {
                cartList = "[]";
            }
            List<Cart> cookieList = JSON.parseArray(cartList, Cart.class);
            return cookieList;
        } else {
            //展示redis中购物车数据
            List<Cart> redisList = cartService.findCartListFromRedis(name);

            //合并购物车的数据
            //2.获取cookie中的数据
            String cartList = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
            if (!StringUtils.isNotBlank(cartList)) {
                cartList = "[]";
            }
            List<Cart> cookieList = JSON.parseArray(cartList, Cart.class);

            //3.合并
            List<Cart> carts = cartService.mergeCartList(cookieList, redisList);
            //4.将最新的数据重新设置回redis
            cartService.saveCartListToRedis(name, carts);
            //5.清除cookie中的数据
            CookieUtil.deleteCookie(request, response, "cartList");
            return carts;
        }
    }

}

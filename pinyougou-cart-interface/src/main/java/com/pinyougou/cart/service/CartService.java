package com.pinyougou.cart.service;

import entity.Cart;

import java.util.List;

public interface CartService {
    /**
     * 向已有的购物车添加商品
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);

    List<Cart> findCartListFromRedis(String name);

    void saveCartListToRedis(String name, List<Cart> newestList);

    List<Cart> mergeCartList(List<Cart> cookieList, List<Cart> redisList);
}

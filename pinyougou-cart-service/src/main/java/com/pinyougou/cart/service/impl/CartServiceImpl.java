package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import entity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 向已有的购物车添加商品
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品的ID 查询商品的数据
        TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
        //2.获取商品数据中的商家的ID sellerID
        String sellerId = tbItem.getSellerId();
        //3.判断已有的购物车中 是否有商家id
        //  如果没有 直接添加商品;
        //  如果有 再判断该商家中是否有相同的商品
        Cart cart = findCartBySellerId(sellerId, cartList);
        if (cart == null) {
            //没有商家
            cart = new Cart();

            cart.setSellerId(sellerId);//店铺id
            cart.setSellerName(tbItem.getSeller());//店铺名

            List<TbOrderItem> orderitemlist = new ArrayList<>();//明细列表

            TbOrderItem orderItem = new TbOrderItem();//明细列表中的购物车单品信息

            //补充属性
            //设置他的属性
            orderItem.setItemId(itemId);
            orderItem.setGoodsId(tbItem.getGoodsId());
            orderItem.setTitle(tbItem.getTitle());
            orderItem.setPrice(tbItem.getPrice());
            orderItem.setNum(num);//传递过来的购买的数量
            double v = num * tbItem.getPrice().doubleValue();
            orderItem.setTotalFee(new BigDecimal(v));//金额
            orderItem.setPicPath(tbItem.getImage());//商品的图片路径

            orderitemlist.add(orderItem);
            cart.setOrderItemList(orderitemlist);
            cartList.add(cart);
        } else {
            //已有商家 判断商家明细列表是否有该商品
            //  如果有，数量相加
            //  如果没有，直接添加
            List<TbOrderItem> orderItemList = cart.getOrderItemList();//明细列表
            TbOrderItem orderItem = findOrderItemByItemId(itemId, orderItemList);

            if (orderItem == null) {
                //没有商品 直接添加
                TbOrderItem tbOrderItem = new TbOrderItem();//明细列表中的购物车单品信息

                //设置他的属性
                tbOrderItem.setItemId(itemId);
                tbOrderItem.setGoodsId(tbItem.getGoodsId());
                tbOrderItem.setTitle(tbItem.getTitle());
                tbOrderItem.setPrice(tbItem.getPrice());
                tbOrderItem.setNum(num);//传递过来的购买的数量
                double v = num * tbItem.getPrice().doubleValue();
                tbOrderItem.setTotalFee(new BigDecimal(v));//金额
                tbOrderItem.setPicPath(tbItem.getImage());//商品的图片路径
                orderItemList.add(tbOrderItem);
            } else {
                //已有商品 数量相加
                orderItem.setNum(orderItem.getNum() + num);//数量相加
                //金额重新计算  数量* 单价
                double v = orderItem.getNum() * orderItem.getPrice().doubleValue();
                orderItem.setTotalFee(new BigDecimal(v));//重新设置

                //判断如果商品的购买数量为0 表示不买了，就要删除商品
                //  （消减购物车中的商品也是走添加购物车方法（添加数量为-1））
                if (orderItem.getNum() == 0) {
                    orderItemList.remove(orderItem);
                }

                //如果明细列表长度为空 说明 用户没购买该商家的商品就直接删除对象
                if (orderItemList.size() == 0) {
                    cartList.remove(cart);//商家也删除了
                }

            }

        }
        return cartList;
    }

    /**
     * 从redis中获取已有购物车数据
     * @param name
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String name) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("CART_REDIS_KEY").get(name);
        if (cartList == null) {
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    /**
     * 将购物车数据存储到redis中
     * @param name
     * @param newestList
     */
    @Override
    public void saveCartListToRedis(String name, List<Cart> newestList) {
        redisTemplate.boundHashOps("CART_REDIS_KEY").put(name, newestList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cookieList, List<Cart> redisList) {
        for (Cart cart : cookieList) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                redisList = addGoodsToCartList(redisList, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return redisList;
    }

    private TbOrderItem findOrderItemByItemId(Long itemId, List<TbOrderItem> orderItemList) {
        for (TbOrderItem orderItem : orderItemList) {
            //Long类型==比较的是内存地址，需转换成 long 类型 再==
            if (orderItem.getItemId().longValue() == itemId) {
                return orderItem;
            }
        }
        return null;
    }

    private Cart findCartBySellerId(String sellerId, List<Cart> cartList) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }
}

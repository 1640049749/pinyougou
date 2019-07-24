package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/pay")
public class PayController {

    @Reference
    private WeixinPayService weixinPayService;

    @Reference
    private SeckillOrderService seckillOrderService;

    /**
     * 生成二维码
     *
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        //1.获取用户id
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //2.从redis中获取预订单 获取预订单的金额和支付订单号
        TbSeckillOrder seckillOrder = seckillOrderService.findOrderByUserId(userId);
        if (seckillOrder != null) {
            //3.调用服务，（内部实现调用统一下单api）
            double v = seckillOrder.getMoney().doubleValue() * 100;
            long fen = (long) v;
            return weixinPayService.createNative(seckillOrder.getId() + "", fen + "");
        } else {
            return new HashMap<>();
        }

    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        Result result = null;
        //有一个超时时间 如果过了5分钟还没支付就表示超时
        int count = 0;

        while (true) {
            //调用查询接口
            count++;

            if (count >= 100) {
                result = new Result(false, "支付超时");

                //删除微信订单
                Map<String,String> map = weixinPayService.closePay(out_trade_no);
                if ("SUCCESS".equals(map.get("result_code"))) {
                    seckillOrderService.deleteOrder(userId);
                } else if ("ORDERPAID".equals(map.get("err_code"))) {
                    //已经支付则更新入库
                    seckillOrderService.updateOrderStatus(map.get("transaction_id"),userId);
                } else {
                    //...
                }
            }
            Map<String, String> map = weixinPayService.queryPayStatus(out_trade_no);
            if (map == null) {//出错
                result = new Result(false, "支付出错");
                break;
            }
            if (map.get("trade_state").equals("SUCCESS")) {//如果成功
                result = new Result(true, "支付成功");
                /**
                 * 更新redis中的预订单状态（status 支付时间 更新微信的交易流水）
                 * 更新到数据库中
                 * 删除redis中的预订单
                 */
                seckillOrderService.updateOrderStatus(map.get("transaction_id"), userId);
                break;
            }
            try {
                Thread.sleep(3000);//间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}

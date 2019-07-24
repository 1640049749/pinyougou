package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/pay")
public class PayController {

    @Reference
    private WeixinPayService weixinPayService;

    @Reference
    private OrderService orderService;

    /**
     * 生成二维码
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative(){
//        //1.生成一个（支付）订单
//        String out_trade_no = new IdWorker().nextId() + "";
//        //2.获取商品的总金额（先写死）
//        String total_fee = "1";//单位：分

        //1.获取用户的id
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //2.调用orderService的服务 从redis中获取支付日志 对象
        TbPayLog payLog = orderService.getPayLogByUserId(userId);
        //3.获取支付订单号 和 金额

        //3.调用服务，（内部实现调用统一下单api）
        return weixinPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        Result result = null;
        //有一个超时时间 如果过了5分钟还没支付就表示超时
        int count = 0;

        while (true) {
            //调用查询接口
            count++;

            if (count >= 100) {
                result = new Result(true, "支付超时");
            }
            Map<String, String> map = weixinPayService.queryPayStatus(out_trade_no);
            if (map == null) {//出错
                result = new Result(false, "支付出错");
                break;
            }
            if (map.get("trade_state").equals("SUCCESS")) {//如果成功
                result = new Result(true, "支付成功");

                orderService.updateStatus(out_trade_no,map.get("transaction_id"));
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

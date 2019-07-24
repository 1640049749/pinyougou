package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.util.HttpClient;
import com.pinyougou.pay.service.WeixinPayService;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    /**
     * 生成微信支付二维码
     * @param out_trade_no 订单号
     * @param total_fee 金额(分)
     * @return
     */
    @Override
    public Map<String, String> createNative(String out_trade_no, String total_fee) {
        try {
            //1.组合参数集，存储到map中，map转换成xml
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("appid", "wx8397f8696b538317");//公众号(传智播客)
            paramMap.put("mch_id", "1473426802");//商户号 财付通平台的商户账号
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
            paramMap.put("body", "品优购");//商品描述
            paramMap.put("out_trade_no", out_trade_no);//商户订单号
            paramMap.put("total_fee",total_fee);//总金额（分）
            paramMap.put("spbill_create_ip", "127.0.0.1");//IP
            paramMap.put("notify_url", "http://a31ef7db.ngrok.io/WeChatPay/WeChatPayNotify");//回调地址(随便写)
            paramMap.put("trade_type", "NATIVE");//交易类型

            //自动添加签名，而且转成字符串（T6m9iK73b0kn9g5v426MKfHQH7X8rKwb 财付通平台的商户密钥）
            String xmlParam = WXPayUtil.generateSignedXml(paramMap, "T6m9iK73b0kn9g5v426MKfHQH7X8rKwb");

            //2.使用httpclient调用接口 发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);//请求体
            httpClient.post();

            //3.获取结果集xml，转换成map
            String resultXml = httpClient.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(resultXml);

            //优化结果集
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("code_url", map.get("code_url"));
            resultMap.put("out_trade_no",out_trade_no);
            resultMap.put("total_fee", total_fee);
            //4.返回map
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 根据订单号 查询支付状态
     * @param out_trade_no
     * @return
     */
    @Override
    public Map<String, String> queryPayStatus(String out_trade_no) {
        try {
            //1.组合参数集，存储到map中，map转换成xml
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("appid", "wx8397f8696b538317");//公众号(传智播客)
            paramMap.put("mch_id", "1473426802");//商户号 财付通平台的商户账号
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
            paramMap.put("out_trade_no", out_trade_no);//商户订单号

            //自动添加签名，而且转成字符串（T6m9iK73b0kn9g5v426MKfHQH7X8rKwb 财付通平台的商户密钥）
            String xmlParam = WXPayUtil.generateSignedXml(paramMap, "T6m9iK73b0kn9g5v426MKfHQH7X8rKwb");

            //2.使用httpclient调用接口 发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);//请求体
            httpClient.post();

            //3.获取结果集xml，转换成map
            String resultXml = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);

            //4.返回map
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map<String, String> closePay(String out_trade_no) {
        try {
            //参数设置
            Map<String,String> paramMap = new HashMap<String,String>();
            paramMap.put("appid","wx8397f8696b538317"); //应用ID
            paramMap.put("mch_id","1473426802");    //商户编号
            paramMap.put("nonce_str",WXPayUtil.generateNonceStr());//随机字符
            paramMap.put("out_trade_no",out_trade_no);   //商家的唯一编号

            //将Map数据转成XML字符
            String xmlParam = WXPayUtil.generateSignedXml(paramMap,"T6m9iK73b0kn9g5v426MKfHQH7X8rKwb");

            //确定url
            String url = "https://api.mch.weixin.qq.com/pay/closeorder";

            //发送请求
            HttpClient httpClient = new HttpClient(url);
            //https
            httpClient.setHttps(true);
            //提交参数
            httpClient.setXmlParam(xmlParam);

            //提交
            httpClient.post();

            //获取返回数据
            String content = httpClient.getContent();

            //将返回数据解析成Map
            return  WXPayUtil.xmlToMap(content);

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }
}

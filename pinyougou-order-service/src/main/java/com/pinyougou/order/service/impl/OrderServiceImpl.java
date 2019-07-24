package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.core.service.CoreServiceImpl;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import entity.Cart;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;




/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl extends CoreServiceImpl<TbOrder>  implements OrderService {
	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbItemMapper itemMapper;


	@Autowired
	private TbOrderItemMapper orderItemMapper;

	@Autowired
	private TbPayLogMapper payLogMapper;

	private TbOrderMapper orderMapper;

	@Autowired
	public OrderServiceImpl(TbOrderMapper orderMapper) {
		super(orderMapper, TbOrder.class);
		this.orderMapper=orderMapper;
	}

	/**
	 * 订单拆分 新增订单
	 * @param order
	 */
	@Override
	public void add(TbOrder order) {
		//获取页面传递的数据

		//2.插入到订单表中 拆单(一个商家就是一个订单) 订单的id要生成
		//2.1获取redis中的购物车数据
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("CART_REDIS_KEY").get(order.getUserId());

		double totalMoney = 0;

		List<String> orderIdList = new ArrayList<>();

		//2.2循环遍历 每一个Cart对象就是一个商家
		for (Cart cart : cartList) {
			//3.插入到订单表中（每一个商家对象对应着一个订单）

			long orderId = idWorker.nextId();

			orderIdList.add(orderId + "");
			TbOrder tbOrder=new TbOrder();//新创建订单对象
			tbOrder.setOrderId(orderId);//订单ID
			tbOrder.setUserId(order.getUserId());//用户名
			tbOrder.setPaymentType(order.getPaymentType());//支付类型
			tbOrder.setStatus("1");//状态：未付款
			tbOrder.setCreateTime(new Date());//订单创建日期
			tbOrder.setUpdateTime(new Date());//订单更新日期
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());//收货人地址
			tbOrder.setReceiverMobile(order.getReceiverMobile());//收货人手机号
			tbOrder.setReceiverZipCode("518000");//收货人邮编
			tbOrder.setReceiver(order.getReceiver());//收货人
			tbOrder.setSourceType("2");//订单来源,写死2--PC端
			tbOrder.setSellerId(cart.getSellerId());//商家ID

			//循环购物车明细
			double money=0;
			for(TbOrderItem orderItem :cart.getOrderItemList()){
				//4.插入到订单选项表中
				orderItem.setId(idWorker.nextId());
				orderItem.setOrderId( orderId  );//订单ID
				orderItem.setSellerId(cart.getSellerId());
				TbItem item = itemMapper.selectByPrimaryKey(orderItem.getItemId());//商品
				orderItem.setGoodsId(item.getGoodsId());//设置商品的SPU的ID

				money+=orderItem.getTotalFee().doubleValue();//金额累加

				orderItemMapper.insert(orderItem);
			}
			tbOrder.setPayment(new BigDecimal(money));//实付金额。精确到2位小数;单位:元。如:200.07，表示:200元7分

			totalMoney += money;

			orderMapper.insert(tbOrder);
		}
		//添加支付的日志
		TbPayLog payLog = new TbPayLog();

		payLog.setOutTradeNo(idWorker.nextId() + "");
		payLog.setCreateTime(new Date());
		long fen = (long) (totalMoney * 100);//fen
		payLog.setTotalFee(fen);
		payLog.setUserId(order.getUserId());
		payLog.setTradeState("0");//未支付
		// 38,37
		payLog.setOrderList(orderIdList.toString().replace("[", "").replace("]", ""));//[1,2]
		payLog.setPayType(order.getPaymentType());//微信支付
		payLogMapper.insert(payLog);
		//存储到redis中  bigKey field  value
		redisTemplate.boundHashOps("TbPayLog").put(order.getUserId(), payLog);

		//移除掉redis的购物车数据
		redisTemplate.boundHashOps("CART_REDIS_KEY").delete(order.getUserId());

	}

	/**
	 * 从redis中获取支付日志信息
	 * @param userId
	 * @return
	 */
	@Override
	public TbPayLog getPayLogByUserId(String userId) {
		return (TbPayLog) redisTemplate.boundHashOps("TbPayLog").get(userId);
	}

	/**
	 * 修改订单状态
	 * @param out_trade_no 支付订单号
	 * @param transaction_id 微信返回的交易流水号
	 */
	@Override
	public void updateStatus(String out_trade_no, String transaction_id) {
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		//* 修改支付日志状态
		payLog.setPayTime(new Date());
		payLog.setTradeState("1");//已支付
		payLog.setTransactionId(transaction_id);//交易号
		payLogMapper.updateByPrimaryKey(payLog);

		//* 修改关联的订单的状态
		//2.1获取订单号列表
		String orderList = payLog.getOrderList();
		//2.2获取订单号数组
		String[] orderIds = orderList.split(",");
		for (String orderId : orderIds) {
			TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.valueOf(orderId));
			if (tbOrder != null) {
				tbOrder.setStatus("2");//已付款
				orderMapper.updateByPrimaryKey(tbOrder);
			}
		}
		//* 清除缓存中的支付日志对象
		redisTemplate.boundHashOps("TbPayLog").delete(payLog.getUserId());
	}


	@Override
    public PageInfo<TbOrder> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo,pageSize);
        List<TbOrder> all = orderMapper.selectAll();
        PageInfo<TbOrder> info = new PageInfo<TbOrder>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbOrder> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }



	 @Override
    public PageInfo<TbOrder> findPage(Integer pageNo, Integer pageSize, TbOrder order) {
        PageHelper.startPage(pageNo,pageSize);

        Example example = new Example(TbOrder.class);
        Example.Criteria criteria = example.createCriteria();

        if(order!=null){			
						if(StringUtils.isNotBlank(order.getPaymentType())){
				criteria.andLike("paymentType","%"+order.getPaymentType()+"%");
				//criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(StringUtils.isNotBlank(order.getPostFee())){
				criteria.andLike("postFee","%"+order.getPostFee()+"%");
				//criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(StringUtils.isNotBlank(order.getStatus())){
				criteria.andLike("status","%"+order.getStatus()+"%");
				//criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(StringUtils.isNotBlank(order.getShippingName())){
				criteria.andLike("shippingName","%"+order.getShippingName()+"%");
				//criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(StringUtils.isNotBlank(order.getShippingCode())){
				criteria.andLike("shippingCode","%"+order.getShippingCode()+"%");
				//criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(StringUtils.isNotBlank(order.getUserId())){
				criteria.andLike("userId","%"+order.getUserId()+"%");
				//criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(StringUtils.isNotBlank(order.getBuyerMessage())){
				criteria.andLike("buyerMessage","%"+order.getBuyerMessage()+"%");
				//criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(StringUtils.isNotBlank(order.getBuyerNick())){
				criteria.andLike("buyerNick","%"+order.getBuyerNick()+"%");
				//criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(StringUtils.isNotBlank(order.getBuyerRate())){
				criteria.andLike("buyerRate","%"+order.getBuyerRate()+"%");
				//criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(StringUtils.isNotBlank(order.getReceiverAreaName())){
				criteria.andLike("receiverAreaName","%"+order.getReceiverAreaName()+"%");
				//criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(StringUtils.isNotBlank(order.getReceiverMobile())){
				criteria.andLike("receiverMobile","%"+order.getReceiverMobile()+"%");
				//criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(StringUtils.isNotBlank(order.getReceiverZipCode())){
				criteria.andLike("receiverZipCode","%"+order.getReceiverZipCode()+"%");
				//criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(StringUtils.isNotBlank(order.getReceiver())){
				criteria.andLike("receiver","%"+order.getReceiver()+"%");
				//criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(StringUtils.isNotBlank(order.getInvoiceType())){
				criteria.andLike("invoiceType","%"+order.getInvoiceType()+"%");
				//criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(StringUtils.isNotBlank(order.getSourceType())){
				criteria.andLike("sourceType","%"+order.getSourceType()+"%");
				//criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(StringUtils.isNotBlank(order.getSellerId())){
				criteria.andLike("sellerId","%"+order.getSellerId()+"%");
				//criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
        List<TbOrder> all = orderMapper.selectByExample(example);
        PageInfo<TbOrder> info = new PageInfo<TbOrder>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbOrder> pageInfo = JSON.parseObject(s, PageInfo.class);

        return pageInfo;
    }



}

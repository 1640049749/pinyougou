package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.SysConstants;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.core.service.CoreServiceImpl;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.pojo.SeckillStatus;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.seckill.thread.OrderHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;


/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class SeckillOrderServiceImpl extends CoreServiceImpl<TbSeckillOrder> implements SeckillOrderService {


    private TbSeckillOrderMapper seckillOrderMapper;

    @Autowired
    public SeckillOrderServiceImpl(TbSeckillOrderMapper seckillOrderMapper) {
        super(seckillOrderMapper, TbSeckillOrder.class);
        this.seckillOrderMapper = seckillOrderMapper;
    }


    @Override
    public PageInfo<TbSeckillOrder> findPage(Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<TbSeckillOrder> all = seckillOrderMapper.selectAll();
        PageInfo<TbSeckillOrder> info = new PageInfo<TbSeckillOrder>(all);

        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbSeckillOrder> pageInfo = JSON.parseObject(s, PageInfo.class);
        return pageInfo;
    }


    @Override
    public PageInfo<TbSeckillOrder> findPage(Integer pageNo, Integer pageSize, TbSeckillOrder seckillOrder) {
        PageHelper.startPage(pageNo, pageSize);

        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();

        if (seckillOrder != null) {
            if (StringUtils.isNotBlank(seckillOrder.getUserId())) {
                criteria.andLike("userId", "%" + seckillOrder.getUserId() + "%");
                //criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
            }
            if (StringUtils.isNotBlank(seckillOrder.getSellerId())) {
                criteria.andLike("sellerId", "%" + seckillOrder.getSellerId() + "%");
                //criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
            }
            if (StringUtils.isNotBlank(seckillOrder.getStatus())) {
                criteria.andLike("status", "%" + seckillOrder.getStatus() + "%");
                //criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
            }
            if (StringUtils.isNotBlank(seckillOrder.getReceiverAddress())) {
                criteria.andLike("receiverAddress", "%" + seckillOrder.getReceiverAddress() + "%");
                //criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
            }
            if (StringUtils.isNotBlank(seckillOrder.getReceiverMobile())) {
                criteria.andLike("receiverMobile", "%" + seckillOrder.getReceiverMobile() + "%");
                //criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
            }
            if (StringUtils.isNotBlank(seckillOrder.getReceiver())) {
                criteria.andLike("receiver", "%" + seckillOrder.getReceiver() + "%");
                //criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
            }
            if (StringUtils.isNotBlank(seckillOrder.getTransactionId())) {
                criteria.andLike("transactionId", "%" + seckillOrder.getTransactionId() + "%");
                //criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
            }

        }
        List<TbSeckillOrder> all = seckillOrderMapper.selectByExample(example);
        PageInfo<TbSeckillOrder> info = new PageInfo<TbSeckillOrder>(all);
        //序列化再反序列化
        String s = JSON.toJSONString(info);
        PageInfo<TbSeckillOrder> pageInfo = JSON.parseObject(s, PageInfo.class);

        return pageInfo;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderHandler orderHandler;

    @Override
    public void submitOrder(Long id, String userId) {
        //1.根据Id从 redis中获取秒杀商品信息
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).get(id);

        //先判断用户是否在排队中
        Object o2 = redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).get(userId);
        if (o2 != null) {
            throw new RuntimeException("正在排队中");
        }
        //先判断是否有未支付的订单
        Object o1 = redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).get(userId);
        if (o1 != null) {
            throw new RuntimeException("有未支付的订单");
        }

        //2.判断商品是否已经售尽，如果售罄 抛出异常
//        if (seckillGoods == null || seckillGoods.getStockCount() < 0) {
//            throw new RuntimeException("卖完了");
//        }
        //弹出队列中的商品
        Object o = redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX + id).rightPop();
        if (o == null) {
            throw new RuntimeException("卖完了");
        }

        //将用户压入队列
        redisTemplate.boundListOps(SysConstants.SEC_KILL_USER_ORDER_LIST).leftPush(new SeckillStatus(userId,id,SeckillStatus.SECKILL_queuing));

        //此时用户已经进入队列中，下单需要时间（10s) 如果用户再次抢购，会再次执行创建订单的方法
        //需标记用户 已经进入到排队中 存入redis中  10s内再次抢购 查询redis会返回正在排队 10s后订单创建成功会消除标记
        redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).put(userId, id);

        //调用多线程
        orderHandler.orderHandler();
    }

    /**
     * 根据用户id查找redis中的域订单
     * @param userId
     * @return
     */
    @Override
    public TbSeckillOrder findOrderByUserId(String userId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).get(userId);
    }

    /**
     * 支付成功 修改预订单中的状态
     * @param transaction_id
     * @param userId
     */
    @Override
    public void updateOrderStatus(String transaction_id, String userId) {
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).get(userId);
        if (seckillOrder != null) {
            seckillOrder.setPayTime(new Date());
            seckillOrder.setStatus("1");
            seckillOrder.setTransactionId(transaction_id);
            //存储到数据库中
            seckillOrderMapper.insert(seckillOrder);
            //删除预订单
            redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).delete(userId);
        }
    }

    /**
     * 支付超时 删除redis中的预订单
     * @param userId
     */
    @Override
    public void deleteOrder(String userId) {
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).get(userId);
        if(seckillOrder==null){
            System.out.println("没有该订单");
            return;
        }
        //1.恢复redis中的商品库存
        Long seckillId = seckillOrder.getSeckillId();
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).get(seckillId);
        if (seckillGoods == null) {
            //从数据库获取秒杀商品 此时商品库存为0（前面创建订单后 当商品剩余库存为0就同步到数据库中 并且从redis中删除）
            seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillId);
            seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
            //重新存储到REDIS中
            redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).put(seckillId, seckillGoods);
            //更新回数据库中
            seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
        } else {
            seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
            redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).put(seckillId, seckillGoods);
        }

        //2.删除预订单
        redisTemplate.boundHashOps(SysConstants.SEC_KILL_ORDER).delete(userId);
        //3.恢复队列
        redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX+seckillId).leftPush(seckillId);
    }

}

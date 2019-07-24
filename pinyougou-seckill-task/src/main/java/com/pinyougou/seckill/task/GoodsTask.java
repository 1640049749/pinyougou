package com.pinyougou.seckill.task;

import com.pinyougou.common.pojo.SysConstants;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class GoodsTask {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    /**
     * 一个被反复执行的方法
     * 通过注解 来指定 cron表达式
     * "0/30 * * * * ?"    每隔30秒执行一次
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void pusGoods() {
        //注入dao
        //注入redis

        //执行查询语句 符合条件的查询语句
        //select * from tb_seckill_good where status=1 and stock_count>0 and 开始时间<当前时间<结束时间 and id not in (redis中已经有的)
        Example example = new Example(TbSeckillGoods.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("status", "1");//审核通过的
        criteria.andGreaterThan("stockCount", 0);//剩余库存数大于0
        Date date = new Date();
        criteria.andLessThan("startTime", date);
        criteria.andGreaterThan("endTime", date);

        //排除 已经在redis中的商品
        Set<Long> keys = redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).keys();

        if (keys != null && keys.size() > 0) {
            criteria.andNotIn("id", keys);
        }

        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);

        //全部存储到redis中
        for (TbSeckillGoods tbSeckillGoods : seckillGoodsList) {
            pushGoodsList(tbSeckillGoods);//将商品库存存入队列中
            redisTemplate.boundHashOps(SysConstants.SEC_KILL_GOODS).put(tbSeckillGoods.getId(), tbSeckillGoods);

        }
    }

    /**
     * 一个队列就是一种商品
     * 队列的长度 就是商品的库存量
     * @param tbSeckillGoods
     */
    private void pushGoodsList(TbSeckillGoods tbSeckillGoods) {
        for (Integer integer = 0; integer < tbSeckillGoods.getStockCount(); integer++) {
            redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX + tbSeckillGoods.getId()).leftPush(tbSeckillGoods.getId());
        }
    }
}

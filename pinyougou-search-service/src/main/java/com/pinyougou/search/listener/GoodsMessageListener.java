package com.pinyougou.search.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.common.pojo.MessageInfo;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 监听器的作用:
 * 1.获取消息
 * 2.获取消息的内容 转换数据
 * 3.更新索引库
 */
public class GoodsMessageListener implements MessageListenerConcurrently {
    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        try {
            if (msgs != null) {
                //1.循环遍历消息
                for (MessageExt msg : msgs) {
                    //2.获取消息体
                    byte[] body = msg.getBody();
                    //3.转成字符串 MessageInfo
                    String jsonstr = new String(body);
                    //4.转成对象
                    MessageInfo messageInfo = JSON.parseObject(jsonstr, MessageInfo.class);
                    //5.判断 对象中的执行的类型(ADD /DELETE /UPDATE)
                    switch (messageInfo.getMethod()) {
                        case 1: {// ADD

                            break;
                        }
                        case 2: { //update
                            Object context1 = messageInfo.getContext();//商品的数据
                            //转成字符串
                            String contexstr = context1.toString();//[{},{},{},{}]
                            //转成json对象
                            List<TbItem> itemList = JSON.parseArray(contexstr, TbItem.class);
                            itemSearchService.updateIndex(itemList);
                            break;
                        }
                        case 3: {//delete
                            Object context1 = messageInfo.getContext();//Long[] ----ids 数据了
                            String contexstr = context1.toString();//
                            Long[] ids = JSON.parseObject(contexstr, Long[].class);
                            itemSearchService.deleteByIds(ids);
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }
}

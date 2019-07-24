package com.pinyougou;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.util.List;

public class ConsumerTest {
    public static void main(String[] args) throws Exception {
        //1.创建消费者 指定 消费者组
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer_cluster_group1");
        //2.设置nameserver地址
        consumer.setNamesrvAddr("192.168.25.129:9876");
        //3.设置订阅的主题.
        //参数1 指定主题
        //参数2 指定主题里面的TAG 指定具体的TAG 或者使用表达式  * 表示所有的TAG
        consumer.subscribe("TopicTest","*");
        //4.设置消费的模式(1.集群模式CLUSTERING(默认)2.广播模式BROADCASTING)
        consumer.setMessageModel(MessageModel.CLUSTERING);
        //5.设置监听器(目的监听主题 获取里面的消息)
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                //获取消息
                try {
                    if(msgs!=null){
                        for (MessageExt msg : msgs) {
                            System.out.println("topic:"+msg.getTopic());
                            System.out.println("tag:"+msg.getTags());
                            System.out.println("keys:"+msg.getKeys());
                            byte[] body = msg.getBody();
                            String messageinfo = new String(body);
                            System.out.println("哈哈11111111:"+messageinfo);
                        }
                    }
                    //消费成功
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } catch (Exception e) {
                    e.printStackTrace();
                    //重新消费
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
        });
        //6.开始连接
        consumer.start();

        //7.关闭资源(不管)
        // consumer.shutdown();
    }
}

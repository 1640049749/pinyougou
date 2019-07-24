package com.pinyougou;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

public class Producer {
    public static void main(String[] args) throws Exception {
        //1.创建生产者 并指定组名
        DefaultMQProducer producer = new DefaultMQProducer("producer_cluster_group1");
        //2.设置nameserver地址
        producer.setNamesrvAddr("192.168.25.129");
        //3.开启连接
        producer.start();
        //4.发送消息
        String messageinfo ="hello world";
        /**
         * 参数1:消息的(大的业务)主题
         * 参数2:消息的标签(小分类)
         * 参数3:key 业务的唯一标识
         * 参数4:消息体(消息内容)
         */
        Message msg = new Message("TopicTest","TagA","唯一的key",messageinfo.getBytes(RemotingHelper.DEFAULT_CHARSET));
        producer.send(msg);
        //5.关闭连接
        producer.shutdown();
    }
}

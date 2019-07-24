package com.pinyougou;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration("classpath:spring-mq-producer.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ProducerTest {

    @Autowired
    private DefaultMQProducer producer;

    @Test
    public void sendMessage() throws Exception {
        Message msg = new Message("TopicSpringTest", "TAGA", "唯一的KEY", "哈哈哈spring message".getBytes(RemotingHelper.DEFAULT_CHARSET));
        producer.send(msg);
        //SendResult sendResult = producer.send(msg);
        //System.out.println(sendResult.getMsgId());
        Thread.sleep(10000000);
    }
}

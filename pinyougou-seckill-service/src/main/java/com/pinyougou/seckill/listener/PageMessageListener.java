package com.pinyougou.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.common.pojo.MessageInfo;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 监听器 目的就是监听秒杀商品的id 生成静态页面
 */
public class PageMessageListener implements MessageListenerConcurrently {

    @Autowired
    private FreeMarkerConfigurer configurer;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        if (msgs != null) {
            //1.获取消息体
            for (MessageExt msg : msgs) {
                byte[] body = msg.getBody();
                //2.转换成string
                String s = new String(body);
                //3.转换成json对象
                MessageInfo messageInfo = JSON.parseObject(s, MessageInfo.class);

                //4.判断是否是ADD 生成静态页
                switch (messageInfo.getMethod()) {
                    case 1://add
                    {
                        //获取对象
                        Long[] ids = JSON.parseObject(messageInfo.getContext().toString(), Long[].class);
                        //查询数据库数据
                        //生成静态页
                        for (Long id : ids) {
                            genHTML("item.ftl", id);
                        }

                        break;
                    }
                    case 2://update
                    {

                        break;
                    }
                    case 3://delete
                    {

                        break;
                    }
                    default: {
                        break;
                    }
                }

            }


        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    @Value("PageDir")
    private String pageDir;

    /**
     * 生成静态页的方法
     *
     * @param templateName
     * @param id
     */
    private void genHTML(String templateName, Long id) {
        FileWriter fileWriter = null;
        try {
            //1.创建一个配置类Configuration
            //2.设置字符编码utf-8
            //3.设置模板所在的目录
            //4.创建模板文件
            //（上面已在spring集成freemarker中配置）
            Configuration configuration = configurer.getConfiguration();
            //5.加载模板对象
            Template template = configuration.getTemplate(templateName);
            //6.从数据库获取数据
            Map<String, Object> model = new HashMap();
            TbSeckillGoods seckillGoods = seckillGoodsMapper.selectByPrimaryKey(id);
            model.put("seckillGoods", seckillGoods);
            //7.创建一个writer
            fileWriter = new FileWriter(new File(pageDir + id + ".html"));
            //8.处理生成页面
            template.process(model, fileWriter);
            //9.关流
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

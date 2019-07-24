package com.pinyougou;

import com.pinyougou.es.service.ImportService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        //1.初始化spring容器
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-es.xml");
        //2.从spring容器中获取service
        ImportService importService = context.getBean(ImportService.class);
        //3.调用service方法即可
        importService.importDBToES();
    }
}

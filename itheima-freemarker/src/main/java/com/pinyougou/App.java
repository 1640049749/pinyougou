package com.pinyougou;

import com.pinyougou.pojo.Person;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.WildcardType;
import java.util.*;

/**
 * Hello world!
 */
public class App {
    // 模板文件  + 数据集 = html
    public static void main(String[] args)  throws Exception{
        //1.创建配置类configuration 设置文件的编码 以及 模板文件所在的位置
        Configuration configuration = new Configuration(Configuration.getVersion());
        configuration.setDefaultEncoding("utf-8");
        configuration.setDirectoryForTemplateLoading(new File("C:\\Users\\Administrator\\IdeaProjects\\60pinyougou\\itheima-freemarker\\src\\main\\resources\\template"));
        //2.创建模板文件 官方推荐使用.ftl后缀 可以任意的。


        //3.加载模板文件 获取模板对象
        //模板名称 是相对路径
        Template template = configuration.getTemplate("template.ftl");


        //4.创建数据集（将来从数据库查询出来） map

        Map<String,Object> model = new HashMap<>();

        model.put("name","你好帅哥");
        model.put("user",new Person(1000L,"特朗普"));

        List<Person> list = new ArrayList<>();
        list.add(new Person(101L,"刘备"));
        list.add(new Person(102L,"张飞"));
        list.add(new Person(103L,"光羽"));
        model.put("mylist",list);

        model.put("date",new Date());

        model.put("nullkey","世上本没有路，走的人多了就有了路了");



        //5.创建输出流 指定输出的文件的位置
        FileWriter writer = new FileWriter(new File("C:\\Users\\Administrator\\IdeaProjects\\60pinyougou\\itheima-freemarker\\src\\main\\resources\\output\\1234.html"));

        //6.执行输出的动作 生成静态页面
        template.process(model,writer);

        //7.关闭流
        writer.close();


    }
}

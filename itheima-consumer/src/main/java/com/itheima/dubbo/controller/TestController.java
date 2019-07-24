package com.itheima.dubbo.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.dubbo.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.itheima.dubbo.controller *
 * @since 1.0
 */
@Controller
@RequestMapping("/test")
public class TestController {
//    @Autowired
//    private TestService testService;
    @Reference
    private TestService testService;

    @RequestMapping("/sayhello")
    @ResponseBody
    public String sayhello(){
        return testService.sayHello();
    }
}

package com.itheima.dubbo.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.dubbo.service.TestService;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.itheima.dubbo.service.impl *
 * @since 1.0
 */

@Service
public class TestServiceImpl implements TestService {
    @Override
    public String sayHello() {
        return "hello world111";
    }
}

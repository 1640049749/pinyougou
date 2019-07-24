package com.itheima.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Component;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.itheima.security.config *
 * @since 1.0
 */
@EnableWebSecurity//开启springseuciryt的自动的配置项 ： 自动的配置
public class MySecurityConfig  extends WebSecurityConfigurerAdapter{

    //认证 和 授权
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //设置用户名和密码和角色
        //ROLE_  默认会自动拼接  不需要写前缀
        auth.inMemoryAuthentication().withUser("admin").password("{noop}admin").roles("ADMIN");
        //super.configure(auth);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //使用默认的配置

        http.authorizeRequests()
                .antMatchers("/login.html","/error.html").permitAll()
                // admin/** 资源 只有拥有了admin的角色人才能访问
                .antMatchers("/admin/**").hasRole("ADMIN")
                // user/** 资源 只有拥有了user的角色人才能访问
                .antMatchers("/user/**").hasRole("USER")
                //其他的任意的请求 只有 认证过的（已经登录过的）用户才能访问
                // /**
                .anyRequest().authenticated();

        http.formLogin()
            .loginPage("/login.html")
            .loginProcessingUrl("/login")
            .defaultSuccessUrl("/index.jsp",true)
            .failureUrl("/error.html");

        //禁用CSRF
        http.csrf().disable();
    }
}

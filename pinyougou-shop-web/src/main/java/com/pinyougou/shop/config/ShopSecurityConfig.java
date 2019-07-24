package com.pinyougou.shop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.shop.config *
 * @since 1.0
 */
@EnableWebSecurity
public class ShopSecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication().withUser("user").password("{noop}123456").roles("USER");
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //1.放行css imgs。。。。
        http.authorizeRequests()
                .antMatchers("/*.html","/css/**","/img/**","/js/**","/plugins/**","/seller/add.shtml").permitAll()
        //2.拦截请求 配置权限
                .anyRequest().authenticated();
        //3.自定义登录的页面
        http.formLogin()
                .loginPage("/shoplogin.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/admin/index.html",true)
                .failureUrl("/shoplogin.html?error");
        //4.禁用csrf
        http.csrf().disable();
        //5.设置同源访问策略
        http.headers().frameOptions().sameOrigin();
    }
}

package com.pinyougou.manager.config;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * 描述
 *
 * @author 三国的包子
 * @version 1.0
 * @package com.pinyougou.manager.config *
 * @since 1.0
 */
@EnableWebSecurity//开启自动的security的配置项
public class ManagerSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("admin").password("{noop}admin").roles("ADMIN");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //1.拦截请求  admin/**
        http.authorizeRequests()
        //2.放行 css js login.html
                .antMatchers("/css/**","/img/**","/js/**","/plugins/**","/login.html").permitAll()
                //.antMatchers("/**").hasRole("ADMIN")
                .anyRequest().authenticated();
        //3.配置自定义的表单的登录
        http.formLogin()
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/admin/index.html",true)
                .failureUrl("/login.html?error");
        //4.csrf禁用
        http.csrf().disable();
        //5.配置 html设置为同源 访问策略
        http.headers().frameOptions().sameOrigin();

        //配置退出
        http.logout().logoutUrl("/logout").invalidateHttpSession(true);
    }
}

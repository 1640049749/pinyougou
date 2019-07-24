package cn.itcast.demo.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailServiceImpl implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //只做授权，不做认证（cas已认证）
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        //角色 和 spring-security.xml中配置的角色一致
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        return new User(username, "", grantedAuthorities);
    }
}

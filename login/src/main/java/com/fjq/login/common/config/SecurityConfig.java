package com.fjq.login.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 也可以传入强度参数，例如 new BCryptPasswordEncoder(12)
        // 默认强度是 10，数字越大，加密过程越慢，越能防范暴力破解
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 禁用 CSRF（如果是前后端分离项目，通常需要禁用）
        http.csrf(AbstractHttpConfigurer::disable)
                // 配置请求拦截规则
                .authorizeHttpRequests(auth -> auth
                        // 放行所有的 auth 接口 (发送验证码、登录等)
                        .requestMatchers("/api/v1/auth/**", "/api/auth/**").permitAll()
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}

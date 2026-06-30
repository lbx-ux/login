package com.fjq.login.common.config;

import com.fjq.login.common.interceptor.JwtTokenAdminInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfiguration implements WebMvcConfigurer {
    private final JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * 注册自定义拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/api/v1/auth/**")          // 拦截管理端所有的接口请求
                .excludePathPatterns(                  // 明确放行以下接口
                        "/api/v1/auth/login",       // 放行员工登录接口
                        "/api/v1/auth/register",// 放行员工注册接口（如果有的话）
                        "/api/v1/auth/send-code"
                );
    }
}

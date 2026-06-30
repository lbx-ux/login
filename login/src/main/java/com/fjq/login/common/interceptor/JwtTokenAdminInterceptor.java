package com.fjq.login.common.interceptor;

import com.fjq.login.common.context.BaseContext;
import com.fjq.login.common.properties.JwtProperties;
import com.fjq.login.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtTokenAdminInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    /**
     * 在请求到达 Controller 之前进行拦截校验
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 判断当前拦截到的是否为 Controller 的动态方法
        if (!(handler instanceof HandlerMethod)) {
            // 如果拦截到的是静态资源（如 html, css, js 等），直接放行
            return true;
        }

        // 2. 从请求头中动态获取令牌（名称由 yml 中的 token-name 决定）
        String tokenName = jwtProperties.getTokenName();
        String token = request.getHeader(tokenName);

        // 3. 校验令牌
        try {
            // 调用 0.12.x 版本的解析方法，如果过期或被篡改，此处会直接抛出异常
            Claims claims = jwtUtil.parseJWT(token);
            String email = claims.get("email", String.class);
            BaseContext.setCurrent(email);
            // 5. 校验通过，放行请求
            return true;

        } catch (Exception ex) {
            // 6. 校验失败，打断请求，向前端响应 401 状态码与标准错误 JSON
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            // 返回标准失败响应体格式
            response.getWriter().write("{\"code\":401, \"message\":\"未登录或Token已失效，请重新登录\"}");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理当前线程的 ThreadLocal 数据
        BaseContext.removeCurrent();
    }

}

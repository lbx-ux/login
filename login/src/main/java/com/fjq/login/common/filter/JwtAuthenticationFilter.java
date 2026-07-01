package com.fjq.login.common.filter;

import com.fjq.login.common.context.BaseContext;
import com.fjq.login.common.properties.JwtProperties;
import com.fjq.login.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // 直接注入你原有的工具类和配置属性
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. 从请求头中动态获取令牌
        String tokenName = jwtProperties.getTokenName();
        String token = request.getHeader(tokenName);

        // 2. 如果请求头中没有 Token，直接放行
        // 【注意】这里放行不代表安全验证通过！
        // 因为如果是去登录接口，本来就没 Token，需要放行；
        // 如果是去受保护的接口没带 Token，放行后，后面的 Spring Security 机制会拦截并报错。
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 校验令牌
        try {
            // 调用你原有的解析方法
            Claims claims = jwtUtil.parseJWT(token);
            String email = claims.get("email", String.class);

            // 4. 【新增】将用户信息告知 Spring Security
            // 如果解析成功，且 Spring Security 上下文中还没有认证信息
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 生成一个认证 Token (代表该用户已合法登录)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        email, // 存入用户的 email 作为身份标识
                        null,  // 凭证(密码)为 null，因为 JWT 已经证明了身份
                        Collections.emptyList() // 权限列表暂为空
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // 将认证信息存入 Security 上下文
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            // 5. 【保留】兼容你原有的 ThreadLocal
            // 这样你之前在 Controller 里用 BaseContext.getCurrent() 的代码就完全不用改了！
            BaseContext.setCurrent(email);

            // 6. 校验通过，放行请求，进入 Controller
            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            // 7. 【保留】校验失败，直接拦截并响应 401 状态码和 JSON 错误信息
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401, \"message\":\"未登录或Token已失效，请重新登录\"}");
            // return 结束方法，不会调用 filterChain.doFilter，请求被彻底拦截
            return;

        } finally {
            // 8. 【保留】清理 ThreadLocal 数据
            // 这里的 finally 块等价于你原本在 Interceptor 里的 afterCompletion 方法。
            // 当请求执行完 Controller 准备返回给前端时，一定会执行这里，防止内存泄漏。
            BaseContext.removeCurrent();
        }
    }
}

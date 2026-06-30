package com.fjq.login.common.exception;

import com.fjq.login.common.api.Result;
import com.fjq.login.common.api.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1. 处理自定义业务异常 (BusinessException)
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        // 业务异常通常是预期的，所以打个 INFO 或 WARN 级别的日志即可，记录一下请求路径
        log.warn("业务异常拦截 [URI: {}] - 异常原因: {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }


    /**
     * 2. 兜底处理：处理所有未知的系统异常 (Exception)
     * 场景：比如 NullPointerException, SQLSyntaxErrorException 等没被捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        // 系统内部未知异常，必须打 ERROR 级别的日志，并且输出完整堆栈信息方便排查！
        log.error("系统未知异常拦截 [URI: {}]", request.getRequestURI(), e);

        // 绝对不要把 e.getMessage() 直接返回给前端，因为可能包含数据库账号密码或 SQL 结构信息，极度危险！
        // 统一返回系统内部异常提示
        return Result.error(ResultCode.ERROR);
    }
}

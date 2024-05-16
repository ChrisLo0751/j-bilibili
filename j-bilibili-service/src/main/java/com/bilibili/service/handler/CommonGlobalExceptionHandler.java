package com.bilibili.service.handler;

import com.bilibili.domain.JsonResponse;
import com.bilibili.domain.exception.ConditionException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonGlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public JsonResponse<String> commonExceptionHandler(HttpServletRequest request, Exception e) {
        // 获取错误信息
        String errorMsg = e.getMessage();
        if (e instanceof ConditionException) {
            // 强制转换错误类型
            String errCode = ((ConditionException) e).getCode();
            return new JsonResponse<>(errCode, errorMsg);
        }else{
            // 返回通用的错误码 500
            return new JsonResponse<>("500", errorMsg);
        }
    }
}

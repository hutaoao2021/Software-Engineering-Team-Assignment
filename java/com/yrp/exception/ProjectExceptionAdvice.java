package com.yrp.exception;

import com.yrp.common.Code;
import com.yrp.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/9
 * @Description: com.yrp.controller
 * @version: 1.0
 */

// rest风格的异常处理器
@Slf4j
@RestControllerAdvice
public class ProjectExceptionAdvice {
    // 第一大类：系统异常
    // 拦截所有的SystemException类型的异常
    @ExceptionHandler(SystemException.class)
    public R dpSystemException(SystemException ex) {
        // 记录日志
        // 发送消息给运维
        // 发送邮件给开发人员
        // 把消息返回出去
        return new R(ex.getCode(), null, ex.getMessage());
    }
    // 第二大类：业务异常
    // 拦截所有的BusinessException类型的异常
    @ExceptionHandler(BusinessException.class)
    public R dpBusinessException(BusinessException ex) {
        log.info(ex.getMessage() +":" + ex.getCode());
        // 把消息返回出去
        return new R(ex.getCode(), null, ex.getMessage());
    }

    // 第三大类：其他异常
    // 拦截所有的Exception类型的异常
    @ExceptionHandler(Exception.class)
    public R dpException(Exception ex) {
        return new R(Code.SYSTEM_UNKNOWN_ERR, null, "系统繁忙，请稍后重试！");
    }
}

package com.yrp.exception;
/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/9
 * @Description: com.yrp.exception
 * @version: 1.0
 */

/**
 * 业务异常
 * 发送消息给用户，提醒用户规范操作
 */
public class BusinessException extends RuntimeException{
    /**
     * 异常编号
     */
    private Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}

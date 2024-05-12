package com.yrp.common;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/9
 * @Description: com.yrp.controller
 * @version: 1.0
 */

/**
 * 响应编码
 */
public class Code {
    // 正确
    public static final Integer OPERATION_ERR = 0;

    public static final Integer OPERATION_SUC = 1;

    // 系统异常
    public static final Integer SYSTEM_ERR = 50001;
    // 其他异常（系统未知异常）
    public static final Integer SYSTEM_UNKNOWN_ERR = 59999;
    // 业务层异常
    public static final Integer BUSINESS_ERR = 60001;

}

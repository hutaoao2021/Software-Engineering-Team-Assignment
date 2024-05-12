package com.yrp.utils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 阿里云oss工具类
 */
@Component
public class OssUtil implements InitializingBean {

    @Value("${spring.cloud.alicloud.access-key}")
    private String accessKeyID;
    @Value("${spring.cloud.alicloud.secret-key}")
    private String accesskeySecret;
    @Value("${spring.cloud.alicloud.oss.bucketname}")
    private String bucketName;
    @Value("${spring.cloud.alicloud.oss.endpoint}")
    private String endpoint;

    public static String END_POINT;
    public static String ACCESS_KEY_ID;
    public static String ACCESS_KEY_SECRET;
    public static String BUCKET_NAME;

    @Override
    public void afterPropertiesSet() throws Exception {
        END_POINT = endpoint;
        ACCESS_KEY_ID = accessKeyID;
        ACCESS_KEY_SECRET = accesskeySecret;
        BUCKET_NAME = bucketName;
    }
}
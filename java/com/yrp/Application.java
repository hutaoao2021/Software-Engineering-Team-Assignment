package com.yrp;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @Auther: 公众号/B站：是叶十三
 * @Date: 2022/7/14
 * @Description: com.yrp.reggie
 * @version: 1.0
 */
// lombok插件提供的日志
@Slf4j
@SpringBootApplication
// 配置过滤器后，需要扫描才能使用的到，暂时先关掉过滤器
@ServletComponentScan
// 开启事务管理
@EnableTransactionManagement
// 开启SpringBoot 注解方式的缓存功能
@EnableCaching
//开启注解开发AOP功能
@EnableAspectJAutoProxy
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
        log.info("项目启动成功");
    }
}

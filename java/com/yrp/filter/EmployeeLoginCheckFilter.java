package com.yrp.filter;

import com.yrp.common.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 只拦截后台，前台一律放行
 */
@WebFilter(filterName = "EmployeeLoginCheckFilter", urlPatterns = "/*")
@Slf4j
public class EmployeeLoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //1、获取本次请求的URI
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}", requestURI);
        //定义不需要处理的请求路径
        String[] urls = new String[]{
                // 后端 所有请求
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                // 前台
                "/front/**",
                "/user/**" ,
                "/order/**",
                "/shoppingCart/**",
                "/addressBook/**"
        };

        //2、判断本次请求是否需要处理
        boolean check = check(urls, requestURI);
        //3、如果不需要处理，则直接放行
        if (check) {
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

//        Long empId = (Long)request.getSession().getAttribute("employee");
//        Long userId = (Long)request.getSession().getAttribute("user");
//
        // 如果前台已登录，想问前台的项目，那你就别拦截了


        //4、判断员工登录状态，如果已登录，则直接放行
        System.out.println("当前员工的用户jsessionid：" + request.getSession().getAttribute("employee"));
        if (request.getSession().getAttribute("employee") != null) {
            log.info("员工已登录，用户id为：{}", request.getSession().getAttribute("employee"));
            // 获取当前登员工的id
            Long id = (Long) request.getSession().getAttribute("employee");
            // 将id存入threadlocal中（作用范围：同一线程内）
            BaseContext.setCurrentId(id);
            filterChain.doFilter(request, response);
            return;
        }

    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     *
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}

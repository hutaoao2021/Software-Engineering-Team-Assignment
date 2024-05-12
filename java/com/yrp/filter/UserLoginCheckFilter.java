package com.yrp.filter;

import com.alibaba.fastjson.JSON;
import com.yrp.common.BaseContext;
import com.yrp.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经完成登录
 * 只拦截前台，后台一律放行
 */
@WebFilter(filterName = "UserLoginCheckFilter", urlPatterns = "/*")
@Slf4j
public class UserLoginCheckFilter implements Filter {

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
                // 后端
                "/backend/**",
                "/common/**",
                "/category/**",
                "/category/**",
                "/dish/**",
                "/dish/**",
                "/employee/**",
                "/setmeal/**",
                // 前端
                "/front/**",
                "/user/login", // 移动端发送短信
                "/user/sendMsg" // 移动端登录
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

        //4.1 判断顾客登录状态，如果已登录，则直接放行
        System.out.println("当前登录的用户jsessionid：" + request.getSession().getAttribute("user"));
        if (request.getSession().getAttribute("user") != null) {
            String userId = (String) request.getSession().getAttribute("user");
            // 将id存入threadlocal中（作用范围：同一线程内）
            BaseContext.setCurrentId(Long.parseLong(userId));
            filterChain.doFilter(request, response);
            return;
        }
        log.info("用户未登录");
        //5、如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
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

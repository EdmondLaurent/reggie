package com.edmond.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.edmond.reggie.common.BaseContext;
import com.edmond.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// 定义拦截所有请求的过滤器
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class loginCheckFilter implements Filter {

    //Spring core 自带的工具用于检测请求是否与不要被处理的请求路径一致
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // 1、获取本次请求的URI
        String requestURI = request.getRequestURI();
        log.info("拦截到的用户请求：{}", requestURI);
        //   2、判断当前URI是否需要处理
        //  2-1、定义不需要被处理的URI列表
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**"
        };
        //  2-2、判断当前请求是否需要被处理
        boolean check = checkURI(urls, requestURI);
        //  3、若当前请求内容不需要处理（登录登出，静态页面）则直接放行当前请求
        if (check){
            filterChain.doFilter(request,response);
            return;     //  放行请求，退出当前方法
        }
        //   4、判断当前是否有用户登录的信息，有用户登录信息则直接放行
        if (request.getSession().getAttribute("employee") != null){
            //  将当前用户的登录信息封装在 ThreadLocal 对象中
            BaseContext.setCurrentId((Long) request.getSession().getAttribute("employee"));
            filterChain.doFilter(request,response);
            return;
        }
        //  5、无用户登录信息则返回没有用户登录的信息
        log.info("系统中没有用户登录的信息...");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    /**
     * 判断当前请求是否需要被处理，返回check旗帜
     *
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean checkURI(String[] urls, String requestURI) {
        for (String url : urls) {
            if (PATH_MATCHER.match(url, requestURI)) {
                //  请求路径与不需要被处理的路径一致，不需要处理
                return true;
            }
        }
        return false;
    }
}

package com.edmond.reggie.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MP 分页配置类
 * 配置MP 分页信息
 */
@Configuration
public class MybatisPlusPageConfig {
    //  MP 的分页是通过拦截器实现的
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        //  1. 初始化MP拦截器
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        //  2. 将分页拦截器添加到拦截器内部
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        //  3. 返回当前拦截器
        return mybatisPlusInterceptor;
    }
}

package com.starter.nova.web;

import com.starter.nova.web.handler.GlobalExceptionHandler;
import com.starter.nova.web.interceptor.ServletApiLogInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author tql
 * @date: 2025/11/13
 * @time: 21:04
 * @desc:
 */
@Configuration
@AutoConfiguration
public class NovaWebStaterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ServletApiLogInterceptor.class)
    public ServletApiLogInterceptor servletApiLogInterceptor() {
        return new ServletApiLogInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean(GlobalExceptionHandler.class)
    public GlobalExceptionHandler globalExceptionHandler(ApplicationContext context) {
        return new GlobalExceptionHandler(context);
    }
}

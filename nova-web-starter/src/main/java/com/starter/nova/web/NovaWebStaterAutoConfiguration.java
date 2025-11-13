package com.starter.nova.web;

import com.starter.nova.web.interceptor.ServletApiLogInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
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
@ComponentScan(basePackages = "com.starter.nova.web")
public class NovaWebStaterAutoConfiguration {

    @Bean
    public ServletApiLogInterceptor servletApiLogInterceptor() {
        return new ServletApiLogInterceptor();
    }
}

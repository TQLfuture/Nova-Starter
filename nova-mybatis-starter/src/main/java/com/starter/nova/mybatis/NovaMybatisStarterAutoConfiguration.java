package com.starter.nova.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.starter.nova.mybatis.extend.NovaMybatisPlusPlugin;
import com.starter.nova.mybatis.interceptor.AdvancedMybatisPlusInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author tql
 * @date: 2025/11/13
 * @time: 20:51
 * @desc:
 */
@Configuration
@AutoConfiguration
@ComponentScan("com.starter.nova.mybatis")
public class NovaMybatisStarterAutoConfiguration {

    @Bean
    public AdvancedMybatisPlusInterceptor mybatisPlusInterceptor(ApplicationContext context) {
        AdvancedMybatisPlusInterceptor interceptor = new AdvancedMybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new AdvancedMybatisPlusInterceptor.PrePaginationInnerInterceptor());
        // Missing: record SQL before/after normal (non-pagination) queries/updates
        interceptor.addInnerInterceptor(new AdvancedMybatisPlusInterceptor.SqlExecutionInnerInterceptor());
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 自动发现用户实现的所有 NovaMybatisPlusInterceptor
        Map<String, NovaMybatisPlusPlugin> beans =
                context.getBeansOfType(NovaMybatisPlusPlugin.class);
        List<InnerInterceptor> list = beans
                .values()
                .stream()
                .sorted(Comparator.comparingInt(NovaMybatisPlusPlugin::getOrder))
                .map(NovaMybatisPlusPlugin::getInterceptor)
                .toList();
        list.forEach(interceptor::addInnerInterceptor);
        return interceptor;
    }
}

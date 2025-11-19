package com.stater.nova.storage;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * `
 *
 * @author tql
 * @date: 2025/11/19
 * @time: 17:11
 * @desc:
 */
@Configuration
@AutoConfiguration
@ConditionalOnProperty(prefix = "aliyun-oss", name = "enabled", havingValue = "true")
@ComponentScan("com.starter.nova.storage")
public class StoreStarterAutoConfiguration {
}

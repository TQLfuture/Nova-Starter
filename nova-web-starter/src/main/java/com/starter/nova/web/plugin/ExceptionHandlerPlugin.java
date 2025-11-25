package com.starter.nova.web.plugin;

import org.springframework.http.ResponseEntity;

/**
 * @author tql
 * @date: 2025/11/25
 * @time: 20:25
 * @desc:
 */
public interface ExceptionHandlerPlugin {

    default void handle(Throwable throwable) {
    }
}

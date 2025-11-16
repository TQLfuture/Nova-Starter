package com.starter.nova.common.code;

import lombok.Getter;

/**
 * @author tql
 * @date: 2025/11/16
 * @time: 14:23
 * @desc:
 */
public abstract class AbstractStatusCode {

    @Getter
    protected int code;

    @Getter
    protected String message;

    public AbstractStatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

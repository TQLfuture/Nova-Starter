package com.starter.nova.common.code;

/**
 * @author tql
 * @date: 2025/11/16
 * @time: 22:03
 * @desc:
 */
public class Unauthorized extends AbstractStatusCode {
    public static final Unauthorized UNAUTHORIZED = new Unauthorized(null);

    public Unauthorized(String message) {
        super(4003, message == null ? "权限不足" : message);
    }
}

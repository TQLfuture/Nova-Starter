package com.starter.nova.common.code;

/**
 * @author tql
 * @date: 2025/11/16
 * @time: 22:06
 * @desc:
 */
public class InternalServerError extends AbstractStatusCode {
    public static final InternalServerError INTERNAL_SERVER_ERROR = new InternalServerError(null);
    public InternalServerError(String message) {
        super(5000, message != null ?"服务器错误":message);
    }
}

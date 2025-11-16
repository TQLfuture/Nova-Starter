package com.starter.nova.common.code;

/**
 * @author tql
 * @date: 2025/11/16
 * @time: 14:33
 * @desc:
 */
public class BadRequest extends AbstractStatusCode {
    public static final BadRequest BAD_REQUEST = new BadRequest(null);

    public BadRequest(String msg) {
        super(400, msg == null ? "请求失败" : msg);
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
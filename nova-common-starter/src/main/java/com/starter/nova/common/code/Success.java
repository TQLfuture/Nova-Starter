package com.starter.nova.common.code;

/**
 * @author tql
 * @date: 2025/11/16
 * @time: 14:34
 * @desc:
 */
public class Success extends AbstractStatusCode {

    public static final Success SUCCESS = new Success(null);

    public Success(String msg) {
        super(200, msg == null ? "操作成功" : msg);
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


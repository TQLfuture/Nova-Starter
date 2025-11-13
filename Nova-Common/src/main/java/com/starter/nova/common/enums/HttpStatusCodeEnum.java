package com.starter.nova.common.enums;


import lombok.Getter;

/**
 * @author tql
 * @date: 2025/11/13
 * @time: 16:25
 * @desc:
 */
@Getter
public enum HttpStatusCodeEnum {

    /**
     * 请求成功
     */
    OK(2000, "请求成功"),
    /**
     * 请求失败
     */
    BAD_REQUEST(4000, "请求失败"),
    /**
     * 未登录
     */
    UNAUTHENTICATED(4001, "未登录"),
    /**
     * 权限不足
     */
    UNAUTHORIZED(4003, "权限不足"),
    /**
     * 服务器错误(内部服务错误)
     */
    INTERNAL_SERVER_ERROR(5000, "服务器错误"),
    /**
     * 服务调用失败
     */
    RPC_INVOKE_ERROR(6000, "服务调用失败");

    private final int code;

    private final String desc;

    HttpStatusCodeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

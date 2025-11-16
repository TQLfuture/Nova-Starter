package com.starter.nova.common.model;

import com.starter.nova.common.code.*;
import lombok.Data;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.MDC;

import java.io.Serializable;

/**
 * 响应结果
 *
 * @author tql
 * @date: 2025/11/13
 * @time: 16:11
 * @desc:
 */
@Data
public class BaseResult<T> implements Serializable {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 响应码，2000: 请求成功，4000：请求失败，4001：未登录，4003：权限不足，5000：服务器错误"
     */
    private int code;

    /**
     * 描述
     */
    private String desc;

    /**
     * 数据
     */
    private T data;

    /**
     * traceId
     */
    private String traceId;

    private static String resolveTraceId() {
        String tid = MDC.get("traceId");
        if (tid == null || tid.isEmpty() || "N/A".equalsIgnoreCase(tid)) {
            String sky = TraceContext.traceId();
            if (!sky.isEmpty() && !"N/A".equalsIgnoreCase(sky)) {
                tid = sky;
            }
        }
        return tid;
    }

    public BaseResult() {
        this.traceId = resolveTraceId();
    }

    public BaseResult(int code, String desc, T data) {
        this.code = code;
        this.success = code == Success.SUCCESS.getCode();
        this.desc = desc;
        this.data = data;
        this.traceId = resolveTraceId();
    }

    public static <T> BaseResult<T> success() {
        return new BaseResult<>(Success.SUCCESS.getCode(), Success.SUCCESS.getMessage(), null);
    }

    public static <T> BaseResult<T> success(T data) {
        return new BaseResult<>(Success.SUCCESS.getCode(), Success.SUCCESS.getMessage(), data);
    }

    public static <T> BaseResult<T> success(T data, String desc) {
        return new BaseResult<>(Success.SUCCESS.getCode(), desc, data);
    }

    public static <T> BaseResult<T> error() {
        return new BaseResult<>(BadRequest.BAD_REQUEST.getCode(), BadRequest.BAD_REQUEST.getMessage(), null);
    }

    public static <T> BaseResult<T> error(String desc) {
        return new BaseResult<>(BadRequest.BAD_REQUEST.getCode(), desc, null);
    }


    public static <T> BaseResult<T> unauthenticated() {
        return new BaseResult<>(Unauthenticated.UNAUTHENTICATED.getCode(), Unauthenticated.UNAUTHENTICATED.getMessage(), null);
    }

    public static <T> BaseResult<T> unauthenticated(String desc) {
        return new BaseResult<>(Unauthenticated.UNAUTHENTICATED.getCode(), desc, null);
    }

    public static <T> BaseResult<T> unauthorized() {
        return new BaseResult<>(Unauthorized.UNAUTHORIZED.getCode(), Unauthorized.UNAUTHORIZED.getMessage(), null);
    }

    public static <T> BaseResult<T> unauthorized(String desc) {
        return new BaseResult<>(Unauthorized.UNAUTHORIZED.getCode(), desc, null);
    }

    public static <T> BaseResult<T> serverError() {
        return new BaseResult<>(InternalServerError.INTERNAL_SERVER_ERROR.getCode(), InternalServerError.INTERNAL_SERVER_ERROR.getMessage(), null);
    }

    public static <T> BaseResult<T> serverError(String desc) {
        return new BaseResult<>(InternalServerError.INTERNAL_SERVER_ERROR.getCode(), desc, null);
    }
}

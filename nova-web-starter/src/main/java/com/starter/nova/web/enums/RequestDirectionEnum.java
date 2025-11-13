package com.starter.nova.web.enums;

import lombok.Getter;

/**
 * @author tql
 */
@Getter
public enum RequestDirectionEnum {
    /**
     * 请求方向
     */
    IN("IN", "服务被外部调用"),

    OUT("OUT", "服务调用外部");

    private final String code;

    private final String desc;

    RequestDirectionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

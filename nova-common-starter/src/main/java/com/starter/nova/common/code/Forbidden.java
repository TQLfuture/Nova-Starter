package com.starter.nova.common.code;

/**
 * @author tql
 * @date: 2025/11/16
 * @time: 14:37
 * @desc:
 */
public class Forbidden extends AbstractStatusCode {

    public static final Forbidden FORBIDDEN = new Forbidden();

    public Forbidden() {
        super(403, "禁止访问");
    }
}

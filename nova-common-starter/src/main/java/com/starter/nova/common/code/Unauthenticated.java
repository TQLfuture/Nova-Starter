package com.starter.nova.common.code;

/**
 * @author tql
 * @date: 2025/11/16
 * @time: 22:02
 * @desc:
 */
public class Unauthenticated extends AbstractStatusCode {

    public static final Unauthenticated UNAUTHENTICATED = new Unauthenticated();

    public Unauthenticated() {
        super(4001, "未登录");
    }
}

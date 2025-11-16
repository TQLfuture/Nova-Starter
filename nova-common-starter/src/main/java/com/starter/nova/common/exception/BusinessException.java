package com.starter.nova.common.exception;

import com.starter.nova.common.code.AbstractStatusCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author tql
 * @date: 2025/11/13
 * @time: 16:21
 * @desc:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessException extends RuntimeException {

    private String message;
    private Object[] args;
    private Integer code;

    public BusinessException() {
        super();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message, Object... args) {
        super(message);
        this.message = message;
        this.args = args;
    }

    public BusinessException(Integer code, String message, Object... args) {
        super(message);
        this.code = code;
        this.message = message;
        this.args = args;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public BusinessException(AbstractStatusCode status, Throwable cause) {
        this(status.getCode(), status.getMessage(), cause);
    }

    public BusinessException(AbstractStatusCode status) {
        this(status.getCode(), status.getMessage(), status.getMessage());
    }

}

package com.example.library7.common;

/**
 * 业务异常，code 直接作为 HTTP 状态码返回。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

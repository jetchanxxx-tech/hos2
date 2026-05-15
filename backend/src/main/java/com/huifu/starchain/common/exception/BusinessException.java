package com.huifu.starchain.common.exception;


public class BusinessException extends RuntimeException {

    private final int code;

    public int getCode() { return code; }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(BizError error) {
        super(error.getMessage());
        this.code = error.getCode();
    }

    public BusinessException(BizError error, String detail) {
        super(error.getMessage() + ": " + detail);
        this.code = error.getCode();
    }
}

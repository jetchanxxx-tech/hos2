package com.huifu.starchain.common.exception;

public class UnauthorizedException extends BusinessException {
    public UnauthorizedException(String message) {
        super(40100, message);
    }
}

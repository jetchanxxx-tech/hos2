package com.huifu.starchain.common.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, Object id) {
        super(40400, resource + " 不存在: " + id);
    }
}

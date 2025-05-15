package com.IKov.TwittService.entity.exceptions;

public class RedisException extends RuntimeException {
    public RedisException(String message) {
        super(message);
    }
}

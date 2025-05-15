package com.IKov.TwittService.entity.exceptions;

public class CassandraException extends RuntimeException {
    public CassandraException(String message) {
        super(message);
    }
}

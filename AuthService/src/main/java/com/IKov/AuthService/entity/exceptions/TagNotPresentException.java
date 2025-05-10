package com.IKov.AuthService.entity.exceptions;

public class TagNotPresentException extends RuntimeException {
    public TagNotPresentException(String message) {
        super(message);
    }
}

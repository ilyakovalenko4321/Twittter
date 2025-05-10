package com.IKov.User_Service.entity.exception;

public class PasswordConfirmationMismatchException extends RuntimeException {
    public PasswordConfirmationMismatchException(String message) {
        super(message);
    }
}

package com.users_service.exception;

public class ResourceNotModifiedException extends RuntimeException {
    public ResourceNotModifiedException(String message) {
        super(message);
    }
}
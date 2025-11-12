package com.notification_service.exception;

public class NotificationSendException extends RuntimeException {
    public NotificationSendException(String message, Throwable cause) { super(message, cause); }
    public NotificationSendException(String message) { super(message); }
}


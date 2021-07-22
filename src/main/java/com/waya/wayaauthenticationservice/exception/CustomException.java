package com.waya.wayaauthenticationservice.exception;

import org.springframework.http.HttpStatus;

import java.util.Date;

public class CustomException extends RuntimeException {
    /**
     * For serialization: if any changes is made to this class, update the
     * serialversionID
     */
    private static final long serialVersionUID = 1L;
    private Date timestamp;
    private final String message;
    private final HttpStatus status;
    private Throwable cause;

    public CustomException(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
        this.timestamp = new Date();
    }

    public CustomException(String message, Throwable cause, HttpStatus status) {
        this.message = message;
        this.cause = cause;
        this.status = status;
        this.timestamp = new Date();
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public synchronized Throwable getCause() {
        return cause;
    }
}

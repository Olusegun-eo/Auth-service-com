package com.waya.wayaauthenticationservice.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private ZonedDateTime timestamp;
    private  String message;
    private  boolean status;
    private HttpStatus httpStatus;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(T data, String message, boolean status) {
        timestamp = ZonedDateTime.now();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(T data, String message, boolean status, HttpStatus httpStatus) {
        timestamp = ZonedDateTime.now();
        this.status = status;
        this.message = message;
        this.data = data;
        this.httpStatus = httpStatus;
    }


    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public boolean getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}


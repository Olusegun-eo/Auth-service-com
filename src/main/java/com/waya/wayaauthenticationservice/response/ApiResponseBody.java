package com.waya.wayaauthenticationservice.response;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.ToString;

@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponseBody<T> {
    private Date timestamp = new Date();
    private String message;
    private boolean status;
    private T data;

    public ApiResponseBody() {
    }

    public ApiResponseBody(T data, String message, boolean status) {
        timestamp = new Date();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ApiResponseBody(String message, boolean status) {

        timestamp = new Date();
        this.status = status;
        this.message = message;
        this.data = null;
    }

    public Date getTimestamp() {
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

}


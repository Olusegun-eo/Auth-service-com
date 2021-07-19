package com.waya.wayaauthenticationservice.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponsePojo {

    private boolean status;
    private String message;
    private int code;

    public ResponsePojo(boolean status, String message, int code) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

    public ResponsePojo(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public static ResponsePojo response(boolean status, String message) {
        return new ResponsePojo(status, message);
    }

    public static ResponsePojo response(boolean status, String message, int code) {
        return new ResponsePojo(status, message,code);
    }
}

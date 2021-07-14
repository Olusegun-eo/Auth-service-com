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

    public ResponsePojo(boolean error, String message) {
        this.status = error;
        this.message = message;
    }

    public static ResponsePojo response(boolean error, String message) {
        return new ResponsePojo(error, message);
    }

    public static ResponsePojo response(boolean status, String message, int code) {
        return new ResponsePojo(status, message,code);
    }
}

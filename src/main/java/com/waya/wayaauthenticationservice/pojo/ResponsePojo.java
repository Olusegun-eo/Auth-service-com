package com.waya.wayaauthenticationservice.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponsePojo {
    private boolean error;
    private String message;

    public ResponsePojo(boolean error, String message) {
        this.error = error;
        this.message = message;
    }

    public static ResponsePojo response(boolean error, String message) {
        return new ResponsePojo(error, message);
    }
}

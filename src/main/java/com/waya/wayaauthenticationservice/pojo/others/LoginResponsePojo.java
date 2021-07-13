package com.waya.wayaauthenticationservice.pojo.others;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponsePojo {
    private boolean status;
    private String message;
    private int code;
    private Object data;
}

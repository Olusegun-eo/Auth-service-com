package com.waya.wayaauthenticationservice.pojo.password;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PinPojo2 {
    private int oldPin;
    private int newPin;
    private String email;
}

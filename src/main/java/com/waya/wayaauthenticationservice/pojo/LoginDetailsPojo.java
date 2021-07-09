package com.waya.wayaauthenticationservice.pojo;

import lombok.Data;

@Data
public class LoginDetailsPojo {
    private boolean admin = false;
    private String emailOrPhoneNumber;
    private String password;
}

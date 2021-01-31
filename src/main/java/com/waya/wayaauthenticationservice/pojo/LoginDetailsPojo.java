package com.waya.wayaauthenticationservice.pojo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class LoginDetailsPojo {
    private  boolean admin = false;
    private String email;
    private String password;
}

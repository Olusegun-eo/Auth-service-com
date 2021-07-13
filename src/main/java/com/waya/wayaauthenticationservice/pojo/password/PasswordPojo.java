package com.waya.wayaauthenticationservice.pojo.password;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordPojo {

    private String newPassword;
    private String oldPassword;
    private String email;


    public PasswordPojo(){}

}

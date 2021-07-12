package com.waya.wayaauthenticationservice.pojo.password;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordPojo {

//    @JsonIgnore
    private int otp;
    private String oldPassword;
    private String newPassword;
    private String email;
}

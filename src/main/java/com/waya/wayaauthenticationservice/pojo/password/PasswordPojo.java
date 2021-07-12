package com.waya.wayaauthenticationservice.pojo.password;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordPojo {

    private int otp;
    private String newPassword;
    private String email;
    @ApiModelProperty(hidden=true)
    private Long userId;

    public PasswordPojo(){}

    public PasswordPojo(int otp, String newPassword, String email, Long userId) {
        this.otp = otp;
        this.newPassword = newPassword;
        this.email = email;
        this.userId = userId;
    }
}

package com.waya.wayaauthenticationservice.pojo.password;

import javax.validation.constraints.NotBlank;

import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.util.Type;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordPojo {

    @NotBlank(message = "otp cannot be blank")
    private String otp;

    @NotBlank(message = "NewPassword cannot be blank")
    private String newPassword;

    @NotBlank(message = "Field cannot be blank or Null")
    @CustomValidator(message = "phoneOrEmail field has to be either a Phone or an Email", type = Type.EMAIL_OR_PHONE)
    private String phoneOrEmail;

    @NotBlank(message = "OldPassword cannot be blank")
    private String oldPassword;

    public PasswordPojo(){}

}

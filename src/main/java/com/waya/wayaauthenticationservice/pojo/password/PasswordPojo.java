package com.waya.wayaauthenticationservice.pojo.password;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class PasswordPojo {

    @NotBlank(message = "NewPassword cannot be blank")
    private String newPassword;

    @Email(message = "Must be a valid Email")
    private String email;

    @NotBlank(message = "OldPassword cannot be blank")
    private String oldPassword;

    public PasswordPojo(){}

}

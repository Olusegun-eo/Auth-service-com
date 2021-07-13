package com.waya.wayaauthenticationservice.pojo.password;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ResetPasswordPojo {

//    @JsonIgnore
    @NotNull
    private int otp;

    @NotBlank(message = "NewPassword cannot be blank")
    private String newPassword;

    @Email(message = "Must be a valid Email")
    private String email;
}

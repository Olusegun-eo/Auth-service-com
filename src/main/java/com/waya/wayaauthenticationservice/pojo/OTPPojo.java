package com.waya.wayaauthenticationservice.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class OTPPojo {

    @NotNull(message = "PhoneNumber or Email  Cannot be Null")
    @NotBlank(message = "PhoneNumber or Email Cannot be blank")
    private String phoneOrEmail;

    @NotNull(message = "OTP Cannot be Null")
    @NotBlank(message = "OTP Cannot be blank")
    private String otp;
}

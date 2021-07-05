package com.waya.wayaauthenticationservice.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class OTPPojo {

    @NotNull(message = "Phone Cannot be Null")
    @NotBlank(message = "phoneNumber Cannot be blank")
    private String phone;

    @NotNull(message = "OTP Cannot be Null")
    private int otp;
}

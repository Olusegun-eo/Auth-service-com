package com.waya.wayaauthenticationservice.pojo.notification;

import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.util.Type;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
public class OTPPojo {

    @NotBlank(message = "PhoneNumber or Email Cannot be blank")
    @CustomValidator(message = "OTP Passed must be either Email or PhoneNumber",
            type = Type.EMAIL_OR_PHONE)
    private String phoneOrEmail;

    @NotBlank(message = "OTP Cannot be blank")
    @CustomValidator(message = "OTP Passed must be numeric", type = Type.NUMERIC_STRING)
    private String otp;
}

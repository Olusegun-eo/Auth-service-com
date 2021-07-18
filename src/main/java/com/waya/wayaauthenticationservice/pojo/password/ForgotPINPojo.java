package com.waya.wayaauthenticationservice.pojo.password;

import com.waya.wayaauthenticationservice.enums.Type;
import com.waya.wayaauthenticationservice.util.CustomValidator;


public class ForgotPINPojo extends NewPinPojo{

    @CustomValidator(message = "Input has to be Numeric", type = Type.NUMERIC_STRING)
    private String otp;

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp.replaceAll("\\s+", "").trim();
    }
}

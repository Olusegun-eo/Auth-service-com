package com.waya.wayaauthenticationservice.pojo.password;

import javax.validation.constraints.*;

import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.enums.Type;


public class NewPinPojo {

    @CustomValidator(message = "Input has to be Numeric", type = Type.NUMERIC_STRING)
    private String otp;

    @NotBlank(message = "newPin cannot be blank or null")
    @Size(message = "Should be of size {}", min = 4, max = 4)
    private String pin;

    @NotBlank(message = "Field cannot be blank or Null")
    @CustomValidator(message = "phoneOrEmail field has to be either a Phone or an Email", type = Type.EMAIL_OR_PHONE)
    private String phoneOrEmail;

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin.replaceAll("\\s+", "").trim();
    }

    public String getPhoneOrEmail() {
        return phoneOrEmail;
    }

    public void setPhoneOrEmail(String phoneOrEmail) {
        this.phoneOrEmail = phoneOrEmail.replaceAll("\\s+", "").trim();
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp.replaceAll("\\s+", "").trim();
    }
}

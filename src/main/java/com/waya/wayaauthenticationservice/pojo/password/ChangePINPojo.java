package com.waya.wayaauthenticationservice.pojo.password;

import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.util.Type;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


public class ChangePINPojo {

    @NotBlank(message = "otp cannot be blank")
    private String otp;

    @NotBlank(message = "oldPin cannot be blank or null")
    @Size(message = "Should be of size {}", min = 4, max = 4)
    private String oldPin;

    @NotBlank(message = "newPin cannot be blank or null")
    @Size(message = "Should be of size {}", min = 4, max = 4)
    private String newPin;

    @NotBlank(message = "Field cannot be blank or Null")
    @CustomValidator(message = "phoneOrEmail field has to be either a Phone or an Email", type = Type.EMAIL_OR_PHONE)
    private String phoneOrEmail;

    public String getOldPin() {
        return oldPin;
    }

    public void setOldPin(String oldPin) {
        this.oldPin = oldPin.replaceAll("\\s+", "").trim();
    }

    public String getNewPin() {
        return newPin;
    }

    public void setNewPin(String newPin) {
        this.newPin = newPin.replaceAll("\\s+", "").trim();
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
        this.otp = otp;
    }
}

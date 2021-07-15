package com.waya.wayaauthenticationservice.pojo.password;

import com.waya.wayaauthenticationservice.util.CustomValidator;
import com.waya.wayaauthenticationservice.util.Type;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


public class ResetPasswordPojo {

    @NotNull
    private int otp;

    @NotBlank(message = "NewPassword cannot be blank")
    private String newPassword;

    @NotBlank(message = "Field cannot be blank or Null")
    @CustomValidator(message = "phoneOrEmail field has to be either a Phone or an Email", type = Type.EMAIL_OR_PHONE)
    private String phoneOrEmail;

    public int getOtp() {
        return otp;
    }

    public void setOtp(int otp) {
        this.otp = otp;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getPhoneOrEmail() {
        return phoneOrEmail;
    }

    public void setPhoneOrEmail(String phoneOrEmail) {
        this.phoneOrEmail = phoneOrEmail.replaceAll("\\s+", "").trim();
    }
}

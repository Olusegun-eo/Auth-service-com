package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.pojo.password.PasswordPojo;
import com.waya.wayaauthenticationservice.pojo.password.ResetPasswordPojo;
import com.waya.wayaauthenticationservice.response.ResponsePojo;

public interface PasswordService {

    ResponsePojo setForgotPassword(PasswordPojo passwordPojo);

    ResponsePojo sendOtp(String email);

    ResponsePojo resetPassword(ResetPasswordPojo resetPasswordPojo);

    ResponsePojo forgotPin(String email);

    ResponsePojo verifyEmail(String email);
}

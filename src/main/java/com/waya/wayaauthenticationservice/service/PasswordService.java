package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.pojo.password.PasswordPojo;
import com.waya.wayaauthenticationservice.pojo.password.ResetPasswordPojo;
import com.waya.wayaauthenticationservice.response.ResponsePojo;
import org.springframework.http.ResponseEntity;

public interface PasswordService {

    ResponseEntity<?> setForgotPassword(PasswordPojo passwordPojo);

    ResponseEntity<?> sendPasswordResetOTP(String email, String baseUrl);

    ResponseEntity<?> resetPassword(ResetPasswordPojo resetPasswordPojo);

    ResponseEntity<?> forgotPin(String email);

    ResponseEntity<?> verifyEmail(String email);
}

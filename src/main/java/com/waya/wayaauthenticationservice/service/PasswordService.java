package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.pojo.password.PasswordPojo;
import com.waya.wayaauthenticationservice.pojo.password.PinPojo;
import com.waya.wayaauthenticationservice.pojo.password.PinPojo2;
import com.waya.wayaauthenticationservice.pojo.password.ResetPasswordPojo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

public interface PasswordService {

    @PreAuthorize(value = "@userSecurity.useHierarchy(#passwordPojo.email, authentication) or hasRole('ADMIN')")
    ResponseEntity<?> setForgotPassword(PasswordPojo passwordPojo);

    @PreAuthorize(value = "@userSecurity.useHierarchy(#email, authentication) or hasRole('ADMIN')")
    ResponseEntity<?> sendPasswordResetOTP(String email, String baseUrl);

    @PreAuthorize(value = "@userSecurity.useHierarchy(#passwordPojo.email, authentication) or hasRole('ADMIN')")
    ResponseEntity<?> resetPassword(ResetPasswordPojo passwordPojo);

    @PreAuthorize(value = "@userSecurity.useHierarchy(#email, authentication) or hasRole('ADMIN')")
    ResponseEntity<?> forgotPin(String email);

    ResponseEntity<?> createPin(PinPojo pinPojo);

    ResponseEntity<?> changePin(PinPojo2 pinPojo);

    ResponseEntity<?> forgotPin(PinPojo pinPojo);

    ResponseEntity<?> validatePin(Long userId, int pin);

    ResponseEntity<?> validatePinFromUser(int pin);


}

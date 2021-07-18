package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.pojo.password.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

public interface PasswordService {

    ResponseEntity<?> changePassword(PasswordPojo passwordPojo);

    ResponseEntity<?> sendPasswordResetOTPByEmail(String email, String baseUrl);

    ResponseEntity<?> sendResetOTPByPhoneNumber(String phoneNumber);

    ResponseEntity<?> resetPassword(ResetPasswordPojo passwordPojo);

    @PreAuthorize(value = "@userSecurity.useHierarchy(#email, authentication) or hasRole('APP_ADMIN')")
    ResponseEntity<?> sendPinResetOTPByEmail(String email, String redirectUrl);

    ResponseEntity<?> createPin(NewPinPojo pinPojo);

    ResponseEntity<?> changePin(ChangePINPojo pinPojo);

    ResponseEntity<?> changeForgotPIN(ForgotPINPojo pinPojo);

    @PreAuthorize(value = "hasRole('APP_ADMIN')")
    ResponseEntity<?> validatePin(Long userId, int pin);

    ResponseEntity<?> validatePinFromUser(int pin);


}

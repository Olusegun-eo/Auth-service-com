package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.pojo.password.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

public interface PasswordService {

    ResponseEntity<?> changePassword(PasswordPojo passwordPojo);

    ResponseEntity<?> sendPasswordResetOTPByEmail(String email, String baseUrl);

    ResponseEntity<?> resetPassword(ResetPasswordPojo passwordPojo);

    ResponseEntity<?> sendPasswordResetOTPByPhoneNumber(String phoneNumber);

    ResponseEntity<?> sendPINResetOTPByPhoneNumber(String phoneNumber);

    @PreAuthorize(value = "@userSecurity.useHierarchy(#email, authentication) or hasRole('APP_ADMIN')")
    ResponseEntity<?> sendPinResetOTPByEmail(String email, String redirectUrl);

    ResponseEntity<?> sendPINChangeOTPByPhoneNumber(String phoneNumber);

    ResponseEntity<?> sendPinChangeOTPByEmail(String email, String redirectUrl);

    ResponseEntity<?> createPin(NewPinPojo pinPojo);

    ResponseEntity<?> changePin(ChangePINPojo pinPojo);

    ResponseEntity<?> changeForgotPIN(NewPinPojo pinPojo);

    @PreAuthorize(value = "hasRole('APP_ADMIN')")
    ResponseEntity<?> validatePin(Long userId, int pin);

    ResponseEntity<?> validatePinFromUser(int pin);

    ResponseEntity<?> sendPinCreationOTPPhone(String phoneNumber);

    ResponseEntity<?> sendPinCreationOTPEmail(String email, String redirectUrl);
}

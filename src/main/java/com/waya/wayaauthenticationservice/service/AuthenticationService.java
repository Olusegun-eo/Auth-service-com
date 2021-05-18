package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.pojo.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.validation.constraints.Email;



public interface AuthenticationService {

    ResponseEntity createUser(UserPojo userPojo);
    ResponseEntity createCorporateUser(CorporateUserPojo corporateUserPojo, String token);
    ResponseEntity createPin(PinPojo pinPojo);
    ResponseEntity verifyOTP(OTPPojo otpPojo);
    ResponseEntity verifyEmail(EmailPojo emailPojo);
    ResponseEntity changePassword(PasswordPojo passwordPojo);
    ResponseEntity forgotPassword(PasswordPojo2 passwordPojo);
    ResponseEntity changePin(PinPojo2 pinPojo);
    ResponseEntity forgotPin(PinPojo pinPojo);
    ResponseEntity resendOTP(String phoneNumber, String email);
    ResponseEntity resendVerificationMail(String email, String userName);
    ResponseEntity validateUser();
    ResponseEntity validatePin(Long userId, int pin);
    ResponseEntity validatePinFromUser(int pin);
    ResponseEntity userByPhone(String phone);
    ResponseEntity createVirtualAccount(VirtualAccountPojo virtualAccountPojo);
    ResponseEntity createWalletAccount(WalletPojo walletPojo);
    ResponseEntity createWayagramAccount(WayagramPojo wayagramPojo);
    ResponseEntity createProfileAccount(ProfilePojo profilePojo);
    ResponseEntity createCorporateProfileAccount(ProfilePojo2 profilePojo2);




}

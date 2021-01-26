package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.pojo.EmailPojo;
import com.waya.wayaauthenticationservice.pojo.OTPPojo;
import com.waya.wayaauthenticationservice.pojo.PinPojo;
import com.waya.wayaauthenticationservice.pojo.UserPojo;
import org.springframework.http.ResponseEntity;

import javax.validation.constraints.Email;

public interface AuthenticationService {

    ResponseEntity createUser(UserPojo userPojo);
    ResponseEntity createPin(PinPojo pinPojo);
    ResponseEntity verifyOTP(OTPPojo otpPojo);
    ResponseEntity verifyEmail(EmailPojo emailPojo);



}

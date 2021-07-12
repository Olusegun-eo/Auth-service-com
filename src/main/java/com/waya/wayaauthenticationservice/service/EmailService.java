package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.response.EmailVerificationResponse;

public interface EmailService {

    boolean sendEmailToken(String email, String fullName, String message);

    EmailVerificationResponse verifyEmailToken(String email, Integer otp);

    OTPBase generateEmailToken(String email);
}
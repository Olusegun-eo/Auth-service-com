package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.response.EmailVerificationResponse;

public interface EmailService {

    boolean sendEmailToken(String email, String fullName);

    EmailVerificationResponse verifyEmailToken(String email, Integer otp);
}
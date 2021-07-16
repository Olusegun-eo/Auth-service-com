package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.response.EmailVerificationResponse;
import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;

import javax.validation.Valid;
import javax.validation.constraints.Email;

public interface EmailService {

    boolean sendAcctVerificationEmailToken(String baseUrl, @Valid @Email String email);

    OTPVerificationResponse verifyEmailToken(@Valid @Email String email, Integer otp);

    OTPBase generateEmailToken(@Valid @Email String email);
}
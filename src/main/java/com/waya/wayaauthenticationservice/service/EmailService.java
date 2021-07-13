package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.response.EmailVerificationResponse;

import javax.validation.Valid;
import javax.validation.constraints.Email;

public interface EmailService {

    boolean sendAcctVerificationEmailToken(String baseUrl, @Valid @Email String email);

    EmailVerificationResponse verifyEmailToken( @Valid @Email String email, Integer otp);

    OTPBase generateEmailToken(@Valid @Email String email);
}
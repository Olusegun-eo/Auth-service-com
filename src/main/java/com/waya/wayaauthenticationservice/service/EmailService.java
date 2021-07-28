package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.enums.OTPRequestType;
import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;

import javax.validation.Valid;
import javax.validation.constraints.Email;

public interface EmailService {

    boolean sendVerificationEmailToken(String baseUrl, Profile profile, OTPRequestType otpRequestType);

    boolean sendAccountVerificationEmailToken(OTPBase otp, String baseUrl, Profile profile);

    OTPVerificationResponse verifyEmailToken(@Valid @Email String email, Integer otp, OTPRequestType otpRequestType);

    OTPBase generateEmailToken(@Valid @Email String email, OTPRequestType otpRequestType);

    void invalidateOldToken(String email, OTPRequestType otpRequestType);
}
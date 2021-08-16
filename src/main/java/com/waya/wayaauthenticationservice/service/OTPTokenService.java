package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.entity.Users;
import com.waya.wayaauthenticationservice.enums.OTPRequestType;
import com.waya.wayaauthenticationservice.pojo.mail.AbstractEmailContext;
import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;

import javax.validation.Valid;
import javax.validation.constraints.Email;

public interface OTPTokenService {

    OTPBase generateSMSOTP(String phoneNumber, OTPRequestType otpRequestType);

    OTPBase generateOTP(String phoneNumber, String email, OTPRequestType otpRequestType);

    boolean sendSMSOTP(String phoneNumber, String fullName, OTPRequestType otpRequestType);

    boolean sendSMSOTP(String name, OTPBase otp);

    OTPVerificationResponse verifySMSOTP(String phoneNumber, Integer otp, OTPRequestType otpRequestType);

    boolean sendVerificationEmailToken(String baseUrl, Users profile, OTPRequestType otpRequestType);

    boolean sendEmailToken(AbstractEmailContext emailContext);

    OTPVerificationResponse verifyJointOTP(String emailOrPhoneNumber, String otp, OTPRequestType otpRequestType);

    OTPVerificationResponse verifyEmailToken(@Valid @Email String email, Integer otp, OTPRequestType otpRequestType);

    OTPBase generateEmailToken(@Valid @Email String email, OTPRequestType otpRequestType);

    void invalidateOldTokenViaEmail(String email, OTPRequestType otpRequestType);

    void invalidateOldTokenViaPhoneNumber(String phoneNumber, OTPRequestType otpRequestType);

    void sendAccountVerificationToken(Users profile, OTPRequestType accountVerification, String baseUrl);
}

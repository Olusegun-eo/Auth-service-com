package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.enums.OTPRequestType;
import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;

public interface SMSTokenService {

    OTPBase generateSMSOTP(String phoneNumber, OTPRequestType otpRequestType);

    boolean sendSMSOTP(String phoneNumber, String fullName, OTPRequestType otpRequestType);

    OTPVerificationResponse verifySMSOTP(String phoneNumber, Integer otp, OTPRequestType otpRequestType);

    void invalidateOldToken(String phoneNumber, OTPRequestType otpRequestType);
}

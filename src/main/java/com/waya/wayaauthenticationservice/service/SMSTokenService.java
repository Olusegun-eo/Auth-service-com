package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.OTPBase;
import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;
import com.waya.wayaauthenticationservice.response.ApiResponse;

public interface SMSTokenService {

    OTPBase generateSMSOTP(String phoneNumber);

    boolean sendSMSOTP(String phoneNumber, String fullName);

    OTPVerificationResponse verifySMSOTP(String phoneNumber, Integer otp);
}

package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;
import com.waya.wayaauthenticationservice.response.ApiResponse;

public interface SMSTokenService {

    boolean sendSMSOTP(String phoneNumber, String fullName);

    ApiResponse<OTPVerificationResponse> verifySMSOTP(String phoneNumber, Integer otp);
}

package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.response.OTPResponse;
import com.waya.wayaauthenticationservice.response.OTPVerificationResponse;
import com.waya.wayaauthenticationservice.util.profile.ApiResponse;

public interface SMSTokenService {

    void sendSMSOTP(String phoneNumber, String keycloakId);

    ApiResponse<OTPVerificationResponse> verifySMSOTP(String number, Integer otp);
}

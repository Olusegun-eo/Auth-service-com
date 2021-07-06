package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.response.OTPResponse;
import com.waya.wayaauthenticationservice.util.ApiResponse;

public interface SMSTokenService {

    void sendSMSOTP(String phoneNumber, String keycloakId);

    ApiResponse<OTPResponse> verifySMSOTP(String number, Integer otp);
}

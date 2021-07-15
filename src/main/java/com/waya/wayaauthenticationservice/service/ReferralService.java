package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.response.ReferralCodeResponse;

public interface ReferralService {
    ReferralCodeResponse getReferralCode(String userId);
}

package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.ReferralCode;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.response.ReferralCodeResponse;
import com.waya.wayaauthenticationservice.response.UserProfileResponse;

import java.util.List;
import java.util.Map;

public interface ReferralService {
    ReferralCodeResponse getReferralCode(String userId);
    List<ReferralCode> migrateReferralCode() throws Exception;
    List<UserProfileResponse> getUsersWithUpToFiveTransactions(String userId, String token) throws CustomException;
    }

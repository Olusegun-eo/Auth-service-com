package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.ReferralBonus;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.pojo.others.ReferralBonusRequest;
import com.waya.wayaauthenticationservice.response.ReferralBonusResponse;

public interface ManageReferralService {
    ReferralBonus toggleReferralAmount(Long id) throws CustomException;
    ReferralBonusResponse createReferralAmount(ReferralBonusRequest referralBonusRequest) throws CustomException;
    ReferralBonusResponse getReferralBonusResponse(ReferralBonus referralBonus);
    ReferralBonus findReferralBonus(String id) throws CustomException;
    ReferralBonus editReferralAmount(ReferralBonusRequest referralBonusRequest) throws CustomException;
}

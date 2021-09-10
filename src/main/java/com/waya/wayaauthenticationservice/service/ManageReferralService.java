package com.waya.wayaauthenticationservice.service;

import com.waya.wayaauthenticationservice.entity.Profile;
import com.waya.wayaauthenticationservice.entity.ReferralBonus;
import com.waya.wayaauthenticationservice.entity.ReferralBonusEarning;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.pojo.others.AssignReferralCodePojo;
import com.waya.wayaauthenticationservice.pojo.others.ReferralBonusRequest;
import com.waya.wayaauthenticationservice.pojo.others.UserReferralBonusPojo;
import com.waya.wayaauthenticationservice.pojo.others.UserTransferToDefaultWallet;
import com.waya.wayaauthenticationservice.response.NewWalletResponse;
import com.waya.wayaauthenticationservice.response.ReferralBonusResponse;
import com.waya.wayaauthenticationservice.response.UserProfileResponse;

import java.util.Map;

public interface ManageReferralService {
    ReferralBonus toggleReferralAmount(Long id) throws CustomException;
    ReferralBonusResponse createReferralAmount(ReferralBonusRequest referralBonusRequest) throws CustomException;
    ReferralBonusResponse getReferralBonusResponse(ReferralBonus referralBonus);
    ReferralBonus findReferralBonus(String id) throws CustomException;
    ReferralBonus editReferralAmount(ReferralBonusRequest referralBonusRequest) throws CustomException;

    Map<String, Object> getUsersWithTheirReferralsByPhoneNumber(String value, int page, int size);
    Map<String, Object> getUsersWithTheirReferrals(int page, int size);
    Map<String, Object> getUserWithoutReferralCode(int page, int size);
    Profile assignReferralCode(AssignReferralCodePojo assignReferralCodePojo);

    NewWalletResponse sendReferralBonusToUser(UserTransferToDefaultWallet transfer);
    Map<String, Object> getUserThatHaveBeenReferred(String referralCode, int page, int size);
}

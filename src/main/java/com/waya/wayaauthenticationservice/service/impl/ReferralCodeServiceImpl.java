package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.ReferralBonus;
import com.waya.wayaauthenticationservice.entity.ReferralCode;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.proxy.BillerProxy;
import com.waya.wayaauthenticationservice.repository.ProfileRepository;
import com.waya.wayaauthenticationservice.repository.ReferralBonusRepository;
import com.waya.wayaauthenticationservice.repository.ReferralCodeRepository;
import com.waya.wayaauthenticationservice.response.ReferralCodeResponse;
import com.waya.wayaauthenticationservice.response.UserProfileResponse;
import com.waya.wayaauthenticationservice.service.ReferralService;
import com.waya.wayaauthenticationservice.util.BearerTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
@Slf4j
@Service
public class ReferralCodeServiceImpl implements ReferralService {

    private final ReferralCodeRepository referralCodeRepository;
    private final ProfileServiceImpl profileService;
    private final ProfileRepository profileRepository;
    private final BillerProxy billerProxy;
    private final ReferralBonusRepository referralBonusRepository;

    @Autowired
    public ReferralCodeServiceImpl(ReferralCodeRepository referralCodeRepository, ProfileServiceImpl profileService, ProfileRepository profileRepository, BillerProxy billerProxy, ReferralBonusRepository referralBonusRepository) {
        this.referralCodeRepository = referralCodeRepository;
        this.profileService = profileService;
        this.profileRepository = profileRepository;
        this.billerProxy = billerProxy;
        this.referralBonusRepository = referralBonusRepository;
    }

    @Override
    public ReferralCodeResponse getReferralCode(String userId) {
        Optional<ReferralCode> referralCode = Optional.of(referralCodeRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException("invalid userId", HttpStatus.NOT_FOUND)));
        return referralCode.map(x -> new ReferralCodeResponse(x.getReferralCode())).get();

    }

    public List<ReferralCode> migrateReferralCode() throws Exception {
        String token = BearerTokenUtil.getBearerTokenHeader();
        List<ReferralCode> errorList = new ArrayList<>();
        List<ReferralCode> referralCodeList = referralCodeRepository.findAll();
        for (ReferralCode referralCode: referralCodeList){
            try{
                profileService.postMigrateReferralCode(referralCode.getProfile().getId(),referralCode.getUserId(), token);
            }catch (Exception exception) {
                errorList.add(referralCode);
                continue;
            }
        }
        log.info("List with errors " + errorList);

        return referralCodeList;
    }



    public List<UserProfileResponse> getUsersWithUpToFiveTransactions(String userId, String token) throws CustomException {
        List<UserProfileResponse> newUserProfileResponseList = new ArrayList<>();
        ReferralBonus referralBonus = referralBonusRepository.findByActive(true);
        List<UserProfileResponse>  userProfileResponseList = profileService.findAllUserReferral(userId,"100");
        if (userProfileResponseList.size() < 0)
            throw new CustomException("User do not have any referred user", HttpStatus.EXPECTATION_FAILED);
        try {
            Collection col = userProfileResponseList;
            Iterator i = col.iterator();
            while (i.hasNext()) {
                UserProfileResponse userProfileResponse = (UserProfileResponse) i.next();
                long count = getTrans(userProfileResponse.getUserId(), token);
                if (count > referralBonus.getNumberOfTransaction()){
                    newUserProfileResponseList.add(userProfileResponse);
                }
            }
        } catch (Exception ex) {
            throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
        return newUserProfileResponseList;
    }



    public long getTrans(String userId, String token) throws CustomException {
        try {
            ResponseEntity<Long> responseEntity = billerProxy.getTransaction(userId,token);
            Long infoResponse = responseEntity.getBody();

            log.info("infoResponse ::: " + infoResponse);

           // long count = infoResponse.data;

            return infoResponse;
        } catch (Exception ex) {
            throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }


}

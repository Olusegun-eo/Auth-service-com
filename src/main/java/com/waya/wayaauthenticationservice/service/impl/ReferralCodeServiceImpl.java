package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.ReferralCode;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.pojo.others.Transactions;
import com.waya.wayaauthenticationservice.proxy.BillerProxy;
import com.waya.wayaauthenticationservice.repository.ReferralCodeRepository;
import com.waya.wayaauthenticationservice.response.ReferralCodeResponse;
import com.waya.wayaauthenticationservice.response.ResponseObj;
import com.waya.wayaauthenticationservice.response.UserProfileResponse;
import com.waya.wayaauthenticationservice.service.ReferralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReferralCodeServiceImpl implements ReferralService {

    private final ReferralCodeRepository referralCodeRepository;
    private final ProfileServiceImpl profileService;
    private final BillerProxy billerProxy;

    @Autowired
    public ReferralCodeServiceImpl(ReferralCodeRepository referralCodeRepository, ProfileServiceImpl profileService, BillerProxy billerProxy) {
        this.referralCodeRepository = referralCodeRepository;
        this.profileService = profileService;
        this.billerProxy = billerProxy;
    }

    @Override
    public ReferralCodeResponse getReferralCode(String userId) {
        Optional<ReferralCode> referralCode = Optional.of(referralCodeRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException("invalid userId", HttpStatus.NOT_FOUND)));
        return referralCode.map(x -> new ReferralCodeResponse(x.getReferralCode())).get();

    }


    public List<UserProfileResponse> getUsersWithUpToFiveTransactions(String userId, String token) throws CustomException {

        List<UserProfileResponse> newUserProfileResponseList = new ArrayList<>();
        List<UserProfileResponse>  userProfileResponseList = profileService.findAllUserReferral(userId,"100");
        if (userProfileResponseList.size() < 0)
            throw new CustomException("User do not have any referred user", HttpStatus.EXPECTATION_FAILED);

        try {
            Collection col = userProfileResponseList;
            Iterator i = col.iterator();
            while (i.hasNext()) {
                UserProfileResponse userProfileResponse = (UserProfileResponse) i.next();

                List<Transactions> count = getTrans(userProfileResponse.getUserId(), token);
                if (count.size() > 5){
//                    transactions.add((Transactions) count);
                    newUserProfileResponseList.add(userProfileResponse);
                }
            }
        } catch (Exception ex) {
            throw new CustomException(ex.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }

        return newUserProfileResponseList;
    }

    public List<Transactions> getTrans(String userId, String token) throws CustomException {
        //List<Transactions> transactions = transactionService.getUserTransactions(userId, 0, 10 , token);

        ResponseEntity<ResponseObj<List<Transactions>>> responseEntity = billerProxy.getTransaction(userId,token);
        ResponseObj infoResponse = (ResponseObj) responseEntity.getBody();
        List<Transactions> transactions = (List<Transactions>) infoResponse.data;

        return transactions;
    }



}

package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.ReferralBonus;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.pojo.others.ReferralBonusRequest;
import com.waya.wayaauthenticationservice.repository.ReferralBonusRepository;
import com.waya.wayaauthenticationservice.response.ReferralBonusResponse;
import com.waya.wayaauthenticationservice.service.ManageReferralService;
import com.waya.wayaauthenticationservice.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class ManageReferralServiceImpl implements ManageReferralService {


    private final ReferralBonusRepository referralBonusRepository;

    @Autowired
    public ManageReferralServiceImpl(ReferralBonusRepository referralBonusRepository) {
        this.referralBonusRepository = referralBonusRepository;
    }


    private ReferralBonus getReferralBonusById(Long id) throws CustomException {
        return referralBonusRepository.findById(id).orElseThrow(() -> new CustomException("Invalid id provided", HttpStatus.NOT_FOUND));
    }

    public ReferralBonus editReferralAmount(ReferralBonusRequest referralBonusRequest) throws CustomException {
        try {
            ReferralBonus referralBonus = getReferralBonusById(referralBonusRequest.getId());
            if (referralBonus == null)
                throw new CustomException(Constant.NOTFOUND, HttpStatus.NOT_FOUND);

            referralBonus.setAmount(referralBonusRequest.getAmount());
            referralBonus.setDescription(referralBonusRequest.getDescription());
            referralBonus.setUserType(referralBonusRequest.getUserType());
            ReferralBonus referralBonus1 = referralBonusRepository.save(referralBonus);

            // notify inApp

            return referralBonus1;
        } catch (Exception exception) {
            log.error("Unable to update referral bonus fee", exception);
            throw new CustomException(exception.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    public ReferralBonus toggleReferralAmount(Long id) throws CustomException {

        if (Objects.isNull(id)){
            throw new CustomException(Constant.ID_IS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        ReferralBonus referralBonus = referralBonusRepository.findById(id).orElseThrow(() -> new CustomException(Constant.ID_IS_UNKNOWN, HttpStatus.BAD_REQUEST));

        referralBonus.setActive(!referralBonus.isActive());
        try{
            ReferralBonus referralBonus1 = referralBonusRepository.save(referralBonus);
            return referralBonus1;

            // notify inApp

        } catch (Exception exception) {
            log.error("Unable to update referral bonus fee", exception);
            throw new CustomException(exception.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }


    public ReferralBonusResponse createReferralAmount(ReferralBonusRequest referralBonusRequest) throws CustomException {
        try {

            log.info("inside {} :::::: " + referralBonusRequest);
            ReferralBonus referralBonus = new ReferralBonus();
            referralBonus.setAmount(referralBonusRequest.getAmount());
            referralBonus.setDescription(referralBonusRequest.getDescription());
            referralBonus.setUserType(referralBonusRequest.getUserType());
            referralBonus = referralBonusRepository.save(referralBonus);
            log.info(" referralBonusreferralBonus ::::" + referralBonus);
            return getReferralBonusResponse(referralBonus);
        } catch (Exception exception) {
            log.error("Unable to save referral bonus fee", exception);
            throw new CustomException(Constant.ERROR_MESSAGE,HttpStatus.EXPECTATION_FAILED);
        }
    }

    public ReferralBonusResponse getReferralBonusResponse(ReferralBonus referralBonus){
        ReferralBonusResponse referralBonusResponse = new ReferralBonusResponse();
        referralBonusResponse.setId(referralBonus.getId());
        referralBonusResponse.setAmount(referralBonus.getAmount());
        referralBonusResponse.setDescription(referralBonus.getDescription());
        referralBonusResponse.setUserType(referralBonus.getUserType());

        // notify inApp
        return referralBonusResponse;
    }

    public ReferralBonus findReferralBonus(String id) throws CustomException {
        try {

            return getReferralBonusById(Long.parseLong(id));
        } catch (Exception exception) {
            log.error("Unable to get referral bonus amount", exception);
            throw new CustomException(Constant.ERROR_MESSAGE, HttpStatus.EXPECTATION_FAILED);
        }
    }
}

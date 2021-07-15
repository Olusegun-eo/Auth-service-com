package com.waya.wayaauthenticationservice.service.impl;

import com.waya.wayaauthenticationservice.entity.ReferralCode;
import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.repository.ReferralCodeRepository;
import com.waya.wayaauthenticationservice.response.ReferralCodeResponse;
import com.waya.wayaauthenticationservice.service.ReferralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReferralCodeServiceImpl implements ReferralService {

    private final ReferralCodeRepository referralCodeRepository;

    @Autowired
    public ReferralCodeServiceImpl(ReferralCodeRepository referralCodeRepository) {
        this.referralCodeRepository = referralCodeRepository;
    }

    @Override
    public ReferralCodeResponse getReferralCode(String userId) {
        Optional<ReferralCode> referralCode = Optional.of(referralCodeRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException("invalid userId", HttpStatus.NOT_FOUND)));

        return referralCode.map(x -> new ReferralCodeResponse(x.getReferalCode())).get();

    }
}

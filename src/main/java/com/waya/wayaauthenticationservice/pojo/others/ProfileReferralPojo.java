package com.waya.wayaauthenticationservice.pojo.others;

import lombok.Data;

import java.util.UUID;

@Data
public class ProfileReferralPojo {
    private UUID id;
    private String referralCode;
    private String userId;
    private ProfileDto profile;
}

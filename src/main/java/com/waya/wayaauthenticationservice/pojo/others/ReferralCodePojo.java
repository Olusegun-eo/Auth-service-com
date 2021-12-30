package com.waya.wayaauthenticationservice.pojo.others;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReferralCodePojo {

    private UUID id;
    private String referralCode;
    private String userId;
    private String profile;
    private LocalDateTime createdAt;

    public ReferralCodePojo(String referralCode, String profile, String userId) {
        this.referralCode = referralCode;
        this.profile = profile;
        this.userId = userId;
    }
}

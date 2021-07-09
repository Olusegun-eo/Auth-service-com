package com.waya.wayaauthenticationservice.pojo;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReferralCodePojo {

    private UUID id;
    private String referralCode;
    private String userId;
    private ProfileDto profile;
    private LocalDateTime createdAt;

    public ReferralCodePojo(String referralCode, ProfileDto profile, String userId) {
        this.referralCode = referralCode;
        this.profile = profile;
        this.userId = userId;
    }
}

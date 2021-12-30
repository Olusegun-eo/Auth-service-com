package com.waya.wayaauthenticationservice.pojo.others;

import lombok.Data;

import java.util.UUID;

@Data
public class ReferralCodeRequest {
    private String userId;
    private UUID profile;
}

package com.waya.wayaauthenticationservice.pojo.others;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class AssignReferralCodePojo {
    @NotEmpty
    @NotNull(message = "userId must be provided")
    private String userId;
    @NotNull(message = "referralCode must be provided")
    private String referralCode;
}

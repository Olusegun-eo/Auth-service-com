package com.waya.wayaauthenticationservice.response;

import com.waya.wayaauthenticationservice.util.UserType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReferralBonusResponse {
    private Long id;
    private BigDecimal amount;
    private String description;
    private UserType userType;
}

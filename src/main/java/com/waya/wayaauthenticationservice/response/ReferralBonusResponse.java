package com.waya.wayaauthenticationservice.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReferralBonusResponse {
    private Long id;
    private BigDecimal amount;
    private Integer numberOfTransaction;
}

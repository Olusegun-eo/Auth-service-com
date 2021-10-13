package com.waya.wayaauthenticationservice.pojo.others;

import com.waya.wayaauthenticationservice.util.ReferralBonusStatus;
import lombok.Data;

import javax.persistence.Enumerated;
import java.math.BigDecimal;

import static javax.persistence.EnumType.STRING;

@Data
public class ReferralBonusEarningRequest {
    private BigDecimal amount;
    private String userId;
    private ReferralBonusStatus status;
}

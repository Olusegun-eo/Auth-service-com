package com.waya.wayaauthenticationservice.entity;

import com.waya.wayaauthenticationservice.util.ReferralBonusStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;

import static javax.persistence.EnumType.STRING;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "m_referral_bonus_earning")
public class ReferralBonusEarning extends AuditModel  {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private BigDecimal amount = BigDecimal.valueOf(0.0);

    private String userId;

    @Enumerated(STRING)
    private ReferralBonusStatus status;

}

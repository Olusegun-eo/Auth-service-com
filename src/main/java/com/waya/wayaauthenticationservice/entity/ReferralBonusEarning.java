package com.waya.wayaauthenticationservice.entity;

import com.waya.wayaauthenticationservice.util.UserType;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
@Data
@Entity
@Table(name = "m_referral_bonus_earning")
public class ReferralBonusEarning extends AuditModel  {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private BigDecimal amount = BigDecimal.valueOf(0.0);

    private String userId;

}

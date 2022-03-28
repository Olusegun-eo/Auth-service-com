package com.waya.wayaauthenticationservice.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "m_referral_bonus")
public class ReferralBonus extends AuditModel  {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private BigDecimal amount = BigDecimal.valueOf(0.0);

    private Integer numberOfTransaction;


    private boolean active;

}

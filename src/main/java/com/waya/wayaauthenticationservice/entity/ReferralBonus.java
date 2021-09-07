package com.waya.wayaauthenticationservice.entity;

import com.waya.wayaauthenticationservice.util.UserType;
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

    private String description;

    @Column(name = "user_type", nullable = true)
    @Enumerated(EnumType.STRING)
    private UserType userType;

    private boolean active = true;

}

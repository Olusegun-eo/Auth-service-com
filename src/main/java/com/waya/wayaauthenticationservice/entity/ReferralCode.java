package com.waya.wayaauthenticationservice.entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@Table(name = "m_referral_tbl")
public class ReferralCode {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true)
    private String referralCode;

    @Column(unique = true)
    private String userId;

    @OneToOne
    @JoinColumn(name = "profile_id", referencedColumnName = "id", unique = true, nullable = false)
    private Profile profile;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public ReferralCode(String referralCode, Profile profile, String userId) {
        this.referralCode = referralCode;
        this.profile = profile;
        this.userId = userId;
    }
}
